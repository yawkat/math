package at.yawk.math.data

/**
 * @author yawkat
 */
abstract class UnaryExpression(val child: Expression) : BaseExpression() {
    protected abstract fun toString(content: String): String

    override fun toString(radix: Int): String {
        return toString(child.toString(radix))
    }

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}