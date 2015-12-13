package at.yawk.math.algorithm

import at.yawk.math.data.*
import org.slf4j.LoggerFactory
import java.math.BigInteger
import kotlin.math.div
import kotlin.math.plus
import kotlin.math.times
import kotlin.math.unaryMinus

/**
 * @author yawkat
 */
object RealExpressionField : ExpressionField {
    private val log = LoggerFactory.getLogger(RealExpressionField::class.java)

    override fun simplify(expression: Expression): Expression {
        return expression.visit(object : ExpressionVisitor {
            override fun postEnterExpression(expression: Expression): Expression {
                return visitSingleExpression(expression)
            }

            override fun visitSingleExpression(expression: Expression): Expression {
                val simplified = simplify0(expression)
                log.trace("{} -> {}", expression, simplified)
                return simplified
            }
        })
    }

    private fun simplify0(expression: Expression): Expression {
        when (expression) {
            is ExponentiationExpression -> {
                val base = expression.base
                val exponent = expression.exponent
                if (exponent == Expressions.minusOne && base is Rational) {
                    return base.reciprocal
                }
                if (exponent == Expressions.one) {
                    return base
                }
                if (base is RealNumberExpression && exponent is Rational) {
                    return normalizeRationalExponentProductToReal(listOf(RationalExponentiation(base, exponent)))
                }
                return expression
            }
            is AdditionExpression -> return add(expression.components)
            is MultiplicationExpression -> return multiply(expression.components)
            is GcdExpression -> {
                if (expression.left is IntegerExpression && expression.left.sign == Sign.POSITIVE &&
                        expression.right is IntegerExpression && expression.right.sign == Sign.POSITIVE) {
                    return GcdSolver.gcd(expression.left, expression.right)
                } else {
                    return expression
                }
            }
            is LcmExpression -> {
                if (expression.left is IntegerExpression && expression.left.sign == Sign.POSITIVE &&
                        expression.right is IntegerExpression && expression.right.sign == Sign.POSITIVE) {
                    return LcmSolver.lcm(expression.left, expression.right)
                } else {
                    return expression
                }
            }
            is DotProductExpression -> {
                if (expression.left is Vector && expression.right is Vector
                        && expression.left.dimension == expression.right.dimension
                        && expression.left.dimension > 0) {
                    var sum: Expression? = null
                    expression.left.rows.forEachIndexed { i, lhs ->
                        val rhs = expression.right[i]
                        if (sum == null) {
                            sum = multiply(listOf(lhs, rhs))
                        } else {
                            sum = add(listOf(sum!!, multiply(listOf(lhs, rhs))))
                        }
                    }
                    return simplify(sum!!)
                } else {
                    return expression
                }
            }
            is IntegerExpression -> return expression
            is Rational -> {
                if (expression.denominator == Expressions.one) return expression.numerator
                if (expression.denominator == Expressions.minusOne) return expression.numerator.negate
                return expression
            }
            else -> return expression
        }
    }

    private class Adder {
        var local = LocalRational.ZERO
        var newExpressions = arrayListOf<Expression>()

        fun pushAll(expressions: List<Expression>) {
            for (expression in expressions) {
                push(expression)
            }
        }

        fun push(expression: Expression) {
            when (expression) {
                is IntegerExpression -> local += expression
                is Rational -> local += expression
                is Vector -> {
                    var merged = false
                    for (i in 0..newExpressions.size - 1) {
                        val item = newExpressions[i]
                        if (item is Vector && expression.isCompatibleWith(item)) {
                            // we can merge these two vectors
                            newExpressions[i] = item.mapIndexed { i, row -> add(listOf(row, expression[i])) }
                            merged = true
                            break
                        }
                    }
                    if (!merged) newExpressions.add(expression)
                }
                is RationalExponentiationProduct -> newExpressions.add(normalizeRationalExponentProductToReal(expression.components))
                is ExponentiationExpression -> {
                    val base = expression.base
                    val exponent = expression.exponent
                    if (base is RealNumberExpression && exponent is Rational) {
                        newExpressions.add(normalizeRationalExponentProductToReal(
                                listOf(RationalExponentiation(base, exponent))))
                    } else {
                        newExpressions.add(expression)
                    }
                }
                is AdditionExpression -> pushAll(expression.components)
                else -> newExpressions.add(expression)
            }
        }

        fun flushLocal() {
            if (local != LocalRational.ZERO) newExpressions.add(local.toRational())
            local = LocalRational.ZERO
        }

