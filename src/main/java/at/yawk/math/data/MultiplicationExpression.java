package at.yawk.math.data;

/**
 * @author yawkat
 */
public class MultiplicationExpression extends BinaryExpression {
    protected MultiplicationExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String toString(String lhs, String rhs) {
        return '(' + lhs + ") * (" + rhs + ')';
    }
}
