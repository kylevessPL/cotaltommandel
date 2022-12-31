package pl.piasta.cotaltommandel.ui.main.fragment

import com.devskiller.friendly_id.FriendlyId
import javafx.beans.binding.Bindings.or
import javafx.beans.property.SimpleListProperty
import javafx.scene.image.Image
import javafx.scene.layout.Priority.ALWAYS
import kotlin.math.ceil
import kotlin.random.Random
import pl.piasta.cotaltommandel.common.Constants
import pl.piasta.cotaltommandel.common.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.common.asByte
import pl.piasta.cotaltommandel.common.asView
import pl.piasta.cotaltommandel.common.div
import pl.piasta.cotaltommandel.common.minus
import pl.piasta.cotaltommandel.common.plus
import pl.piasta.cotaltommandel.common.replaceAll
import pl.piasta.cotaltommandel.common.updateStatus
import pl.piasta.cotaltommandel.ui.main.FSNode
import pl.piasta.cotaltommandel.ui.main.SharedDataScope
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.progressLabel
import tornadofx.Controller
import tornadofx.Fragment
import tornadofx.TaskStatus
import tornadofx.ViewModel
import tornadofx.action
import tornadofx.addClass
import tornadofx.asObservable
import tornadofx.button
import tornadofx.disableWhen
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.progressbar
import tornadofx.useMaxHeight
import tornadofx.useMaxWidth
import tornadofx.vbox
import tornadofx.vgrow
import tornadofx.visibleWhen
import wtf.metio.storageunits.model.Byte

internal class ClientFragment : Fragment("Client Fragment") {
    private val clientViewModel: ClientViewModel by inject()

    override val root = vbox {
        vgrow = ALWAYS
        add(
            find<DriveFragment>(
                DriveFragment::drive to clientViewModel.clientName,
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

internal class ClientViewModel : ViewModel() {
    override val scope = super.scope as SharedDataScope

    val status = TaskStatus()
    val files = SimpleListProperty(mutableListOf<FSNode.File>().asObservable())
    val clientName by lazy { "${Constants.CLIENT_NAME_PREFIX}$clientNumber" }
    private val clientController: ClientController by inject()
    private val sharedModel = scope.sharedModel
    private val clientNumber = ++sharedModel.clientCount
    private var timeElapsed = 0
    private var bytesTransferred = 0L.asByte()

    fun transfer() {
        val resource = files.first()
        runAsync(true, status) {
            var progress: Double
            val transferRate = clientController.calculateTransferRate(resource.size)
            updateStatus(0.0)
            Thread.sleep(1000)
            do {
                val sizeLeft = clientController.calculateSizeLeft(resource.size, bytesTransferred)
                val priority = clientController.calculatePriority(sharedModel.clientCount, timeElapsed, sizeLeft)
                val priorityTotal = sharedModel.clientPriority + (clientNumber to priority)
                tornadofx.runLater { sharedModel.clientPriority.replaceAll(priorityTotal) }
                val priorityRate = clientController.calculatePriorityRate(priority, priorityTotal)
                bytesTransferred += clientController.copyNextChunk(sizeLeft, priorityRate, transferRate)
                timeElapsed++
                progress = bytesTransferred / resource.size
                updateStatus(progress)
                Thread.sleep(1000)
            } while (progress < 1.0)
            clientController.findServerClientDirectory(sharedModel.serverFiles, clientName)
        }.ui {
            sharedModel.clientPriority.remove(clientNumber)
            files.clear()
            updateServer(it, resource)
        }
    }

    fun generateFile() {
        runAsync(true) {
            clientController.createFile()
        } ui {
            updateFiles(it)
        }
    }

    private fun updateServer(clientDirectory: FSNode.Directory?, file: FSNode.File) {
        clientDirectory
            ?.children
            ?.add(file)
            ?: run {
                sharedModel.serverFiles.add(FSNode.Directory(clientName, mutableListOf(file).asObservable()))
            }
    }

    private fun updateFiles(file: FSNode.File) = with(files) {
        timeElapsed = 0
        bytesTransferred = 0L.asByte()
        clear()
        addAll(file)
    }
}

internal class ClientController : Controller() {
    fun findServerClientDirectory(serverFiles: MutableList<FSNode>, clientName: String) = serverFiles
        .filterIsInstance<FSNode.Directory>()
        .firstOrNull { e -> e.dirname == clientName }

    fun createFile() = FSNode.File(FriendlyId.createFriendlyId(), Random.nextLong(1, 900000000).asByte())

    fun calculateSizeLeft(totalSize: Byte, bytesTransferred: Byte) = totalSize - bytesTransferred

    fun calculatePriority(clientsTotal: Int, timeElapsed: Int, sizeLeft: Byte) =
        clientsTotal / sizeLeft.toDouble() + timeElapsed / clientsTotal

    fun calculatePriorityRate(priority: Double, priorityTotal: Map<Int, Double>) = priority / priorityTotal.values.sum()

    fun calculateTransferRate(totalSize: Byte) =
        ceil(Constants.DATA_TRANSFER_RATE * totalSize.toLong()).toLong()

    fun copyNextChunk(size: Byte, priorityRate: Double, transferRate: Long) =
        ceil(size.toLong() * priorityRate)
            .toLong()
            .coerceAtMost(transferRate)
            .asByte()
}
