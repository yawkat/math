package at.yawk.math.data

/**
 * Product in the form of `3^2 * 5^(1/2)`.
 *
 * @author yawkat
 */
class RationalExponentiationProduct(override val components: List<RationalExponentiation>)
: MultiplicationExpression(components), RealNumberExpression {

    override val sign: Sign
        get() = components.fold(Sign.POSITIVE, { leftSign, component -> leftSign.multiply { component.sign } })
    override val abs: RealNumberExpression
        get() = RationalExponentiationProduct(components.map { it.abs })
    override val zero: Boolean
        get() = sign == Sign.ZERO
    override val negate: RationalExponentiationProduct
        get() = RationalExponentiationProduct(listOf(
                RationalExponentiation(Expressions.minusOne, Expressions.oneRational), *components.toTypedArray()
        ))
    override val reciprocal: RationalExponentiationProduct
        get() = RationalExponentiationProduct(components.map { it.reciprocal })

    override fun toString(radix: Int): String {
        return components.joinToString(separator = "*") { it.toString(radix) }
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitComposite(visitor, this, {
            val newComponents = visitList(components, visitor)
            if (newComponents == null) this else RationalExponentiationProduct(newComponents)
        })
    }
}

class RationalExponentiation(override val base: RealNumberExpression, override val exponent: Rational)
: ExponentiationExpression(base, exponent), RealNumberExpression {

    override val sign: Sign
        get() {
            val baseSign = base.sign
            if (baseSign != Sign.NEGATIVE) return baseSign
            if (exponent.denominator.abs == Expressions.one) {
                if (exponent.numerator.even) return Sign.POSITIVE else return Sign.NEGATIVE
            } else {
                // we have a root somewhere, just assume the sign is positive because otherwise the root would be invalid over the reals!
                // todo: what about complexes?
                return Sign.POSITIVE
            }
        }
    override val abs: RationalExponentiation
        get() = RationalExponentiation(base.abs, exponent)
    override val zero: Boolean
        get() = base.zero
    override val negate: RationalExponentiationProduct
        get() = RationalExponentiationProduct(listOf(RationalExponentiation(Expressions.minusOne, Expressions.oneRational), this))
    override val reciprocal: RationalExponentiation
        get() = RationalExponentiation(base, exponent.negate)

    override fun toString(lhs: String, rhs: String): String {
        // 4^1 -> 4
        if (exponent == Expressions.oneRational) {
            return lhs
        } else if (exponent.denominator == Expressions.one) {
            return "$lhs^${exponent.numerator}"
        } else {
            return "$lhs^$rhs"
        }
    }

    override fun visit(visitor: ExpressionVisitor): Expression {
        val newBase = base.visit(visitor)
        val newExponent = exponent.visit(visitor)
        return if (newBase === base && newExponent === exponent) this else RationalExponentiation(newBase as RealNumberExpression, newExponent as Rational)
    }
}