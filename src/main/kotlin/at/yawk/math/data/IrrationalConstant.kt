package at.yawk.math.data

/**
 * @author yawkat
 */
enum class IrrationalConstant(val constantName: String, val approximation: Double): RealNumberExpression {
    PI("pi", Math.PI),
    E("e", Math.E);

    override val sign: Sign
        get() = Sign.POSITIVE
    override val abs: RealNumberExpression
        get() = this
    override val zero: Boolean
        get() = false

    override fun toString(radix: Int): String {
        return constantName
    }

    override fun toString(): String {
        return toString(DEFAULT_RADIX)
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitor.visitSingleExpression(this)
    }
}