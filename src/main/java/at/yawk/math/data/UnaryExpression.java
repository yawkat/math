package at.yawk.math.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public abstract class UnaryExpression implements Expression {
    private final Expression child;

    protected abstract String toString(String content);

    @Override
    public String toString(int radix) {
        return toString(child.toString(radix));
    }

    @Override
    public String toString() {
        return toString(10);
    }
}
