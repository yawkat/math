package at.yawk.math.algorithm

import at.yawk.math.data.Expression

/**
 * @author yawkat
 */
interface ExpressionField {
    fun simplify(expression: Expression): Expression
}