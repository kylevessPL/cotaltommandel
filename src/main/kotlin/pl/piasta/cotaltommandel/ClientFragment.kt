package pl.piasta.cotaltommandel

import com.devskiller.friendly_id.FriendlyId.createFriendlyId
import javafx.beans.binding.Bindings.or
import javafx.beans.property.SimpleListProperty
import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import javafx.scene.layout.Priority.ALWAYS
import kotlin.math.ceil
import kotlin.random.Random
import pl.piasta.cotaltommandel.Constants.CLIENT_NAME_PREFIX
import pl.piasta.cotaltommandel.Constants.DATA_TRANSFER_RATE
import pl.piasta.cotaltommandel.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.FSNode.Directory
import pl.piasta.cotaltommandel.FSNode.Directory.Root
import pl.piasta.cotaltommandel.FSNode.File
import pl.piasta.cotaltommandel.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.Styles.Companion.driveLabel
import pl.piasta.cotaltommandel.Styles.Companion.progressLabel
import tornadofx.Controller
import tornadofx.Fragment
import tornadofx.Scope
import tornadofx.TaskStatus
import tornadofx.ViewModel
import tornadofx.action
import tornadofx.addClass
import tornadofx.asObservable
import tornadofx.button
import tornadofx.cellFormat
import tornadofx.disableWhen
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.populate
import tornadofx.progressbar
import tornadofx.runLater
import tornadofx.treeview
import tornadofx.useMaxHeight
import tornadofx.useMaxWidth
import tornadofx.vbox
import tornadofx.vgrow
import tornadofx.visibleWhen
import wtf.metio.storageunits.model.Byte

class DriveFragment : Fragment("Drive Tree View") {
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
        }
    }
}

class SharedDataScope(val sharedModel: SharedModel) : Scope()

class ClientFragment : Fragment("Client Fragment") {
    private val clientViewModel: ClientViewModel by inject()

    override val root = vbox {
        vgrow = ALWAYS
        add(
            find<DriveFragment>(
                DriveFragment::drive to clientViewModel.getClientName(),
                DriveFragment::nodes to clientViewModel.files
            )
        )
        hbox {
            button("Draw") {
                graphic = Image("$IMAGE_ASSETS/question-mark.png").asView()
                disableWhen(clientViewModel.status.running)
                action(clientViewModel::generateFile)
            }
            button("Start") {
                graphic = Image("$IMAGE_ASSETS/run.png").asView()
                disableWhen(or(clientViewModel.status.running, clientViewModel.files.emptyProperty()))
                action(clientViewModel::transfer)
            }
            progressbar(clientViewModel.status.progress) {
                useMaxHeight = true
                useMaxWidth = true
                hgrow = ALWAYS
                visibleWhen(clientViewModel.status.running)
            }
            label(clientViewModel.status.message) {
                useMaxHeight = true
                addClass(progressLabel)
                visibleWhen(clientViewModel.status.running)
            }
            addClass(actionPane)
        }
    }
}

class ClientViewModel : ViewModel() {
    override val scope = super.scope as SharedDataScope

    val status = TaskStatus()
    val files = SimpleListProperty(mutableListOf<File>().asObservable())
    private val clientController: ClientController by inject()
    private val sharedModel = scope.sharedModel
    private val clientNumber = ++sharedModel.clientCount
    private var timeElapsed = 0
    private var bytesTransferred = 0L.asByte()

    fun transfer() {
        val resource = files.first()
        runAsync(status) {
            var progress: Double
            val transferRate = clientController.calculateTransferRate(resource.size)
            do {
                val sizeLeft = clientController.calculateSizeLeft(resource.size, bytesTransferred)
                val priority = clientController.calculatePriority(sharedModel.clientCount, timeElapsed, sizeLeft)
                val priorityTotal = sharedModel.clientPriority.plus(clientNumber to priority)
                runLater { sharedModel.clientPriority.replaceAll(priorityTotal) }
                val priorityRate = clientController.calculatePriorityRate(priority, priorityTotal)
                bytesTransferred += clientController.copyNextChunk(sizeLeft, priorityRate, transferRate)
                timeElapsed++
                progress = bytesTransferred / resource.size
                updateMessage("${(progress * 100).toInt()}%")
                updateProgress(progress, 1.0)
                Thread.sleep(1000)
            } while (progress < 1.0)
            clientController.findServerClientDirectory(sharedModel.serverFiles, getClientName())
        }.ui {
            sharedModel.clientPriority.remove(clientNumber)
            files.clear()
            updateServer(it, resource)
        }
    }

    fun getClientName() = "$CLIENT_NAME_PREFIX$clientNumber"

    fun generateFile() {
        runAsync(status) {
            clientController.createFile()
        } ui {
            updateFiles(it)
        }
    }

    private fun updateServer(clientDirectory: Directory?, file: File) {
        val clientName = getClientName()
        clientDirectory
            ?.children
            ?.add(file)
            ?: run {
                sharedModel.serverFiles.add(Directory(clientName, mutableListOf(file).asObservable()))
            }
    }

    private fun updateFiles(file: File) = with(this.files) {
        timeElapsed = 0
        bytesTransferred = 0L.asByte()
        clear()
        addAll(file)
    }
}

class ClientController : Controller() {
    fun findServerClientDirectory(serverFiles: MutableList<FSNode>, clientName: String) =
        serverFiles.filterIsInstance<Directory>()
            .firstOrNull { e -> e.dirname == clientName }

    fun createFile() = File(createFriendlyId(), Random.nextLong(1, 900000000).asByte())

    fun calculateSizeLeft(totalSize: Byte, bytesTransferred: Byte) = totalSize - bytesTransferred

    fun calculatePriority(clientsTotal: Int, timeElapsed: Int, sizeLeft: Byte) =
        clientsTotal / sizeLeft.toDouble() + timeElapsed / clientsTotal

    fun calculatePriorityRate(priority: Double, priorityTotal: Map<Int, Double>) = priority / priorityTotal.values.sum()

    fun calculateTransferRate(totalSize: Byte) = ceil(DATA_TRANSFER_RATE * totalSize.toLong()).toLong()

    fun copyNextChunk(size: Byte, priorityRate: Double, transferRate: Long) =
        ceil(size.toLong() * priorityRate)
            .toLong()
            .coerceAtMost(transferRate)
            .asByte()
}