        fun toExpression(): Expression {
            flushLocal()

            if (newExpressions.isEmpty()) return Expressions.zero
            if (newExpressions.size == 1) return newExpressions[0]
            return AdditionExpression(newExpressions)
        }
    }

    private fun add(expressions: List<Expression>): Expression {
        val adder = Adder()
        adder.pushAll(expressions)
        return adder.toExpression()
    }

    private fun multiply(expressions: List<Expression>): Expression {
        val newExpressions = arrayListOf<Expression>()
        val newVectors = arrayListOf<Vector>()

        // combine rationals
        val rationalExponentProductItems = arrayListOf<RationalExponentiation>()
        for (expression in expressions) {
            when (expression) {
                is RealNumberExpression -> rationalExponentProductItems.add(RationalExponentiation(expression, Expressions.one))
                is Vector -> newVectors.add(expression)
                else -> newExpressions.add(expression)
            }
        }
        val rationalExponentProduct = normalizeRationalExponentProductToReal(rationalExponentProductItems)
        if (rationalExponentProduct.zero) return Expressions.zero

        if (rationalExponentProduct != Expressions.one) {

            // merge rational and vectors
            if (newVectors.isEmpty()) {
                // no vectors - just append the rational
                newExpressions.add(rationalExponentProduct)
            } else {
                // at least one vector - merge the rational into the vector
                newExpressions.addAll(newVectors.map { it.map { row -> multiply(listOf(row, rationalExponentProduct)) } })
            }
        } else {
            // add vectors without multiplication since the constant factor is 0
            newExpressions.addAll(newVectors)
        }

        if (newExpressions.isEmpty()) return Expressions.one
        if (newExpressions.size == 1) return newExpressions[0]
        return MultiplicationExpression(newExpressions)
    }

    private fun safeRoot(base: BigInteger, exponent: BigInteger): BigInteger? {
        assert(exponent.signum() > 0)
        val approximatePow = Math.round(Math.pow(Math.abs(base.toDouble()), 1 / exponent.toDouble()))
        val approximatePowBig = BigInteger.valueOf(approximatePow)
        if (approximatePowBig.pow(exponent.toInt()) == base) {
            return approximatePowBig
        } else
            return null
    }

    private fun safeIntegerExponentiation(base: IntegerExpression, exponent: Rational): IntegerExpression? {
        if (exponent == Expressions.one) return base // shortcut
        try {
            val rootContent = base.value.pow(exponent.numerator.value.intValueExact())
            // check for even root of negative base (sqrt(-1))
            if (exponent.denominator.even && rootContent.signum() == -1) return null
            val result = safeRoot(rootContent, exponent.denominator.value) ?: return null
            return IntegerExpression(result)
        } catch(e: ArithmeticException) {
            // too large exponent, can't simplify
            return null
        }
    }

    private fun normalizeRationalExponentProductToReal(components: List<RationalExponentiation>): RealNumberExpression {
        val normalized = normalizeRationalExponentProduct(components)
        // if size is 0, the product is 1
        // normalization will make sure components will be empty when value is 1
        if (normalized.size == 0) return Expressions.one
        if (normalized.size == 1 && normalized[0].exponent == Expressions.one) return normalized[0].base
        // try representing as a single rational
        if (normalized.all { it.exponent.abs == Expressions.one && it.base is IntegerExpression }) {
            return normalized.fold(LocalRational.ONE, { lhs, rhs ->
                if (rhs.exponent == Expressions.one) {
                    lhs * rhs.base as IntegerExpression
                } else {
                    assert(rhs.exponent == Expressions.rational(-1, 1))
                    lhs / rhs.base as IntegerExpression
                }
            }).toRational()
        }
        return RationalExponentiationProduct(normalized)
    }

