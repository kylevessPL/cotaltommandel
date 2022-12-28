package pl.piasta.cotaltommandel

import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import pl.piasta.cotaltommandel.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.FSNode.Directory
import pl.piasta.cotaltommandel.FSNode.Directory.Root
import pl.piasta.cotaltommandel.FSNode.File
import pl.piasta.cotaltommandel.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.Styles.Companion.driveLabel
import tornadofx.Fragment
import tornadofx.View
import tornadofx.addClass
import tornadofx.asObservable
import tornadofx.button
import tornadofx.cellFormat
import tornadofx.hbox
import tornadofx.label
import tornadofx.populate
import tornadofx.splitpane
import tornadofx.treeview
import tornadofx.vbox
import tornadofx.vgrow
import wtf.metio.storageunits.model.Byte

sealed interface FSNode {
    open class File(val filename: String, val size: Byte) : FSNode
    open class Directory(val dirname: String, val children: Collection<FSNode> = emptyList()) : FSNode {
        class Root(dirname: String, children: List<FSNode> = emptyList()) : Directory(dirname, children)
    }
}

class MainView : View("Cotal Tommandel") {
    override val root = vbox {
        splitpane {
            vgrow = Priority.ALWAYS
            val serverTreeView = find<DriveTreeViewFragment>(DriveTreeViewFragment::drive to "Server")
            add(serverTreeView)
        }
        hbox {
            button {
                graphic = Image("$IMAGE_ASSETS/add-client.png").asView()
            }
            button {
                graphic = Image("$IMAGE_ASSETS/run.png").asView()
            }
            addClass(actionPane)
        }
    }
}

class DriveTreeViewFragment : Fragment("Drive Tree View") {
    val drive: String by param()
    val children = listOf<FSNode>().asObservable()
    private val rootNode = Root(drive, children)

    override val root = vbox {
        label {
            text = drive
            addClass(driveLabel)
        }
        treeview<FSNode>(TreeItem(rootNode)) {
            vgrow = Priority.ALWAYS
            populate {
                with(it) {
                    isExpanded = true
                    when (val node = value) {
                        is Directory -> node.children
                        is File -> null
                    }
                }
            }

            cellFormat {
                with(it) {
                    when (this) {
                        is Directory -> {
                            text = dirname
                            graphic = Image(
                                when (this) {
                                    is Root -> "$IMAGE_ASSETS/directory-root.png"
                                    else -> "$IMAGE_ASSETS/directory.png"
                                }
                            ).asView()
                        }

                        is File -> {
                            text = "$filename | ${size.asBestMatchingUnit()}"
                        }
                    }
                }
            }
        }
    }
}
