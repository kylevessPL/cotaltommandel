package pl.piasta.cotaltommandel

import tornadofx.View
import tornadofx.label
import tornadofx.vbox

class MainView : View("Cotal Tommandel") {
    override val root = vbox {
        label("Initial message")
    }
}
