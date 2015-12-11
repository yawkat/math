package at.yawk.math.data

const val DEFAULT_RADIX = 10

internal fun <E : Expression> visitList(expressions: List<E>, visitor: ExpressionVisitor): List<E>? {
    val newExpressions = arrayListOf<E>()
    var anyChanged = false
    for (column in expressions) {
        val newColumn = column.visit(visitor)
        anyChanged = anyChanged || newColumn !== column
        @Suppress("UNCHECKED_CAST")
        newExpressions.add(newColumn as E)
    }
    return if (anyChanged) newExpressions else null
}

internal fun <E : Expression> visitComposite(visitor: ExpressionVisitor, composite: E, visitFunction: () -> E): E {
    if (visitor.preEnterExpression(composite) == EntranceMode.VISIT) {
        @Suppress("UNCHECKED_CAST")
        return visitor.postEnterExpression(visitFunction.invoke()) as E
    } else {
        return composite
    }
}

/**
 * @author yawkat
 */
interface Expression {
    fun toString(radix: Int): String

    fun visit(visitor: ExpressionVisitor): Expression
}

abstract class BaseExpression : Expression {
    override fun toString(): String {
        return toString(DEFAULT_RADIX)
    }
}