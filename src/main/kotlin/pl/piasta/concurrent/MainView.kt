package pl.piasta.concurrent

import tornadofx.View
import tornadofx.label
import tornadofx.vbox

class MainView : View("AuMerge") {
    override val root = vbox {
        label("Drop your audio files here:")
    }
}
