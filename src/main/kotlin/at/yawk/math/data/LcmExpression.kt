package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
class LcmExpression(left: Expression, right: Expression) : BinaryExpression(left, right) {
    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<LcmExpression>(other, { it.left == left && it.right == right })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(left, right)
    }

    override fun withChildren(left: Expression, right: Expression): BinaryExpression {
        return LcmExpression(left, right)
    }

    override fun toString(lhs: String, rhs: String): String {
        return "lcm($lhs, $rhs)"
    }
}