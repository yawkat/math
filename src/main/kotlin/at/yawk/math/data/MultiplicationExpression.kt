package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
open class MultiplicationExpression(left: Expression, right: Expression) : BinaryExpression(left, right) {
    override fun toString(lhs: String, rhs: String): String {
        return "$lhs * $rhs"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<MultiplicationExpression>(other, { it.left == left && it.right == right })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(left, right)
    }
}