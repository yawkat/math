package at.yawk.math.data

/**
 * Product in the form of `3^2 * 5^(1/2)`
 *
 * @author yawkat
 */
class RationalExponentProduct(val components: List<Component>) : BaseExpression(), RealNumberExpression {
    override val sign: Sign
        get() = components.fold(Sign.POSITIVE, { leftSign, component -> leftSign.multiply { component.sign } })
    override val abs: RealNumberExpression
        get() = RationalExponentProduct(components.map { it.abs })
    override val zero: Boolean
        get() = sign == Sign.ZERO

    override fun toString(radix: Int): String {
        return components.joinToString { it.toString(radix) }
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitComposite(visitor, this, {
            val newComponents = visitList(components, visitor)
            if (newComponents == null) this else RationalExponentProduct(newComponents)
        })
    }
}

class Component(override val base: RealNumberExpression, override val exponent: Rational)
: ExponentiationExpression(base, exponent), RealNumberExpression {

    override val sign: Sign
        get() = base.sign // todo: check exponent
    override val abs: Component
        get() = Component(base.abs, exponent)
    override val zero: Boolean
        get() = base.zero

    override fun visit(visitor: ExpressionVisitor): Expression {
        val newBase = base.visit(visitor)
        val newExponent = exponent.visit(visitor)
        return if (newBase === base && newExponent === exponent) this else Component(newBase as RealNumberExpression, newExponent as Rational)
    }
}