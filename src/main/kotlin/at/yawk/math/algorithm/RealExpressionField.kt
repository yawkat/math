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
                    is ReciprocalExpression -> {
                        val child = expression.child
                        if (child is RealNumberExpression) {
                            return Rational(Expressions.one, child)
                        } else {
                            return expression
                        }
                    }
                    is AdditionExpression -> return add(expression.left, expression.right)
                    is MultiplicationExpression -> return multiply(expression.left, expression.right)
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

internal class Rational(numerator: RealNumberExpression, denominator: RealNumberExpression)
: MultiplicationExpression(numerator, Expressions.reciprocal(denominator)), RealNumberExpression {
    val numerator: RealNumberExpression
        get() = left as RealNumberExpression
    val denominator: RealNumberExpression
        get() = (right as ReciprocalExpression).child as RealNumberExpression

    override val zero: Boolean
        get() {
            return numerator.zero
        }

    override val positive: Boolean
        get() = if (numerator.positive) {
            denominator.positive
        } else {
            !zero && denominator.negative
        }

    override val negative: Boolean
        get() = if (numerator.positive) {
            denominator.negative
        } else {
            !zero && denominator.positive
        }

    override val abs: RealNumberExpression
        get() = Rational(numerator.abs, denominator.abs)

    override fun toString(radix: Int): String {
        return "($numerator) / ($denominator)"
    }
}