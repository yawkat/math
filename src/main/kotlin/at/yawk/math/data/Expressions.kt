package at.yawk.math.data

import java.math.BigInteger

/**
 * @author yawkat
 */
object Expressions {
    val zero = int(0)
    val one = int(1)
    val minusOne = int(-1)

    fun add(vararg a: Expression): Expression {
        return AdditionExpression(listOf(*a))
    }

    fun multiply(vararg a: Expression): Expression {
        return MultiplicationExpression(listOf(*a))
    }

    fun subtract(minuend: Expression, subtrahend: Expression): Expression {
        return add(minuend, negate(subtrahend))
    }

    fun divide(dividend: Expression, divisor: Expression): Expression {
        return multiply(dividend, reciprocal(divisor))
    }

    fun negate(a: Expression): Expression {
        if (a is RealNumberExpression) return a.negate
        return multiply(minusOne, a)
    }

    fun reciprocal(a: Expression): Expression {
        if (a is RealNumberExpression) return a.reciprocal
        return ExponentiationExpression(a, minusOne)
    }

    fun vector(rows: List<Expression>): Expression {
        return Vector(rows)
    }

    fun int(value: BigInteger): IntegerExpression {
        return IntegerExpression(value)
    }

    fun int(value: Long): IntegerExpression {
        return int(BigInteger.valueOf(value))
    }

    fun rational(numerator: Long, denominator: Long): Rational {
        return SimpleRational(int(numerator), int(denominator))
    }

    fun rational(numerator: BigInteger, denominator: BigInteger): Rational {
        return SimpleRational(int(numerator), int(denominator))
    }

    fun abs(expression: Expression): Expression {
        return AbsoluteValueExpression(expression)
    }

    fun gcd(a: Expression, b: Expression): Expression {
        return GcdExpression(a, b)
    }

    fun lcm(a: Expression, b: Expression): Expression {
        return LcmExpression(a, b)
    }

    fun dotProduct(a: Expression, b: Expression): Expression {
        return DotProductExpression(a, b)
    }

    fun pow(base: Expression, exponent: Expression): Expression {
        return ExponentiationExpression(base, exponent)
    }
}