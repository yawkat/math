package at.yawk.math.ui.javafx

import at.yawk.math.parser.ChainedParserContext
import at.yawk.math.parser.DefaultParserContext
import at.yawk.math.parser.NamedFunctionVariableParserContext
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.VBox
import java.util.prefs.Preferences

/**
 * @author yawkat
 */
class ReplController {
    @FXML var steps: VBox? = null

    var last: ReplStepController? = null

    @FXML
    fun initialize() {
        val steps = getPreferences().node("steps")
        val stepCount = steps.getInt("stepCount", 0)
        var first: ReplStepController? = null
        for (i in stepCount - 1 downTo 0) {
            val here = addStepAtEnd(false)
            here.expressionField!!.text = steps.get("step-$i", null)
            first = first ?: here
        }
        if (first == null) {
            // no entries loaded
            addStepAtEnd()
        } else {
            var node = first
            while (node != null) {
                node.init()
                node = node.next
            }
            first.eval()
        }
    }

    fun addStepAtEnd(): ReplStepController {
        return addStepAtEnd(true)
    }

    private fun addStepAtEnd(initialize: Boolean): ReplStepController {
        val loader = FXMLLoader(ReplApplication::class.java.getResource("repl_step.fxml"))
        val step = loader.load<Node>()
        steps!!.children.add(step)

        val controller = loader.getController<ReplStepController>()
        if (initialize) controller.init()
        controller.parent = this
        controller.prev = last

        val oldLast = last
        last = controller
        if (oldLast == null) {
            controller.context = ChainedParserContext(DefaultParserContext, NamedFunctionVariableParserContext)
            if (initialize) controller.eval()
        } else {
            oldLast.next = controller
            if (initialize) oldLast.eval()
        }
        return controller
    }

    fun save() {
        val steps = getPreferences().node("steps")
        steps.clear()
        var node = last
        var i = 0
        while (node != null) {
            steps.put("step-$i", node.expressionField!!.text)
            node = node.prev
            i++
        }
        steps.putInt("stepCount", i)
        steps.flush()
    }

    private fun getPreferences() = Preferences.userNodeForPackage(ReplController::class.java)
}