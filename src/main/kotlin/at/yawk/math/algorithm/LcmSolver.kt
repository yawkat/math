package at.yawk.math.algorithm

import at.yawk.math.data.Expression
import at.yawk.math.data.Expressions
import at.yawk.math.data.IntegerExpression
import at.yawk.math.data.Sign

/**
 * @author yawkat
 */
object LcmSolver {
    fun lcm(a: IntegerExpression, b: IntegerExpression): Expression {
        if (a.sign != Sign.POSITIVE) {
            throw IllegalArgumentException("$a is not positive")
        }
        if (b.sign != Sign.POSITIVE) {
            throw IllegalArgumentException("$b is not positive")
        }
        return Expressions.int(a.value.multiply(b.value).divide(a.value.gcd(b.value)))
    }
}