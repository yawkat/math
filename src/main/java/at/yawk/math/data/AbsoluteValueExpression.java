package at.yawk.math.data;

/**
 * @author yawkat
 */
public class AbsoluteValueExpression extends UnaryExpression {
    AbsoluteValueExpression(Expression child) {
        super(child);
    }

    @Override
    protected String toString(String content) {
        return '|' + content + '|';
    }
}
