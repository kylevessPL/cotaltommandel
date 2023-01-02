package pl.piasta.cotaltommandel.ui.main

import javafx.beans.binding.Bindings.and
import javafx.beans.binding.Bindings.or
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import kotlin.collections.Map.Entry
import pl.piasta.cotaltommandel.common.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.common.Constants.STATE_READY
import pl.piasta.cotaltommandel.common.asView
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.actionButton
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.playButton
import pl.piasta.cotaltommandel.ui.main.fragment.ClientFragment
import pl.piasta.cotaltommandel.ui.main.fragment.DriveFragment
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.disableWhen
import tornadofx.find
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.onChange
import tornadofx.scrollpane
import tornadofx.splitpane
import tornadofx.vbox
import tornadofx.vgrow

internal class MainView : View("Cotal Tommandel") {
    private val viewModel: MainViewModel by inject()
    private lateinit var clientView: Pane

    init {
        with(viewModel) {
            sharedModel.currentRunning.onChange {
                if (it == STATE_READY) {
                    runNext()
                }
            }
        }
    }

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
            }
            button {
                graphic = Image("$IMAGE_ASSETS/play.png").asView()
                disableWhen(
                    or(
                        viewModel.sharedModel.clientPriority.emptyProperty(), and(
                            !viewModel.sharedModel.clientPriority.emptyProperty(),
                            viewModel.sharedModel.currentRunning.greaterThan(STATE_READY)
                        )
                    )
                )
                action(viewModel::runNext)
                addClass(playButton)
            }
            children.addClass(actionButton)
            addClass(actionPane)
        }
    }
}

internal class MainViewModel : ViewModel() {
    val sharedModel = SharedModel()

    fun runNext() = with(sharedModel) {
        val next = sharedModel.clientPriority
            .maxByOrNull(Entry<Int, Double>::value)
            ?.key
            ?: STATE_READY
        currentRunning.set(next)
    }
}
