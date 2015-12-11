package at.yawk.math.data;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class Matrix implements Expression {
    private final List<Vector> columns;
    private final int height;

    @Builder
    private Matrix(@Singular List<Vector> vertices) {
        this.height = vertices.isEmpty() ? 0 : vertices.get(0).getDimension();
        for (Vector vector : vertices) {
            if (vector.getDimension() != height) {
                throw new IllegalArgumentException("Illegal dimension for vector " + vector + ", expected " + height);
            }
        }
        this.columns = vertices;
    }

    public int getWidth() {
        return columns.size();
    }

    @Override
    public String toString(int radix) {
        // ((1, 2, 3), (4, 5, 6))^T
        return columns.stream().map(v -> v.toString(radix))
                .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public String toString() {
        return toString(10);
    }
}
