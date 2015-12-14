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

    protected open fun simplifyDotProduct(expression: DotProductExpression): Expression {
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

    protected open fun simplifyLcm(expression: LcmExpression): Expression {
        if (expression.left is IntegerExpression && expression.left.sign == Sign.POSITIVE &&
                expression.right is IntegerExpression && expression.right.sign == Sign.POSITIVE) {
            // if both are positive integers, calculate the LCM

            return LcmSolver.lcm(expression.left, expression.right)
        } else {
            return expression
        }
    }

    protected open fun simplifyGcd(expression: GcdExpression): Expression {
        if (expression.left is IntegerExpression && expression.left.sign == Sign.POSITIVE &&
                expression.right is IntegerExpression && expression.right.sign == Sign.POSITIVE) {
            // if both are positive integers, calculate the GCD

            return GcdSolver.gcd(expression.left, expression.right)
        } else {
            return expression
        }
    }

    protected open fun simplifyExponentiation(expression: ExponentiationExpression): Expression {
        val base = expression.base
        val exponent = expression.exponent
        // (a/b)^-1 = b/a
        if (exponent == Expressions.minusOne && base is Rational) {
            return base.reciprocal
        }
        // a^1 = a
        if (exponent == Expressions.one) {
            return base
        }
        // transform constant exponentiation to RationalExponentProduct so it can be further simplified
        if (base is RealNumberExpression && exponent is Rational) {
            return normalizeRationalExponentProductToReal(listOf(RationalExponentiation(base, exponent)))
        }
        return expression
    }

    /**
     * Helper class used to add a set of expressions
     */
    protected open inner class Adder {
        var rationalAddend = LocalRational.ZERO
        var addends = arrayListOf<Expression>()

        internal final fun pushAll(expressions: List<Expression>) = expressions.forEach { push(it) }

        internal final fun push(expression: Expression) {
            when (expression) {
                is Rational -> pushRational(expression)
                is Vector -> pushVector(expression)
                is RationalExponentiationProduct -> pushRationalExponentiationProduct(expression)
                is AdditionExpression -> pushAddition(expression)
                else -> pushOther(expression)
            }
        }

        /**
         * Push the given rational by adding it to [rationalAddend]
         */
        protected open fun pushRational(rational: Rational) {
            rationalAddend += rational
        }

        /**
         * (a,b)+(c,d) = (a+b,c+d)
         */
        protected open fun pushVector(vector: Vector) {
            val mergeableVectorIndex = addends.indexOfFirst { it is Vector && vector.isCompatibleWith(it) }
            if (mergeableVectorIndex == -1) {
                // cannot merge
                addends.add(vector)
            } else {
                // merge with a vector
                addends[mergeableVectorIndex] = (addends[mergeableVectorIndex] as Vector)
                        .mapIndexed { i, row -> simplifyAddition(listOf(row, vector[i])) }
            }
        }

        protected open fun pushRationalExponentiationProduct(expression: RationalExponentiationProduct) {
            val simplified = normalizeRationalExponentProductToReal(expression.components)
            if (simplified is RationalExponentiationProduct) {
                addends.add(simplified)
            } else {
                // if the simplified version is not a RationalExponentiationProduct, it might be a Rational or Integer and we can optimize that
                push(simplified)
            }
        }

        protected open fun pushAddition(expression: AdditionExpression) {
            pushAll(expression.components)
        }

        protected open fun pushOther(expression: Expression) {
            addends.add(expression)
        }

        /**
         * Flush the [rationalAddend] into the [addends] and reset [rationalAddend] back to `0`.
         */
        internal fun flushLocal() {
            if (rationalAddend != LocalRational.ZERO) {
                addends.add(rationalAddend.toRational())
                rationalAddend = LocalRational.ZERO
            }
        }

        /**
         * Create an [AdditionExpression] representing all pushed expressions.
         */
        internal fun toExpression(): Expression {
            flushLocal()

            if (addends.isEmpty()) return Expressions.zero
            if (addends.size == 1) return addends[0]
            return AdditionExpression(addends)
        }
    }

    protected open fun makeAdder(): Adder {
        return Adder()
    }

    private fun simplifyAddition(expressions: List<Expression>): Expression {
        val adder = makeAdder()
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
        // 0^n = 1 with n != 0
        if (base.zero && !exponent.zero) return listOf()
        // 0^n = 1
        if (base.value == BigInteger.ONE) return listOf()

        if (exponent == Expressions.one) return listOf(RationalExponentiation(base, exponent)) // shortcut
        try {
            // computation time limit: don't exceed 512 bits in exponentiation
            if (base.value.bitCount() + exponent.numerator.value.intValueExact() > 512) {
                return listOf(RationalExponentiation(base, exponent))
            }

            val rootContent = base.value.pow(exponent.numerator.value.intValueExact())
            // shortcut
            if (exponent.denominator == Expressions.one) {
                return listOf(RationalExponentiation(Expressions.int(rootContent), Expressions.one))
            }
            // check for even root of negative base (sqrt(-1))
            if (exponent.denominator.even && rootContent.signum() == -1) {
                return listOf(RationalExponentiation(Expressions.int(rootContent), SimpleRational(Expressions.one, exponent.denominator)))
            }

            val exponentiations = arrayListOf<RationalExponentiation>()

            val denominator = exponent.denominator.value.abs()
            val rootContentFactorization = PrimeFactorSolver.factorize(rootContent.abs())

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
        // flatten product if possible
        if (normalized.size == 1) {
            if (normalized[0].exponent == Expressions.one) return normalized[0].base
            return normalized[0]
        }
        return RationalExponentiationProduct(normalized)
    }

    private fun normalizeRationalExponentProduct(components: List<RationalExponentiation>): List<RationalExponentiation> {
        // remove rational bases
        var oldComponents = components

        do {
            var newComponents = oldComponents

            newComponents = newComponents.flatMap {
                when (it.base) {
                    is Rational ->
                        if (it.base.denominator == Expressions.one) {
                            listOf(RationalExponentiation(it.base.numerator, it.exponent))
                        } else {
                            // explode rationals
                            listOf(
                                    RationalExponentiation(it.base.numerator, it.exponent),
                                    RationalExponentiation(it.base.denominator, it.exponent.negate)
                            )
                        }
                    is RationalExponentiationProduct -> normalizeRationalExponentProduct(it.base.components.map { c ->
                        val newExponent = (LocalRational.ofRational(it.exponent) * c.exponent).normalize().toRational()
                        RationalExponentiation(c.base, newExponent)
                    })
                    is RationalExponentiation -> {
                        val newExponent = (LocalRational.ofRational(it.base.exponent) * it.exponent).normalize().toRational()
                        listOf(RationalExponentiation(it.base.base, newExponent))
                    }
                    else -> listOf(it)
                }
            }

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

data class LocalRational(val numerator: BigInteger, val denominator: BigInteger) {
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
