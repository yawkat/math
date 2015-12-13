package at.yawk.math.ui.javafx

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(ReplApplication::class.java, *args)
}

/**
 * @author yawkat
 */
class ReplApplication : Application() {
    override fun start(primaryStage: Stage) {
        val scene = Scene(FXMLLoader.load(ReplApplication::class.java.getResource("repl.fxml")))
        scene.stylesheets.add("/at/yawk/math/ui/javafx/repl.css")
        primaryStage.title = "REPL"
        primaryStage.scene = scene
        primaryStage.show()
    }
}