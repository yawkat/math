package at.yawk.math.data;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

/**
 * @author yawkat
 */
@UtilityClass
public class Expressions {
    public static Expression add(Expression lhs, Expression rhs) {
        return new AdditionExpression(lhs, rhs);
    }

    public static Expression multiply(Expression lhs, Expression rhs) {
        return new MultiplicationExpression(lhs, rhs);
    }

    public static Expression subtract(Expression lhs, Expression rhs) {
        return add(lhs, negate(rhs));
    }

    public static Expression divide(Expression lhs, Expression rhs) {
        return multiply(lhs, reciprocal(rhs));
    }

    public static Expression negate(Expression expression) {
        return multiply(integer(-1), expression);
    }

    public static Expression reciprocal(Expression expression) {
        return new ReciprocalExpression(expression);
    }

    public static Vector vector(List<Expression> expressions) {
        return Vector.builder()
                .expressions(expressions)
                .build();
    }

    public static Vector vector(Expression... expressions) {
        return vector(Arrays.asList(expressions));
    }

    public static IntegerExpression integer(long value) {
        return new IntegerExpression(value);
    }

    public static IntegerExpression integer(BigInteger value) {
        return new IntegerExpression(value);
    }

    public static Expression rational(long numerator, long denominator) {
        return divide(integer(numerator), integer(denominator));
    }

    public static Expression abs(Expression expression) {
        return new AbsoluteValueExpression(expression);
    }
}
