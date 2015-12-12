package at.yawk.math.algorithm

import at.yawk.math.data.*
import java.math.BigInteger
import kotlin.math.div
import kotlin.math.plus
import kotlin.math.times
import kotlin.math.unaryMinus

/**
 * @author yawkat
 */
object RealExpressionField : ExpressionField {
    override fun simplify(expression: Expression): Expression {
        return expression.visit(object : ExpressionVisitor {
            override fun postEnterExpression(expression: Expression): Expression {
                return visitSingleExpression(expression)
            }

            override fun visitSingleExpression(expression: Expression): Expression {
                when (expression) {
                    is ExponentiationExpression -> {
                        val base = expression.base
                        if (expression.exponent == Expressions.minusOne) {
                            if (base is IntegerExpression) {
                                return Rational(Expressions.one, base)
                            }
                            if (base is Rational) {
                                return Rational(base.denominator, base.numerator)
                            }
                        }
                        // todo: RationalExponentProduct
                        if (expression.exponent == Expressions.one) {
                            return base
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
                                val rhs = expression.right.rows[i]
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
                    else -> return expression
                }
            }
        })
    }

    private fun add(expressions: List<Expression>): Expression {
        val newExpressions = arrayListOf<Expression>()

        // combine rationals
        var numerator = BigInteger.ZERO
        var denominator = BigInteger.ONE // always > 0
        for (expression in expressions) {
            when (expression) {
                is IntegerExpression -> numerator += denominator * expression.value
                is Rational -> {
                    // a/b + c/d = (a*d+c*b)/(b*d)
                    numerator = numerator * expression.denominator.value + expression.numerator.value * denominator
                    denominator *= expression.denominator.value
                    // ensure denominator > 0
                    when (denominator.signum()) {
                        0 -> {
                            numerator = BigInteger.ZERO
                            denominator = BigInteger.ONE
                        }
                        -1 -> {
                            numerator = -numerator
                            denominator = denominator.abs()
                        }
                    }
                }
                is Vector -> {
                    var merged = false
                    for (i in 0..newExpressions.size - 1) {
                        val item = newExpressions[i]
                        if (item is Vector && item.dimension == expression.dimension) {
                            // we can merge these two vectors
                            newExpressions[i] = item.mapIndexed { i, row -> add(listOf(row, expression.rows[i])) }
                            merged = true
                            break
                        }
                    }
                    if (!merged) newExpressions.add(expression)
                }
            // todo: RationalExponentProduct
                else -> newExpressions.add(expression)
            }
        }

        val rational = makeRational(numerator, denominator)
        if (rational != Expressions.zero) newExpressions.add(rational)

        if (newExpressions.isEmpty()) return Expressions.zero
        if (newExpressions.size == 1) return newExpressions[0]
        return AdditionExpression(newExpressions)
    }

    private fun multiply(expressions: List<Expression>): Expression {
        val newExpressions = arrayListOf<Expression>()
        val newVectors = arrayListOf<Vector>()

        // combine rationals
        var numerator = BigInteger.ONE
        var denominator = BigInteger.ONE // always > 0
        for (expression in expressions) {
            when (expression) {
                is IntegerExpression -> numerator *= expression.value
                is Rational -> {
                    numerator *= expression.numerator.value
                    denominator *= expression.denominator.value
                    // ensure denominator > 0
                    when (denominator.signum()) {
                        0 -> {
                            numerator = BigInteger.ONE
                            denominator = BigInteger.ONE
                        }
                        -1 -> {
                            numerator = -numerator
                            denominator = denominator.abs()
                        }
                    }
                }
                is Vector -> newVectors.add(expression)
            // todo: RationalExponentProduct
                else -> newExpressions.add(expression)
            }
        }

        val rational = makeRational(numerator, denominator)
        if (rational == Expressions.zero) return Expressions.zero

        // merge rational and vectors
        if (newVectors.isEmpty()) {
            // no vectors - just append the rational
            if (rational != Expressions.one) {
                newExpressions.add(rational)
            }
        } else {
            // at least one vector - merge the rational into the vector
            newExpressions.addAll(newVectors.map { it.map { row -> multiply(listOf(row, rational)) } })
        }

        if (newExpressions.isEmpty()) return Expressions.one
        if (newExpressions.size == 1) return newExpressions[0]
        return MultiplicationExpression(newExpressions)
    }

    private fun makeRational(numerator: BigInteger, denominator: BigInteger): RealNumberExpression {
        assert(denominator.signum() == 1)
        if (numerator == BigInteger.ZERO) return Expressions.zero

        val gcd = numerator.gcd(denominator)
        if (gcd == denominator) {
            return Expressions.int(numerator / gcd)
        } else {
            return Expressions.rational(numerator / gcd, denominator / gcd)
        }
    }
}

