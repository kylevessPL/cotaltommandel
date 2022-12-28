package pl.piasta.cotaltommandel

import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import javafx.scene.layout.Priority.ALWAYS
import pl.piasta.cotaltommandel.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.FSNode.Directory
import pl.piasta.cotaltommandel.FSNode.Directory.Root
import pl.piasta.cotaltommandel.FSNode.File
import pl.piasta.cotaltommandel.Styles.Companion.driveLabel
import pl.piasta.cotaltommandel.Styles.Companion.drivePane
import pl.piasta.cotaltommandel.Styles.Companion.treePane
import tornadofx.Fragment
import tornadofx.addClass
import tornadofx.asObservable
import tornadofx.cellFormat
import tornadofx.label
import tornadofx.populate
import tornadofx.treeview
import tornadofx.vbox
import tornadofx.vgrow

class DriveTreeViewFragment : Fragment("Drive Tree View") {
    val drive: String by param()
    val children = mutableListOf<FSNode>().asObservable()
    private val rootNode = Root(drive, children)

    override val root = vbox {
        vgrow = ALWAYS
        label(drive) {
            addClass(driveLabel)
        }
        treeview<FSNode>(TreeItem(rootNode)) {
            vgrow = ALWAYS
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
                            graphic = null
                        }
                    }
                }
            }
            addClass(treePane)
        }
        addClass(drivePane)
    }
}
