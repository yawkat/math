package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * Expression that represents an undefined function with a name and parameters.
 *
 * @author yawkat
 */
class NamedFunctionExpression(val name: String, components: List<Expression>) : ChainExpression(components) {
    override fun withComponents(components: List<Expression>): ChainExpression {
        return NamedFunctionExpression(name, components)
    }

    override fun toString(radix: Int): String {
        return components.joinToString(separator = ", ", prefix = "$name(", postfix = ")", transform = { it.toString(radix) })
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