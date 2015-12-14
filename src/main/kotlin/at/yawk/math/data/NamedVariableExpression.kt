package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * Expression that represents an undefined variable with a name.
 *
 * @author yawkat
 */
class NamedVariableExpression(val name: String) : BaseExpression() {
    override fun toString(radix: Int): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<NamedVariableExpression>(other, { it.name == name })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(name)
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitor.visitSingleExpression(this)
    }
}