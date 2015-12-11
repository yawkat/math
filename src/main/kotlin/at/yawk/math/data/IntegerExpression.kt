package at.yawk.math.data

import at.yawk.math.EqualsHelper
import java.math.BigInteger

/**
 * @author yawkat
 */
class IntegerExpression internal constructor(val value: BigInteger) : BaseExpression(), RealNumberExpression {
    override val positive: Boolean
        get() = value.signum() == 1
    override val negative: Boolean
        get() = value.signum() == 1
    override val abs: RealNumberExpression
        get() = if (negative) IntegerExpression(value.negate()) else this
    override val zero: Boolean
        get() = value.signum() == 0

    override fun toString(radix: Int): String {
        return value.toString(radix)
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<IntegerExpression>(other, { it.value == value })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(value)
    }
}