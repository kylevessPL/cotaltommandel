package pl.piasta.cotaltommandel

import javafx.scene.image.Image
import javafx.stage.Stage
import pl.piasta.cotaltommandel.Constants.IMAGE_ASSETS
import tornadofx.App
import tornadofx.launch
import tornadofx.setStageIcon

class CotalTommandel : App(MainView::class, Styles::class) {
    override fun start(stage: Stage) {
        setStageIcon(Image("$IMAGE_ASSETS/icon.png"))
        with(stage) {
            minWidth = 450.0
            minHeight = 400.0
            super.start(this)
        }
    }
}

fun main() = launch<CotalTommandel>()
