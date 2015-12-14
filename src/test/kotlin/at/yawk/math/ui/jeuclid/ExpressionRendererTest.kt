package at.yawk.math.ui.jeuclid

import at.yawk.math.algorithm.BasicRealSimplificationEngine
import at.yawk.math.algorithm.RealSimplificationEngine
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
                ExpressionRenderer.toMathMl(BasicRealSimplificationEngine.simplify(Expressions.divide(Expressions.int(5), IrrationalConstant.PI))),
                HEADER + "<mfrac><mrow><mn>5</mn></mrow><mrow><mtext>pi</mtext></mrow></mfrac>" + FOOTER
        )
    }
}