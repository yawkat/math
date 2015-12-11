package at.yawk.math.data;

import java.util.UUID;
import lombok.EqualsAndHashCode;

/**
 * @author yawkat
 */
@EqualsAndHashCode
public class GeneratedVariableExpression implements Expression {
    private final UUID id = UUID.randomUUID();

    @Override
    public String toString(int radix) {
        return "v[" + id + ']';
    }
}
