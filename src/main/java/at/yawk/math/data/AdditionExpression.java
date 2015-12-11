package at.yawk.math.data;

/**
 * @author yawkat
 */
public class AdditionExpression extends BinaryExpression {
    AdditionExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String toString(String lhs, String rhs) {
        return lhs + " + " + rhs;
    }
}
