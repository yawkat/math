package at.yawk.math.algorithm

import at.yawk.math.EqualsHelper
import at.yawk.math.data.*
import java.math.BigInteger

/**
 * @author yawkat
 */
object RealExpressionField : ExpressionField {
    override fun simplify(vector: Vector): Vector {
        return vector.map { simplify(it) }
    }

    override fun simplify(expression: Expression): Expression {
        when (expression) {
            is ReciprocalExpression -> {
                val child = simplify(expression.child)
                if (child is RealNumberExpression) {
                    return Rational(Expressions.one, child)
                } else {
                    return Expressions.reciprocal(child)
                }
            }
            is AdditionExpression -> {
                return Expressions.add(simplify(expression.left), simplify(expression.right))
            }
            is MultiplicationExpression -> {
                return Expressions.multiply(simplify(expression.left), simplify(expression.right))
            }
            else -> {
                return expression
            }
        }
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
        return Expressions.add(left, right)
    }

    private fun multiply(left: Expression, right: Expression): Expression {
        if (left is IntegerExpression) {
            if (right is IntegerExpression) {
                return Expressions.int(left.value.multiply(right.value))
            }
            if (right is Rational) {
                return divideNumber(
                        multiply(
                                left,
                                right.numerator) as RealNumberExpression,
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
        return Expressions.multiply(left, right)
    }

    private fun divideNumber(numerator: RealNumberExpression, denominator: RealNumberExpression): Rational {
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
        return Rational(newNumerator, newDenominator)
    }
}

internal class Rational(val numerator: RealNumberExpression, val denominator: RealNumberExpression)
: MultiplicationExpression(numerator, Expressions.reciprocal(denominator)), RealNumberExpression {
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

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<Rational>(other, { it.denominator == denominator && it.numerator == numerator })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(numerator, denominator)
    }
}