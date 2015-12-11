package at.yawk.math.data

/**
 * @author yawkat
 */
interface RealNumberExpression : NumberExpression {
    val positive: Boolean
    val negative: Boolean
    val abs: RealNumberExpression
}