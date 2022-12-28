package pl.piasta.cotaltommandel

import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import pl.piasta.cotaltommandel.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.Styles.Companion.clientPane
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.scrollpane
import tornadofx.splitpane
import tornadofx.vbox
import tornadofx.vgrow

class MainView : View("Cotal Tommandel") {
    private val controller: MainController by inject()
    private lateinit var clientView: Pane
    private lateinit var serverTreeView: DriveTreeViewFragment

    override val root = vbox {
        splitpane {
            vgrow = ALWAYS
            scrollpane {
                hbox {
                    clientView = vbox {
                        hgrow = ALWAYS
                    }
                }
                addClass(clientPane)
            }
            scrollpane {
                serverTreeView = find(DriveTreeViewFragment::drive to "Server")
                add(serverTreeView)
            }
        }
        hbox {
            button {
                graphic = Image("$IMAGE_ASSETS/add-client.png").asView()
                action {
                    val client = find<DriveTreeViewFragment>(DriveTreeViewFragment::drive to controller.newDriveName())
                    clientView.add(client)
                }
            }
            button {
                graphic = Image("$IMAGE_ASSETS/run.png").asView()
            }
            addClass(actionPane)
        }
    }
}
