package at.yawk.math.data;

/**
 * @author yawkat
 */
public class ReciprocalExpression extends UnaryExpression {
    ReciprocalExpression(Expression child) {
        super(child);
    }

    @Override
    protected String toString(String content) {
        return "1 / (" + content + ')';
    }
}
