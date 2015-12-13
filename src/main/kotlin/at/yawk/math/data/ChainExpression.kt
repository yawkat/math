package at.yawk.math.data

/**
 * @author yawkat
 */
abstract class ChainExpression(open val components: List<Expression>) : BaseExpression() {
    protected abstract fun withComponents(components: List<Expression>): ChainExpression

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitComposite(visitor, this, {
            val newComponents = visitList(components, visitor)
            if (newComponents == null) this else withComponents(newComponents)
        })
    }

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}