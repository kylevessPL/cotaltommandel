package pl.piasta.cotaltommandel

import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.multi
import tornadofx.percent
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val clientPane by cssclass()
        val drivePane by cssclass()
        val treePane by cssclass()
        val actionPane by cssclass()
        val driveLabel by cssclass()
    }

    init {
        root {
            prefWidth = 600.px
            prefHeight = 600.px
        }
        button {
            minWidth = 50.px
            minHeight = 50.px
            endMargin = 20.px
            backgroundRadius = multi(box(50.percent))
        }
        scrollPane {
            minWidth = 200.px
            fitToWidth = true
            fitToHeight = true
            hBarPolicy = NEVER
        }
        clientPane {
            padding = box(0.px, 0.px, 0.49.px, 0.px)
        }
        drivePane {
            padding = box(0.px, 0.px, 0.99.px, 0.px)
        }
        treePane {
            minHeight = 80.px
        }
        actionPane {
            padding = box(5.px)
            spacing = 5.px
        }
        driveLabel {
            fontSize = 14.px
            padding = box(5.px)
        }
    }
}
