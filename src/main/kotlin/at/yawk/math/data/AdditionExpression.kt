package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
class AdditionExpression(components: List<Expression>) : ChainExpression(components) {
    override fun withComponents(components: List<Expression>): ChainExpression {
        return AdditionExpression(components)
    }

    override fun toString(radix: Int): String {
        return components.map { it.toString(radix) }.joinToString(" + ")
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<AdditionExpression>(other, { it.components == components })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(components)
    }
}