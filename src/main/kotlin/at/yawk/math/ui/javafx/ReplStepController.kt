package at.yawk.math.ui.javafx

import at.yawk.math.algorithm.EvaluatingRealSimplificationEngine
import at.yawk.math.data.AlgorithmExpression
import at.yawk.math.parser.ExpressionParser
import at.yawk.math.ui.jeuclid.ExpressionRenderer
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.image.ImageView

/**
 * @author yawkat
 */
class ReplStepController {
    @FXML var expressionField: TextField? = null
    @FXML var resultLabel: ImageView? = null
    var parser: ExpressionParser? = null

    @FXML
    fun initialize() {
        expressionField!!.textProperty().addListener { v -> eval() }
    }

    fun eval() {
        try {
            if (expressionField!!.text.trim() == "") {
                resultLabel!!.styleClass.remove("error")
                resultLabel!!.image = null
            } else {
                val expression = parser!!.parse(expressionField!!.text)
                val simplified = if (expression is AlgorithmExpression) expression.evaluate() else expression
                resultLabel!!.styleClass.remove("error")
                resultLabel!!.image = SwingFXUtils.toFXImage(ExpressionRenderer.render(simplified), null)
            }
        } catch(e: Exception) {
            e.printStackTrace()
            resultLabel!!.styleClass.add("error")
            resultLabel!!.image = null // todo
        }
    }
}