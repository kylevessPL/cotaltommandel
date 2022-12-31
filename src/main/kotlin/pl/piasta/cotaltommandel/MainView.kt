package pl.piasta.cotaltommandel

import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import pl.piasta.cotaltommandel.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.Styles.Companion.addClientButton
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.action
import tornadofx.addClass
import tornadofx.asObservable
import tornadofx.button
import tornadofx.find
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.scrollpane
import tornadofx.splitpane
import tornadofx.vbox
import tornadofx.vgrow

class MainView : View("Cotal Tommandel") {
    private val viewModel: MainViewModel by inject()
    private lateinit var clientView: Pane

    override val root = vbox {
        splitpane {
            vgrow = ALWAYS
            scrollpane {
                hbox {
                    clientView = vbox {
                        hgrow = ALWAYS
                        heightProperty().addListener { _ ->
                            vvalue = 1.0
                        }
                    }
                }
            }
            scrollpane {
                val serverTreeView = find<DriveFragment>(
                    DriveFragment::drive to "Server",
                    DriveFragment::nodes to viewModel.sharedModel.serverFiles
                )
                add(serverTreeView)
            }
        }
        hbox {
            button {
                graphic = Image("$IMAGE_ASSETS/add-client.png").asView()
                action {
                    val client = find(ClientFragment::class, SharedDataScope(viewModel.sharedModel))
                    clientView.add(client)
                }
                addClass(addClientButton)
            }
            addClass(actionPane)
        }
    }
}

class SharedModel {
    val serverFiles = mutableListOf<FSNode>().asObservable()
    val clientPriority = mutableMapOf<Int, Double>()
    var clientCount = 0
}

class MainViewModel : ViewModel() {
    val sharedModel = SharedModel()
}
