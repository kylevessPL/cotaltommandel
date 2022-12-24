package pl.piasta.concurrent

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.multi
import tornadofx.px

class Styles : Stylesheet() {
    init {
        root {
            backgroundColor = multi(Color.web("32302F"))
        }
        label {
            fontSize = 20.px
            padding = box(10.px)
            fontWeight = FontWeight.BOLD
            textFill = Color.WHITE
        }
        listView {
            // Disable focus
            faintFocusColor = Color.TRANSPARENT
            focusColor = Color.TRANSPARENT
        }
        button {
            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))
            backgroundColor = multi(Color.web("32302F"))
            borderColor = multi(box(Color.web("928374")))
            borderWidth = multi(box(4.px))
            fill = Color.WHITE
            textFill = Color.WHITE
        }
        progressBar {
            borderRadius = multi(box(0.px))
            backgroundRadius = multi(box(0.px))
        }
    }
}
