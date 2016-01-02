package at.yawk.math.ui.javafx

import at.yawk.math.data.AlgorithmExpression
import at.yawk.math.data.Expression
import at.yawk.math.parser.ChainedParserContext
import at.yawk.math.parser.EmptyParserContext
import at.yawk.math.parser.ExpressionParser
import at.yawk.math.parser.ParserContext
import at.yawk.math.ui.jeuclid.ExpressionRenderer
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.image.ImageView

/**
 * @author yawkat
 */
class ReplStepController {
    @FXML lateinit var expressionField: TextField
    @FXML lateinit var resultLabel: ImageView

    lateinit var parent: ReplController

    var next: ReplStepController? = null
    var prev: ReplStepController? = null
    var context: ParserContext = EmptyParserContext

    fun init() {
        expressionField.textProperty().addListener { v -> eval() }
    }

    @FXML
    fun enter() {
        val n = next
        if (n == null) {
            parent.addStepAtEnd().focus()
        } else {
            eval()
            n.focus()
        }
    }

    private fun focus() {
        expressionField.requestFocus()
    }

    fun eval() {
        try {
            if (expressionField.text.trim() == "") {
                resultLabel.styleClass.remove("error")
                resultLabel.image = null
            } else {
                val parser = ExpressionParser(context)

                var expression = parser.parse(expressionField.text)
                var toEvaluate = expression
                if (toEvaluate is ExpressionParser.AssignmentExpression) {
                    toEvaluate = toEvaluate.value
                }
                val simplified = if (toEvaluate is AlgorithmExpression) toEvaluate.evaluate() else toEvaluate
                resultLabel.styleClass.remove("error")
                resultLabel.image = SwingFXUtils.toFXImage(ExpressionRenderer.render(simplified), null)

                next?.context = if (expression is ExpressionParser.AssignmentExpression) {
                    val assignment = expression
                    ChainedParserContext(
                            object : ParserContext {
                                override fun getFunction(name: String, parameters: List<Expression>): Expression? = null

                                override fun getVariable(name: String): Expression? {
                                    if (name == assignment.name) return simplified
                                    return null
                                }
                            },
                            context
                    )
                } else {
                    context
                }
                next?.eval()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            resultLabel.styleClass.add("error")
            resultLabel.image = null // todo
        }
        parent.save()
    }
}