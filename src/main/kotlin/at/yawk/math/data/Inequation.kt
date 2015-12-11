package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
class Inequation(left: Expression, val equality: Equality, right: Expression) : BinaryExpression(left, right), Condition {
    override fun toString(lhs: String, rhs: String): String {
        return "$lhs ${equality.symbol} $rhs"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<Inequation>(other, { it.left == left && it.equality == equality && it.right == right })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(left, equality, right)
    }

    protected override fun withChildren(left: Expression, right: Expression): BinaryExpression {
        return Inequation(left, equality, right)
    }
}
