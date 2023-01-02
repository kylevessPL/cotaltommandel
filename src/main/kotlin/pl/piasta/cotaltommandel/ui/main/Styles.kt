package pl.piasta.cotaltommandel.ui.main

import javafx.geometry.Pos.BASELINE_RIGHT
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.multi
import tornadofx.percent
import tornadofx.px

internal class Styles : Stylesheet() {
    companion object {
        val actionButton by cssclass()
        val playButton by cssclass()
        val actionPane by cssclass()
        val drivePane by cssclass()
        val queueEntry by cssclass()
        val driveLabel by cssclass()
        val progressLabel by cssclass()
        val elapsedTimeLabel by cssclass()
    }

    init {
        root {
            prefWidth = 950.px
            prefHeight = 600.px
        }
        treeView {
            minHeight = 80.px
        }
        scrollPane {
            minWidth = 422.px
            fitToWidth = true
            fitToHeight = true
            hBarPolicy = NEVER
        }
        actionButton {
            minWidth = 50.px
            minHeight = 50.px
            backgroundRadius = multi(box(50.percent))
        }
        playButton {
            padding = box(0.px, 0.px, 0.px, 5.px)
        }
        actionPane {
            padding = box(5.px)
            spacing = 5.px
        }
        drivePane {
            padding = box(0.px, 0.px, 0.99.px, 0.px)
        }
        queueEntry {
            spacing = 5.px
        }
        driveLabel {
            fontSize = 14.px
            padding = box(5.px)
        }
        progressLabel {
            alignment = BASELINE_RIGHT
            minWidth = 30.px
        }
        elapsedTimeLabel {
            alignment = BASELINE_RIGHT
            minWidth = 78.px
        }
    }
}
