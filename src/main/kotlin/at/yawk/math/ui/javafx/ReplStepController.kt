package at.yawk.math.ui.javafx

import at.yawk.math.algorithm.RealExpressionField
import at.yawk.math.parser.ExpressionParser
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField

/**
 * @author yawkat
 */
class ReplStepController {
    @FXML var expressionField: TextField? = null
    @FXML var resultLabel: Label? = null
    var parser: ExpressionParser? = null

    @FXML
    fun initialize() {
        expressionField!!.textProperty().addListener { v -> eval() }
    }

    fun eval() {
        try {
            if (expressionField!!.text.trim() == "") {
                resultLabel!!.styleClass.remove("error")
                resultLabel!!.text = ""
            } else {
                val expression = parser!!.parse(expressionField!!.text)
                val simplified = RealExpressionField.simplify(expression)
                resultLabel!!.styleClass.remove("error")
                resultLabel!!.text = simplified.toString(10)
            }
        } catch(e: Exception) {
            e.printStackTrace()
            resultLabel!!.styleClass.add("error")
            resultLabel!!.text = e.message
        }
    }
}