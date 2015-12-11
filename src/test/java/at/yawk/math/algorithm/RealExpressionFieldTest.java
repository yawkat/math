package at.yawk.math.algorithm;

import at.yawk.math.data.Expression;
import org.testng.annotations.Test;

import static at.yawk.math.data.Expressions.add;
import static at.yawk.math.data.Expressions.divide;
import static at.yawk.math.data.Expressions.integer;
import static at.yawk.math.data.Expressions.multiply;
import static at.yawk.math.data.Expressions.rational;
import static at.yawk.math.data.Expressions.subtract;
import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class RealExpressionFieldTest {
    @Test
    public void testSimplify() throws Exception {
        assertEquals(
                simplify(add(integer(1), integer(2))),
                integer(3)
        );
        assertEquals(
                simplify(add(rational(1, 6), rational(3, 4))),
                rational(11, 12)
        );
        assertEquals(
                simplify(add(integer(5), rational(3, 4))),
                rational(23, 4)
        );
        assertEquals(
                simplify(subtract(integer(1), integer(2))),
                integer(-1)
        );
        assertEquals(
                simplify(multiply(integer(3), integer(4))),
                integer(12)
        );
        assertEquals(
                simplify(multiply(rational(1, 2), rational(4, 5))),
                rational(2, 5)
        );
        assertEquals(
                simplify(divide(rational(1, 2), rational(4, 5))),
                rational(5, 8)
        );
        assertEquals(
                simplify(divide(rational(1, 2), integer(5))),
                rational(1, 10)
        );
    }

    private Expression simplify(Expression expression) {
        return RealExpressionField.getInstance().simplify(expression);
    }
}