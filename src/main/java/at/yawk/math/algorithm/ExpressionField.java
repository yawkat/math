package at.yawk.math.algorithm;

import at.yawk.math.data.Expression;
import at.yawk.math.data.Vector;

/**
 * @author yawkat
 */
public interface ExpressionField {
    Vector simplify(Vector vector);

    Expression simplify(Expression expression);
}
