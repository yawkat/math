package at.yawk.math.algorithm

import at.yawk.math.data.*
import java.math.BigInteger

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
                        if (expression.exponent == Expressions.minusOne &&
                                expression.base is RealNumberExpression) {
                            return Rational(Expressions.one, expression.base as RealNumberExpression)
                        } else if (expression.exponent == Expressions.one) {
                            return expression.base
                        } else {
                            return expression
                        }
                    }
                    is AdditionExpression -> return add(expression.left, expression.right)
                    is MultiplicationExpression -> return multiply(expression.left, expression.right)
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
                                    sum = multiply(lhs, rhs)
                                } else {
                                    sum = add(sum!!, multiply(lhs, rhs))
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

    private fun add(left: Expression, right: Expression): Expression {
        if (left is IntegerExpression) {
            if (right is IntegerExpression) {
                return Expressions.int(left.value.add(right.value))
            }
            if (right is Rational) {
                return divideNumber(
                        add(
                                multiply(left, right.denominator),
                                right.numerator
                        ) as RealNumberExpression,
                        right.denominator)
            }
        }
        if (left is Rational) {
            if (right is IntegerExpression) {
                return add(right, left)
            }
            if (right is Rational) {
                // a/b + c/d = (a*d + c*b)/(b*d)
                return divideNumber(
                        add(
                                multiply(left.numerator, right.denominator),
                                multiply(right.numerator, left.denominator)) as RealNumberExpression,
                        multiply(left.denominator, right.denominator) as RealNumberExpression)
            }
        }
        if (left is Vector) {
            if (right is Vector) {
                return left.mapIndexed { i, lhs -> add(lhs, right.rows[i]) }
            }
        }
        return Expressions.add(left, right)
    }

    private fun multiply(left: Expression, right: Expression): Expression {
        if (left is IntegerExpression) {
            if (right is IntegerExpression) {
                return Expressions.int(left.value.multiply(right.value))
            }
            if (right is Rational) {
                return divideNumber(
                        multiply(left, right.numerator) as RealNumberExpression,
                        right.denominator)
            }
        }
        if (left is Rational) {
            if (right is IntegerExpression) {
                return multiply(right, left)
            }
            if (right is Rational) {
                return divideNumber(
                        multiply(left.numerator, right.numerator) as RealNumberExpression,
                        multiply(left.denominator, right.denominator) as RealNumberExpression)
            }
        }
        if (left is Vector) {
            return left.mapIndexed { i, lhs -> multiply(lhs, right) }
        }
        return Expressions.multiply(left, right)
    }

    private fun divideNumber(numerator: RealNumberExpression, denominator: RealNumberExpression): RealNumberExpression {
        var newNumerator = numerator
        var newDenominator = denominator
        // optimizations
        while (true) {
            if (newNumerator is Rational) {
                // (a/b)/c = a/(b*c)
                val oldNumerator = newNumerator
                newNumerator = oldNumerator.numerator
                newDenominator = multiply(oldNumerator.denominator, newDenominator) as RealNumberExpression
                continue
            }
            if (newDenominator is Rational) {
                // a/(b/c) = (a*c)/b
                val oldDenominator = newDenominator
                newDenominator = oldDenominator.numerator
                newNumerator = multiply(newNumerator, oldDenominator.denominator) as RealNumberExpression
                continue
            }
            if (newNumerator is IntegerExpression && newDenominator is IntegerExpression) {
                val gcd = newNumerator.value.gcd(newDenominator.value)
                // check gcd > 1
                if (gcd != BigInteger.ONE && gcd.signum() != 0) {
                    newNumerator = Expressions.int(newNumerator.value.divide(gcd))
                    newDenominator = Expressions.int(newDenominator.value.divide(gcd))
                    continue
                }
            }
            break
        }
        // a / 1 = a
        if (newDenominator is IntegerExpression && newDenominator.value.equals(BigInteger.ONE)) {
            return newNumerator
        }
        return Rational(newNumerator, newDenominator)
    }
}

