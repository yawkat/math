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
    override val negate: RationalExponentiationProduct
        get() = RationalExponentiationProduct(listOf(
                RationalExponentiation(this, Expressions.rational(1, 1)),
                RationalExponentiation(Expressions.minusOne, Expressions.rational(1, 1))
        ))
    override val reciprocal: RationalExponentiationProduct
        get() = RationalExponentiationProduct(listOf(RationalExponentiation(this, Expressions.rational(-1, 1))))

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