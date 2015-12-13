package at.yawk.math.ui.jeuclid

import at.yawk.math.algorithm.RealExpressionField
import at.yawk.math.parser.ExpressionParser
import org.testng.annotations.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * @author yawkat
 */
class ExpressionRendererTest {
    @Test
    fun test() {
        val parser = ExpressionParser()
        parser.addDefaultFunctions()
        val image = ExpressionRenderer.render(RealExpressionField.simplify(parser.parse("5^1*2^(1/2)+3")))
        ImageIO.write(image, "PNG", File("test.png"))
    }
}