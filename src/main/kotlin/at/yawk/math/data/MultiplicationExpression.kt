package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
open class MultiplicationExpression(components: List<Expression>) : ChainExpression(components) {
    override fun withComponents(components: List<Expression>): ChainExpression {
        return MultiplicationExpression(components)
    }

    override fun toString(radix: Int): String {
        return components.map { it.toString(radix) }.joinToString(" * ")
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<MultiplicationExpression>(other, { it.components == components })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(components)
    }
}