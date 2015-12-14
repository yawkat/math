package at.yawk.math.algorithm

import at.yawk.math.data.*
import org.slf4j.LoggerFactory
import java.math.BigInteger
import kotlin.math.div
import kotlin.math.plus
import kotlin.math.times
import kotlin.math.unaryMinus

/**
 * Simplification engine that computes constant values but does not solve addition, multiplication etc.
 */
object BasicRealSimplificationEngine : RealSimplificationEngine()

/**
 * Simplification engine that tries to transform expressions to a simple chain of additions, i.e.:
 * `(x - 1)(x + 1) => x^2 - 1`
 */
object DistributiveSumSimplificationEngine : RealSimplificationEngine() {
}

abstract class RealSimplificationEngine : SimplificationEngine {
    private val log = LoggerFactory.getLogger(RealSimplificationEngine::class.java)

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
            is ExponentiationExpression -> return simplifyExponentiation(expression)
            is AdditionExpression -> return simplifyAddition(expression.components)
            is MultiplicationExpression -> return simplifyMultiplication(expression.components)
            is GcdExpression -> return simplifyGcd(expression)
            is LcmExpression -> return simplifyLcm(expression)
            is DotProductExpression -> return simplifyDotProduct(expression)
            is IntegerExpression -> return expression
            is Rational -> return simplifyRational(expression)
            else -> return expression
        }
    }

    protected open fun simplifyRational(expression: Rational): Expression {
        return LocalRational.ofRational(expression)
                .normalize().toRational()
    }

    private fun simplifyDotProduct(expression: DotProductExpression): Expression {
        if (expression.left is Vector && expression.right is Vector
                && expression.left.isCompatibleWith(expression.right)
                && expression.left.dimension > 0) {
            // if both are vectors and compatible, expand the cross product

            val sum = arrayListOf<Expression>()
            expression.left.rows.forEachIndexed { i, lhs ->
                val rhs = expression.right[i]
                sum.add(simplifyMultiplication(listOf(lhs, rhs)))
            }
            return simplifyAddition(sum)
        } else {
            return expression
        }
    }

    private fun simplifyLcm(expression: LcmExpression): Expression {
        if (expression.left is IntegerExpression && expression.left.sign == Sign.POSITIVE &&
                expression.right is IntegerExpression && expression.right.sign == Sign.POSITIVE) {
            return LcmSolver.lcm(expression.left, expression.right)
        } else {
            return expression
        }
    }

    private fun simplifyGcd(expression: GcdExpression): Expression {
        if (expression.left is IntegerExpression && expression.left.sign == Sign.POSITIVE &&
                expression.right is IntegerExpression && expression.right.sign == Sign.POSITIVE) {
            return GcdSolver.gcd(expression.left, expression.right)
        } else {
            return expression
        }
    }

    private fun simplifyExponentiation(expression: ExponentiationExpression): Expression {
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

    protected inner class Adder {
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
                            newExpressions[i] = item.mapIndexed { i, row -> simplifyAddition(listOf(row, expression[i])) }
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

    private fun simplifyAddition(expressions: List<Expression>): Expression {
        val adder = Adder()
        adder.pushAll(expressions)
        return adder.toExpression()
    }

    private inner class Multiplier {
        val newExpressions = arrayListOf<Expression>()
        val newVectors = arrayListOf<Vector>()

        // combine rationals
        val rationalExponentProductItems = arrayListOf<RationalExponentiation>()

        fun pushAll(expressions: List<Expression>) {
            for (expression in expressions) {
                push(expression)
            }
        }

        fun push(expression: Expression) {
            when (expression) {
                is RationalExponentiation -> rationalExponentProductItems.add(expression)
                is RealNumberExpression -> rationalExponentProductItems.add(RationalExponentiation(expression, Expressions.one))
                is Vector -> newVectors.add(expression)
                is MultiplicationExpression -> pushAll(expression.components)
                else -> newExpressions.add(expression)
            }
        }

        fun toExpression(): Expression {
            val rationalExponentProduct = normalizeRationalExponentProductToReal(rationalExponentProductItems)
            if (rationalExponentProduct.zero) return Expressions.zero

            if (rationalExponentProduct != Expressions.one) {

                // merge rational and vectors
                if (newVectors.isEmpty()) {
                    // no vectors - just append the rational
                    newExpressions.add(rationalExponentProduct)
                } else {
                    // at least one vector - merge the rational into the vector
                    newExpressions.addAll(newVectors.map { it.map { row -> simplifyMultiplication(listOf(row, rationalExponentProduct)) } })
                }
            } else {
                // add vectors without multiplication since the constant factor is 0
                newExpressions.addAll(newVectors)
            }

            if (newExpressions.isEmpty()) return Expressions.one
            if (newExpressions.size == 1) return newExpressions[0]
            return MultiplicationExpression(newExpressions)
        }
    }

    private fun simplifyMultiplication(expressions: List<Expression>): Expression {
        val multiplier = Multiplier()
        multiplier.pushAll(expressions)
        return multiplier.toExpression()
    }

    private fun safeIntegerExponentiation(base: IntegerExpression, exponent: Rational): List<RationalExponentiation> {
        if (exponent == Expressions.one) return listOf(RationalExponentiation(base, exponent)) // shortcut
        try {
            // computation time limit: don't exceed 512 bits in exponentiation
            if (base.value.bitCount() + exponent.numerator.value.intValueExact() > 512) {
                return listOf(RationalExponentiation(base, exponent))
            }

            val rootContent = base.value.pow(exponent.numerator.value.intValueExact())
            // shortcut
            if (exponent.denominator == Expressions.one) return listOf(RationalExponentiation(base, Expressions.one))
            // check for even root of negative base (sqrt(-1))
            if (exponent.denominator.even && rootContent.signum() == -1) {
                return listOf(RationalExponentiation(Expressions.int(rootContent), SimpleRational(Expressions.one, exponent.denominator)))
            }

            val exponentiations = arrayListOf<RationalExponentiation>()

            val denominator = exponent.denominator.value.abs()
            val rootContentFactorization = PrimeFactorSolver.factorize(rootContent)

            for (e in rootContentFactorization.primeFactors) {
                var factorNumerator = BigInteger.valueOf(e.value.toLong())
                var factorDenominator = denominator

                val gcd = factorNumerator.gcd(factorDenominator)
                factorNumerator /= gcd
                factorDenominator /= gcd

                exponentiations.add(RationalExponentiation(
                        Expressions.int(e.key.toLong()),
                        LocalRational(factorNumerator, factorDenominator).normalize().toRational()
                ))
            }

            if (rootContentFactorization.hasRemainder()) {
                exponentiations.add(RationalExponentiation(
                        Expressions.int(rootContentFactorization.remainder),
                        exponent.denominator
                ))
            }

            // apply sign
            if (rootContent.signum() == -1) {
                exponentiations.add(RationalExponentiation(Expressions.minusOne, Expressions.one))
            }
            return exponentiations
        } catch(e: ArithmeticException) {
            // too large exponent, can't simplify
            return listOf(RationalExponentiation(base, exponent))
        }
    }

    private fun normalizeRationalExponentProductToReal(components: List<RationalExponentiation>): RealNumberExpression {
        val normalized = normalizeRationalExponentProduct(components)
        // if size is 0, the product is 1
        // normalization will make sure components will be empty when value is 1
        if (normalized.size == 0) return Expressions.one
        if (normalized.size == 1 && normalized[0].exponent == Expressions.one) return normalized[0].base
        // try representing as a single rational
        // base is never rational, always int or irrational!
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
        var oldComponents = components.flatMap {
            when (it.base) {
                is Rational ->
                    if (it.base.denominator == Expressions.one) {
                        listOf(RationalExponentiation(it.base.numerator, it.exponent))
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
                is RationalExponentiation -> {
                    val newExponent = (LocalRational.ofRational(it.exponent) * it.exponent).normalize().toRational()
                    listOf(RationalExponentiation(it.base, newExponent))
                }
                else -> listOf(it)
            }
        }

        do {
            var newComponents = oldComponents

            // group by base: same base -> add exponents
            newComponents = newComponents.groupBy { it.base }.map {
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
            newComponents = newComponents.map { RationalExponentiation(it.base, normalizeRational(it.exponent)) }

            // remove 0 exponents
            newComponents = newComponents.filter { it.exponent.numerator != Expressions.zero }

            // evaluate integer components where possible
            newComponents = newComponents.flatMap {
                if (it.base !is IntegerExpression) listOf(it) else {
                    safeIntegerExponentiation(it.base, it.exponent)
                }
            }

            // group by exponent: same exponent -> multiply values
            newComponents = newComponents.groupBy { it.exponent }.flatMap {
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
            newComponents = newComponents.map { if (it.exponent.numerator.even) RationalExponentiation(it.base.abs, it.exponent) else it }
                    // remove 1 bases
                    .filter { it.base != Expressions.one }

            var changed = newComponents != oldComponents
            oldComponents = newComponents
        } while (changed)

        return oldComponents
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
