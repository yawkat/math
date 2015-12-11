package at.yawk.math.data;

/**
 * @author yawkat
 */
public interface RealNumberExpression extends NumberExpression {
    boolean isPositive();

    boolean isNegative();

    RealNumberExpression abs();
}
