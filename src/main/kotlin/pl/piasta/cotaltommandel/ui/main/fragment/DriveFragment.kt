package pl.piasta.cotaltommandel.ui.main.fragment

import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import javafx.scene.layout.Priority.ALWAYS
import pl.piasta.cotaltommandel.common.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.common.asView
import pl.piasta.cotaltommandel.ui.main.FSNode
import pl.piasta.cotaltommandel.ui.main.FSNode.Directory
import pl.piasta.cotaltommandel.ui.main.FSNode.Directory.Root
import pl.piasta.cotaltommandel.ui.main.FSNode.File
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.driveLabel
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.drivePane
import tornadofx.Fragment
import tornadofx.addClass
import tornadofx.cellFormat
import tornadofx.label
import tornadofx.populate
import tornadofx.treeview
import tornadofx.vbox
import tornadofx.vgrow

internal class DriveFragment : Fragment("Drive Fragment") {
    val drive: String by param()
    val nodes: MutableList<FSNode> by param()

    override val root = vbox {
        vgrow = ALWAYS
        label(drive) {
            addClass(driveLabel)
        }
        treeview<FSNode>(TreeItem(Root(drive, nodes))) {
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
                            text = name
                            graphic = Image(
                                when (this) {
                                    is Root -> "$IMAGE_ASSETS/directory-root.png"
                                    else -> "$IMAGE_ASSETS/directory.png"
                                }
                            ).asView()
                        }

                        is File -> {
                            text = "$name | ${size.asBestMatchingUnit()}"
                            graphic = null
                        }
                    }
                }
            }
        }
        addClass(drivePane)
    }
}
