package at.yawk.math.ui.javafx

import at.yawk.math.parser.ExpressionParser
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.VBox

/**
 * @author yawkat
 */
class ReplController {
    @FXML var steps: VBox? = null

    @FXML
    fun initialize() {
        val firstParser = ExpressionParser()
        firstParser.addDefaultFunctions()
        addStep(firstParser)
    }

    fun addStep(parser: ExpressionParser): ReplStepController {
        val loader = FXMLLoader(ReplApplication::class.java.getResource("repl_step.fxml"))
        val step = loader.load<Node>()
        steps!!.children.add(step)
        val controller = loader.getController<ReplStepController>()
        controller.parser = parser
        controller.eval()
        return controller
    }
}