    private fun normalizeRationalExponentProduct(components: List<RationalExponentiation>): List<RationalExponentiation> {
        // remove rational bases
        return components.flatMap {
            when (it.base) {
                is Rational ->
                    if (it.base.denominator == Expressions.one) {
                        // solve int exponentiation where possible (2^2 -> 4, 4^1/2 -> 2)
                        val result = safeIntegerExponentiation(it.base.numerator, it.exponent)
                        if (result == null) {
                            listOf(it)
                        } else {
                            listOf(RationalExponentiation(result, Expressions.one))
                        }
                    } else {
                        // explode rationals
                        normalizeRationalExponentProduct(listOf(
                                RationalExponentiation(it.base.numerator, it.exponent),
                                RationalExponentiation(it.base.denominator, it.exponent.negate)
                        ))
                    }
                is RationalExponentiationProduct -> normalizeRationalExponentProduct(it.base.components.map { c ->
                    val newExponent = (LocalRational.ofRational(it.exponent) * c.exponent).normalize().toRational()
                    RationalExponentiation(c.base, newExponent)
                })
                else -> listOf(it)
            }
        }
                // group by base: same base -> add exponents
                .groupBy { it.base }
                .map {
                    if (it.value.size == 1) {
                        it.value[0]
                    } else {
                        // sum exponents
                        val exponentSum = it.value.fold(LocalRational.ZERO, { lhs, rhs -> lhs + rhs.exponent })
                                .normalize().toRational()
                        RationalExponentiation(it.key, exponentSum)
                    }
                }
                // normalize exponents
                .map { RationalExponentiation(it.base, normalizeRational(it.exponent)) }
                // remove 0 exponents
                .filter { it.exponent.numerator != Expressions.zero }
                // evaluate integer components where possible
                .map {
                    if (it.base !is IntegerExpression) it else {
                        val result = safeIntegerExponentiation(it.base, it.exponent)
                        if (result == null) it else {
                            RationalExponentiation(result, Expressions.one)
                        }
                    }
                }
                // group by exponent: same exponent -> multiply values
                .groupBy { it.exponent }
                .flatMap {
                    if (it.value.size == 1) {
                        it.value
                    } else {
                        var baseProduct = LocalRational.ONE
                        val nonCombinable = arrayListOf<RationalExponentiation>()
                        for (expr in it.value) {
                            when (expr.base) {
                                is IntegerExpression -> baseProduct *= expr.base
                                else -> nonCombinable.add(expr)
                            }
                        }
                        baseProduct = baseProduct.normalize()
                        if (baseProduct != LocalRational.ONE) nonCombinable.add(RationalExponentiation(baseProduct.toRational(), it.key))
                        nonCombinable
                    }
                }
                // when exponent even -> use absolute base
                .map { if (it.exponent.numerator.even) RationalExponentiation(it.base.abs, it.exponent) else it }
                // remove 1 bases
                .filter { it.base != Expressions.one }
    }

    private fun normalizeRational(rational: Rational): Rational {
        return LocalRational.ofRational(rational).normalize().toRational()
    }
}

internal data class LocalRational(val numerator: BigInteger, val denominator: BigInteger) {
    companion object {
        val ZERO = LocalRational(BigInteger.ZERO, BigInteger.ONE)
        val ONE = LocalRational(BigInteger.ONE, BigInteger.ONE)

        fun ofRational(rational: Rational): LocalRational {
            return LocalRational(rational.numerator.value, rational.denominator.value)
        }
    }

    /**
     * Make sure that
     * - the denominator is positive
     * - the numerator is not a multiple of the denominator
     */
    fun normalize(): LocalRational {
        if (numerator == BigInteger.ZERO) {
            return ZERO
        }
        if (numerator == BigInteger.ONE && denominator == BigInteger.ONE) {
            return ONE
        }
        // ensure denominator > 0
        if (denominator.signum() == -1) {
            return LocalRational(-numerator, -denominator).normalize()
        }
        val gcd = numerator.gcd(denominator)
        if (gcd > BigInteger.ONE) {
            return LocalRational(numerator / gcd, denominator / gcd).normalize()
        }
        return this
    }

    fun toRational(): Rational {
        if (numerator == BigInteger.ZERO) {
            return Expressions.zero
        } else if (denominator == BigInteger.ONE) {
            return Expressions.int(numerator)
        } else {
            return SimpleRational(Expressions.int(numerator), Expressions.int(denominator))
        }
    }

    operator fun plus(int: IntegerExpression): LocalRational {
        return LocalRational(
                numerator + int.value * denominator,
                denominator
        ).normalize()
    }

    operator fun plus(rational: Rational): LocalRational {
        return LocalRational(
                numerator * rational.denominator.value + rational.numerator.value * denominator,
                denominator * rational.denominator.value
        ).normalize()
    }

    operator fun times(int: IntegerExpression): LocalRational {
        return LocalRational(
                numerator * int.value,
                denominator
        ).normalize()
    }

    operator fun times(rational: Rational): LocalRational {
        return LocalRational(
                numerator * rational.numerator.value,
                denominator * rational.denominator.value
        ).normalize()
    }

    operator fun div(int: IntegerExpression): LocalRational {
        return LocalRational(
                numerator,
                denominator * int.value
        ).normalize()
    }
}
