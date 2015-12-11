package at.yawk.math.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.*;

/**
 * @author yawkat
 */
@Value
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Vector implements Expression {
    @Singular private final List<Expression> expressions;

    public int getDimension() {
        return expressions.size();
    }

    @Override
    public String toString(int radix) {
        // (a, b, c)^T
        return expressions.stream().map(t -> t.toString(radix))
                .collect(Collectors.joining(", ", "(", ")^T"));
    }

    @Override
    public String toString() {
        return toString(10);
    }

    public Vector add(Vector other) {
        if (getDimension() != other.getDimension()) {
            throw new IllegalArgumentException("Incompatible dimensions");
        }
        List<Expression> expressions = new ArrayList<>(getDimension());
        for (int i = 0; i < getDimension(); i++) {
            Expression lhs = this.expressions.get(i);
            Expression rhs = other.expressions.get(i);
            expressions.add(Expressions.add(lhs, rhs));
        }
        return Vector.builder().expressions(expressions).build();
    }

    public Vector map(Function<Expression, Expression> mapFunction) {
        return builder()
                .expressions(
                        getExpressions().stream()
                                .map(mapFunction)
                                .collect(Collectors.toList())
                ).build();
    }

    public Vector multiply(Expression scalar) {
        return map(e -> Expressions.multiply(scalar, e));
    }
}
