package pl.piasta.cotaltommandel

import javafx.geometry.Pos.BASELINE_RIGHT
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.multi
import tornadofx.percent
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val addClientButton by cssclass()
        val actionPane by cssclass()
        val drivePane by cssclass()
        val driveLabel by cssclass()
        val progressLabel by cssclass()
    }

    init {
        root {
            prefWidth = 800.px
            prefHeight = 600.px
        }
        treeView {
            minHeight = 80.px
        }
        scrollPane {
            minWidth = 300.px
            fitToWidth = true
            fitToHeight = true
            hBarPolicy = NEVER
        }
        addClientButton {
            minWidth = 50.px
            minHeight = 50.px
            backgroundRadius = multi(box(50.percent))
        }
        actionPane {
            padding = box(5.px)
            spacing = 5.px
        }
        drivePane {
            padding = box(0.px, 0.px, 0.99.px, 0.px)
        }
        driveLabel {
            fontSize = 14.px
            padding = box(5.px)
        }
        progressLabel {
            alignment = BASELINE_RIGHT
            minWidth = 30.px
        }
    }
}
