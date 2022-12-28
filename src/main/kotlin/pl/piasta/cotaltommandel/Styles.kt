package pl.piasta.cotaltommandel

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.multi
import tornadofx.percent
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val driveLabel by cssclass()
        val actionPane by cssclass()
    }

    init {
        root {
            prefWidth = 400.px
            prefHeight = 600.px
        }
        driveLabel {
            fontSize = 14.px
        }
        button {
            minWidth = 50.px
            minHeight = 50.px
            endMargin = 20.px
            backgroundRadius = multi(box(50.percent))
        }
        actionPane {
            padding = box(5.px)
            spacing = 5.px
        }
    }
}
