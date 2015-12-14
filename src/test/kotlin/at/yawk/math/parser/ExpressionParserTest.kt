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
                ExpressionParser(EmptyParserContext).parse("1 + 2"),
                Expressions.add(
                        Expressions.int(1),
                        Expressions.int(2)
                )
        )
        Assert.assertEquals(
                ExpressionParser(EmptyParserContext).parse("1 + 2 * 3 / 3"),
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
        val parser = ExpressionParser(object : ParserContext {
            override fun getVariable(name: String): Expression? = throw UnsupportedOperationException()

            override fun getFunction(name: String, parameters: List<Expression>): Expression? {
                Assert.assertEquals(name, "f")
                Assert.assertEquals(parameters.size, 1)
                return Expressions.multiply(parameters[0], Expressions.int(2))
            }
        })
        Assert.assertEquals(
                parser.parse("f(1)"),
                Expressions.multiply(Expressions.int(1), Expressions.int(2))
        )
    }
}