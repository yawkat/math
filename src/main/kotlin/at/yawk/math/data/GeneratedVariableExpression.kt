package at.yawk.math.data

import at.yawk.math.EqualsHelper
import java.util.*

/**
 * @author yawkat
 */
class GeneratedVariableExpression : BaseExpression() {
    private val id = UUID.randomUUID()

    override fun toString(radix: Int): String {
        return "v[$id]"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<GeneratedVariableExpression>(other, { it.id == id })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(id)
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitor.visitSingleExpression(this)
    }
}