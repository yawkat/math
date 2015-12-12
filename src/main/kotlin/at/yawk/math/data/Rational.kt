package at.yawk.math.data

import at.yawk.math.EqualsHelper

class Rational(val numerator: IntegerExpression, val denominator: IntegerExpression) : BinaryExpression(numerator, denominator), RealNumberExpression {
    override val zero: Boolean
        get() = numerator.zero
    override val sign: Sign
        get() = numerator.sign

    override val abs: Rational
        get() = Rational(numerator.abs, denominator.abs)

    override fun toString(lhs: String, rhs: String): String {
        return "$lhs / $rhs"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<Rational>(other, { it.numerator == numerator && it.denominator == denominator })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(numerator, denominator)
    }

    override fun withChildren(left: Expression, right: Expression): BinaryExpression {
        return Rational(left as IntegerExpression, right as IntegerExpression)
    }
}