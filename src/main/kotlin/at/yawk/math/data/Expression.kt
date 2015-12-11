package at.yawk.math.data

const val DEFAULT_RADIX = 10

/**
 * @author yawkat
 */
interface Expression {
    fun toString(radix: Int): String
}

abstract class BaseExpression : Expression {
    override fun toString(): String {
        return toString(DEFAULT_RADIX)
    }
}