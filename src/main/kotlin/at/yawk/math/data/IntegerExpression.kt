package at.yawk.math.data

import at.yawk.math.EqualsHelper
import java.math.BigInteger

/**
 * @author yawkat
 */
class IntegerExpression internal constructor(val value: BigInteger) : BaseExpression(), RealNumberExpression, Comparable<IntegerExpression> {
    override val sign: Sign
        get() = when (value.signum()) {
            0 -> Sign.ZERO
            1 -> Sign.POSITIVE
            -1 -> Sign.NEGATIVE
            else -> throw AssertionError()
        }
    override val negate: IntegerExpression
        get() = IntegerExpression(value.negate())
    override val abs: IntegerExpression
        get() = if (sign == Sign.NEGATIVE) IntegerExpression(value.abs()) else this
    override val zero: Boolean
        get() = value.signum() == 0
    override val reciprocal: Rational
        get() = Rational(Expressions.one, this)

    val even: Boolean
        get() = value.and(BigInteger.ONE) == BigInteger.ZERO

    override fun toString(radix: Int): String {
        return value.toString(radix)
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<IntegerExpression>(other, { it.value == value })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(value)
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitor.visitSingleExpression(this)
    }

    operator override fun compareTo(other: IntegerExpression): Int {
        return value.compareTo(other.value)
    }
}