package pl.piasta.cotaltommandel.ui.main.fragment

import com.devskiller.friendly_id.FriendlyId.createFriendlyId
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.security.SecureRandom
import javafx.beans.binding.Bindings.or
import javafx.beans.property.SimpleListProperty
import javafx.scene.image.Image
import javafx.scene.layout.Priority.ALWAYS
import kotlin.math.ceil
import pl.piasta.cotaltommandel.common.Constants.CLIENT_NAME_PREFIX
import pl.piasta.cotaltommandel.common.Constants.DATA_TRANSFER_MAX_SIZE_B
import pl.piasta.cotaltommandel.common.Constants.FILE_SIZE_MAX_MB
import pl.piasta.cotaltommandel.common.Constants.FILE_SIZE_MIN_KB
import pl.piasta.cotaltommandel.common.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.common.asByte
import pl.piasta.cotaltommandel.common.asView
import pl.piasta.cotaltommandel.common.div
import pl.piasta.cotaltommandel.common.minus
import pl.piasta.cotaltommandel.common.plus
import pl.piasta.cotaltommandel.common.runLaterBlocking
import pl.piasta.cotaltommandel.common.updateStatus
import pl.piasta.cotaltommandel.ui.main.FSNode
import pl.piasta.cotaltommandel.ui.main.FSNode.Directory
import pl.piasta.cotaltommandel.ui.main.FSNode.File
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
    val files = SimpleListProperty(mutableListOf<File>().asObservable())
    val clientName by lazy { "$CLIENT_NAME_PREFIX$clientNumber" }
    private val clientController: ClientController by inject()
    private val sharedModel = scope.sharedModel
    private val clientNumber = ++sharedModel.clientCount
    private var timeElapsed = 0
    private var bytesTransferred = 0L.asByte()

    fun transfer() {
        val resource = files.first()
        runAsync(true, status) {
            var progress: Double
            updateStatus(0.0)
            sleep(1000)
            do {
                timeElapsed++
                val sizeLeft = clientController.calculateSizeLeft(resource.size, bytesTransferred)
                var clientCount = runLaterBlocking { sharedModel.clientPriority.keys.count() }!!
                timeElapsed.takeIf { it == 1 }?.let { clientCount++ }
                val priority = clientController.calculatePriority(clientCount, timeElapsed, sizeLeft)
                val clientPriority = runLaterBlocking {
                    sharedModel.clientPriority += clientNumber to priority
                    sharedModel.clientPriority
                }!!
                val priorityRate = clientController.calculatePriorityRate(priority, clientPriority)
                val bytesAdded = clientController.copyNextChunk(sizeLeft, priorityRate)
                bytesTransferred += bytesAdded
                progress = bytesTransferred / resource.size
                if (clientName == "Client1")
                    println("priority: ${BigDecimal(priority).toPlainString()}, priorityRate:$priorityRate, sizeLeft: $sizeLeft, bytesTransferred: $bytesTransferred, bytesAdded: $bytesAdded, progress: $progress")
                updateStatus(progress)
                sleep(1000)
            } while (progress < 1.0)
            val serverFiles = runLaterBlocking { sharedModel.serverFiles }!!
            clientController.findServerClientDirectory(serverFiles, clientName)
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

    private fun updateServer(clientDirectory: Directory?, file: File) {
        clientDirectory
            ?.children
            ?.add(file)
            ?: run {
                sharedModel.serverFiles += Directory(clientName, mutableListOf(file).asObservable())
            }
    }

    private fun updateFiles(file: File) = with(files) {
        timeElapsed = 0
        bytesTransferred = 0L.asByte()
        clear()
        addAll(file)
    }
}

internal class ClientController : Controller() {
    private val random: SecureRandom by lazy { SecureRandom.getInstanceStrong() }

    private fun randomSize(): Byte {
        val fraction = random.nextLong(0, 100)
        fun number(whole: Long) = whole + fraction / 100.0

        return when (random.nextBoolean()) {
            true -> number(random.nextLong(FILE_SIZE_MIN_KB, 1024)) * 1024
            false -> number(random.nextLong(1, FILE_SIZE_MAX_MB)) * 1024 * 1024
        }.toLong().asByte()
    }

    fun findServerClientDirectory(serverFiles: MutableList<FSNode>, clientName: String) = serverFiles
        .filterIsInstance<Directory>()
        .firstOrNull { it.name == clientName }

    fun createFile() = File(createFriendlyId(), randomSize())

    fun calculateSizeLeft(totalSize: Byte, bytesTransferred: Byte) = totalSize - bytesTransferred

    fun calculatePriority(clientsTotal: Int, timeElapsed: Int, sizeLeft: Byte) =
        clientsTotal / sizeLeft.toDouble() * 10.0 + timeElapsed / clientsTotal

    fun calculatePriorityRate(priority: Double, priorityTotal: Map<Int, Double>) = priority / priorityTotal.values.sum()

    fun copyNextChunk(size: Byte, priorityRate: Double) = ceil(size.toLong() * priorityRate).toLong()
        .coerceAtMost(DATA_TRANSFER_MAX_SIZE_B)
        .asByte()
}
