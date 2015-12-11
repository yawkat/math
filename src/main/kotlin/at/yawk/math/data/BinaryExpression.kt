package at.yawk.math.data

/**
 * @author yawkat
 */
abstract class BinaryExpression(val left: Expression, val right: Expression) : Expression {
    protected abstract fun toString(lhs: String, rhs: String): String

    override fun toString(radix: Int): String {
        return toString(left.toString(radix), right.toString(radix))
    }

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}