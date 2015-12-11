package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
class ReciprocalExpression(child: Expression) : UnaryExpression(child) {
    override fun toString(content: String): String {
        return "1 / ($content)"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<ReciprocalExpression>(other, { it.child == child })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(child)
    }

    protected override fun withChild(child: Expression): UnaryExpression {
        return ReciprocalExpression(child)
    }
}