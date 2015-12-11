package at.yawk.math.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class Inequation implements Condition {
    private final Expression lhs;
    private final Equality equality;
    private final Expression rhs;

    @Override
    public String toString(int radix) {
        return lhs.toString(radix) + equality.getSymbol() + rhs.toString(radix);
    }

    @RequiredArgsConstructor
    @Getter
    public enum Equality {
        EQUAL("="),
        NOT_EQUAL("!="),
        LESS_THAN("<"),
        GREATER_THAN(">"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN_OR_EQUAL(">=");

        private final String symbol;
    }
}
