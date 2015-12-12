package at.yawk.math.data

/**
 * @author yawkat
 */
interface RealNumberExpression : NumberExpression {
    val sign: Sign
    val abs: RealNumberExpression
}