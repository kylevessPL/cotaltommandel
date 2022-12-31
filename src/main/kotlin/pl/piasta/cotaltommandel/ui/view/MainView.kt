package pl.piasta.cotaltommandel.ui.view

import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import pl.piasta.cotaltommandel.common.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.common.asView
import pl.piasta.cotaltommandel.ui.fragment.ClientFragment
import pl.piasta.cotaltommandel.ui.fragment.DriveFragment
import pl.piasta.cotaltommandel.ui.shared.SharedDataScope
import pl.piasta.cotaltommandel.ui.shared.SharedModel
import pl.piasta.cotaltommandel.ui.style.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.ui.style.Styles.Companion.addClientButton
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.find
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.scrollpane
import tornadofx.splitpane
import tornadofx.vbox
import tornadofx.vgrow

internal class MainView : View("Cotal Tommandel") {
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

internal class MainViewModel : ViewModel() {
    val sharedModel = SharedModel()
}
