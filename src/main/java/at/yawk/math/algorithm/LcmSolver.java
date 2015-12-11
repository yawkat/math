package at.yawk.math.algorithm;

import at.yawk.math.data.Expressions;
import at.yawk.math.data.IntegerExpression;
import java.math.BigInteger;
import lombok.experimental.UtilityClass;

/**
 * @author yawkat
 */
@UtilityClass
public class LcmSolver {
    public static IntegerExpression lcm(IntegerExpression a, IntegerExpression b) {
        if (!a.isPositive()) { throw new IllegalArgumentException(a + " is not positive"); }
        if (!b.isPositive()) { throw new IllegalArgumentException(b + " is not positive"); }
        BigInteger x = a.getValue();
        BigInteger y = b.getValue();
        return Expressions.integer(x.multiply(y).divide(x.gcd(y)));
    }
}
