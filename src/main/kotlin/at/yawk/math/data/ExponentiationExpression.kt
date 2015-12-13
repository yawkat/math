package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
open class ExponentiationExpression(open val base: Expression, open val exponent: Expression) : BinaryExpression(base, exponent) {
    override fun toString(lhs: String, rhs: String): String {
        return "($lhs^$rhs)"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<ExponentiationExpression>(other, { it.base == base && it.exponent == exponent })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(base, exponent)
    }

    override fun withChildren(left: Expression, right: Expression): BinaryExpression {
        return ExponentiationExpression(left, right)
    }
}