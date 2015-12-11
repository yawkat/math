package at.yawk.math.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class BinaryExpression implements Expression {
    private final Expression left;
    private final Expression right;

    protected abstract String toString(String lhs, String rhs);

    @Override
    public String toString(int radix) {
        return toString(left.toString(radix), right.toString(radix));
    }

    @Override
    public String toString() {
        return toString(10);
    }
}
