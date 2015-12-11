package at.yawk.math.algorithm;

import at.yawk.math.data.Expressions;
import at.yawk.math.data.IntegerExpression;
import lombok.experimental.UtilityClass;

/**
 * @author yawkat
 */
@UtilityClass
public class GcdSolver {
    public static IntegerExpression gcd(IntegerExpression a, IntegerExpression b) {
        if (!a.isPositive()) { throw new IllegalArgumentException(a + " is not positive"); }
        if (!b.isPositive()) { throw new IllegalArgumentException(b + " is not positive"); }
        return Expressions.integer(a.getValue().gcd(b.getValue()));
    }
}
