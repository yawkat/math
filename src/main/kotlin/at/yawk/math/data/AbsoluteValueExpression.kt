package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
class AbsoluteValueExpression(child: Expression) : UnaryExpression(child) {
    override fun toString(content: String): String {
        return "|$content|"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<AbsoluteValueExpression>(other, { it.child == child })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(child)
    }

    protected override fun withChild(child: Expression): UnaryExpression {
        return AbsoluteValueExpression(child)
    }
}