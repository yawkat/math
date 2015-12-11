package at.yawk.math.data

/**
 * @author yawkat
 */
interface ExpressionVisitor {
    fun visitSingleExpression(expression: Expression): Expression

    fun preEnterExpression(expression: Expression): EntranceMode {
        return EntranceMode.VISIT
    }

    fun postEnterExpression(expression: Expression): Expression
}

enum class EntranceMode {
    SKIP,
    VISIT,
}