package at.yawk.math.ui.jeuclid

import at.yawk.math.data.Expressions
import at.yawk.math.data.IrrationalConstant
import org.testng.Assert
import org.testng.annotations.Test

const val HEADER = "<?xml version=\"1.0\"?>\n<math>"
const val FOOTER = "</math>\n"

/**
 * @author yawkat
 */
class ExpressionRendererTest {
    @Test
    fun test() {
        Assert.assertEquals(
                ExpressionRenderer.toMathMl(Expressions.divide(Expressions.int(1), IrrationalConstant.PI)),
                HEADER + "<mrow><mn>1</mn><mo>·</mo><mrow><mfrac><mn>1</mn><mtext>pi</mtext></mfrac></mrow></mrow>" + FOOTER
        )
    }
}