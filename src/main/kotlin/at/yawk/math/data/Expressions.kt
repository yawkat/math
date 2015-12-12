package at.yawk.math.data

import java.math.BigInteger

/**
 * @author yawkat
 */
object Expressions {
    val zero = int(0)
    val one = int(1)
    val minusOne = int(-1)

    fun add(a: Expression, b: Expression): Expression {
        return AdditionExpression(a, b)
    }

    fun multiply(a: Expression, b: Expression): Expression {
        return MultiplicationExpression(a, b)
    }

    fun subtract(minuend: Expression, subtrahend: Expression): Expression {
        return add(minuend, negate(subtrahend))
    }

    fun divide(dividend: Expression, divisor: Expression): Expression {
        return multiply(dividend, reciprocal(divisor))
    }

    fun negate(a: Expression): Expression {
        return multiply(minusOne, a)
    }

    fun reciprocal(a: Expression): Expression {
        return ExponentiationExpression(a, minusOne)
    }

    fun vector(rows: List<Expression>): Expression {
        return Vector(rows)
    }

    fun int(value: BigInteger): RealNumberExpression {
        return IntegerExpression(value)
    }

    fun int(value: Long): RealNumberExpression {
        return int(BigInteger.valueOf(value))
    }

    fun rational(numerator: Long, denominator: Long): Expression {
        return Rational(int(numerator), int(denominator))
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
}