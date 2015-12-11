package at.yawk.math.data

/**
 * @author yawkat
 */
abstract class BinaryExpression(val left: Expression, val right: Expression) : BaseExpression() {
    protected abstract fun toString(lhs: String, rhs: String): String

    override fun toString(radix: Int): String {
        return toString(left.toString(radix), right.toString(radix))
    }

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    protected abstract fun withChildren(left: Expression, right: Expression): BinaryExpression

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitComposite(visitor, this, {
            val newLeft = left.visit(visitor)
            val newRight = right.visit(visitor)
            if (newLeft === left && newRight === right) this else withChildren(newLeft, newRight)
        })
    }
}