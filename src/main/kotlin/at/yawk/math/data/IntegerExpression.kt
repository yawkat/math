package at.yawk.math.data

import java.math.BigInteger

/**
 * @author yawkat
 */
class IntegerExpression internal constructor(val value: BigInteger) : BaseExpression(), Rational {
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
        get() = SimpleRational(Expressions.one, this)
    override val numerator: IntegerExpression
        get() = this
    override val denominator: IntegerExpression
        get() = Expressions.one

    val even: Boolean
        get() = value.and(BigInteger.ONE) == BigInteger.ZERO

    override fun toString(radix: Int): String {
        return value.toString(radix)
    }

    override fun equals(other: Any?): Boolean {
        return rationalEquals(this, other)
    }

    override fun hashCode(): Int {
        return rationalHashcode(this)
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitor.visitSingleExpression(this)
    }

    operator override fun compareTo(other: Rational): Int {
        if (other is IntegerExpression) {
            return value.compareTo(other.value)
        } else {
            return -other.compareTo(this)
        }
    }
}