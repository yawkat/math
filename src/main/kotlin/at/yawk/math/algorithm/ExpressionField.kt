package at.yawk.math.algorithm

import at.yawk.math.data.Expression
import at.yawk.math.data.Vector

/**
 * @author yawkat
 */
interface ExpressionField {
    fun simplify(vector: Vector): Vector
    fun simplify(expression: Expression): Expression
}