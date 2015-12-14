package at.yawk.math.parser

import at.yawk.math.data.Expression
import at.yawk.math.data.Expressions
import org.testng.Assert
import org.testng.annotations.Test

/**
 * @author yawkat
 */
class ExpressionParserTest {
    @Test
    fun testParse() {
        Assert.assertEquals(
                ExpressionParser().parse("1 + 2"),
                Expressions.add(
                        Expressions.int(1),
                        Expressions.int(2)
                )
        )
        Assert.assertEquals(
                ExpressionParser().parse("1 + 2 * 3 / 3"),
                Expressions.add(
                        Expressions.int(1),
                        Expressions.divide(
                                Expressions.multiply(Expressions.int(2), Expressions.int(3)),
                                Expressions.int(3)
                        )
                )
        )
    }

    @Test
    fun testFunction() {
        val parser = ExpressionParser()
        parser.functions["f"] = {
            Assert.assertEquals(it.size, 1)
            Expressions.multiply(it[0], Expressions.int(2))
        }
        Assert.assertEquals(
                parser.parse("f(1)"),
                Expressions.multiply(Expressions.int(1), Expressions.int(2))
        )
    }
}