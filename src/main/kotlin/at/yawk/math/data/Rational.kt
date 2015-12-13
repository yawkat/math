package at.yawk.math.data

import at.yawk.math.EqualsHelper
import kotlin.math.times

internal fun rationalEquals(a: Rational, b: Any?): Boolean {
    return b is Rational &&
            a.numerator.value == b.numerator.value && a.denominator.value == b.denominator.value
}

internal fun rationalHashcode(rational: Rational): Int {
    return EqualsHelper.hashCode(rational.numerator.value, rational.denominator.value)
}

interface Rational : RealNumberExpression, Comparable<Rational> {
    val numerator: IntegerExpression
    val denominator: IntegerExpression
    override val abs: Rational
    override val negate: Rational
    override val reciprocal: Rational

    // note: equals and hashcode must compare numerator and denominator and work for all rationals!
}

class SimpleRational(override val numerator: IntegerExpression, override val denominator: IntegerExpression)
: BinaryExpression(numerator, denominator), Rational {

    override val negate: Rational
        get() = SimpleRational(numerator.negate, denominator)
    override val reciprocal: Rational
        get() = SimpleRational(denominator, numerator)
    override val zero: Boolean
        get() = numerator.zero
    override val sign: Sign
        get() = numerator.sign

    override val abs: Rational
        get() = SimpleRational(numerator.abs, denominator.abs)

    override fun toString(lhs: String, rhs: String): String {
        return "($lhs/$rhs)"
    }

    override fun equals(other: Any?): Boolean {
        return rationalEquals(this, other)
    }

    override fun hashCode(): Int {
        return rationalHashcode(this)
    }

    override fun withChildren(left: Expression, right: Expression): BinaryExpression {
        return SimpleRational(left as IntegerExpression, right as IntegerExpression)
    }

    operator override fun compareTo(other: Rational): Int {
        return (this.numerator.value * other.denominator.value).compareTo(other.numerator.value * this.denominator.value)
    }
}