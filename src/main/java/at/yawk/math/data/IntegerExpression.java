package at.yawk.math.data;

import java.math.BigInteger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class IntegerExpression implements RealNumberExpression {
    private final BigInteger value;

    IntegerExpression(long value) {
        this(BigInteger.valueOf(value));
    }

    @Override
    public String toString() {
        return toString(10);
    }

    @Override
    public String toString(int radix) {
        return value.toString(radix);
    }

    @Override
    public boolean isZero() {
        return value.signum() == 0;
    }

    @Override
    public boolean isPositive() {
        return value.signum() == 1;
    }

    @Override
    public boolean isNegative() {
        return value.signum() == -1;
    }

    @Override
    public IntegerExpression abs() {
        return isNegative() ? new IntegerExpression(value.abs()) : this;
    }
}
