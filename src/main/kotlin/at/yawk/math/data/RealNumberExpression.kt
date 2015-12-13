package at.yawk.math.data

/**
 * A *constant* real number.
 *
 * @author yawkat
 */
interface RealNumberExpression : NumberExpression {
    val sign: Sign
    val abs: RealNumberExpression
    val negate: RealNumberExpression
    val reciprocal: RealNumberExpression
}