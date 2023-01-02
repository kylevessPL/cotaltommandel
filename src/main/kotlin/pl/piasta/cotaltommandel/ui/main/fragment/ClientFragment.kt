package pl.piasta.cotaltommandel.ui.main.fragment

import com.devskiller.friendly_id.FriendlyId.createFriendlyId
import java.lang.Thread.sleep
import java.security.SecureRandom
import java.util.Locale.US
import javafx.beans.binding.Bindings.and
import javafx.beans.binding.Bindings.or
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.scene.image.Image
import javafx.scene.layout.Priority.ALWAYS
import kotlin.math.ceil
import pl.piasta.cotaltommandel.common.Constants.CLIENT_NAME_PREFIX
import pl.piasta.cotaltommandel.common.Constants.DATA_TRANSFER_MAX_SIZE_B
import pl.piasta.cotaltommandel.common.Constants.FILE_SIZE_MAX_MB
import pl.piasta.cotaltommandel.common.Constants.FILE_SIZE_MIN_KB
import pl.piasta.cotaltommandel.common.Constants.IMAGE_ASSETS
import pl.piasta.cotaltommandel.common.Constants.SIZE_PRIORITY_MULTIPLIER
import pl.piasta.cotaltommandel.common.Constants.STATE_READY
import pl.piasta.cotaltommandel.common.asByte
import pl.piasta.cotaltommandel.common.asDurationString
import pl.piasta.cotaltommandel.common.asView
import pl.piasta.cotaltommandel.common.div
import pl.piasta.cotaltommandel.common.minus
import pl.piasta.cotaltommandel.common.plus
import pl.piasta.cotaltommandel.common.runLaterBlocking
import pl.piasta.cotaltommandel.common.toInt
import pl.piasta.cotaltommandel.common.updateStatus
import pl.piasta.cotaltommandel.ui.main.FSNode
import pl.piasta.cotaltommandel.ui.main.FSNode.Directory
import pl.piasta.cotaltommandel.ui.main.FSNode.File
import pl.piasta.cotaltommandel.ui.main.SharedDataScope
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.actionPane
import pl.piasta.cotaltommandel.ui.main.Styles.Companion.elapsedTimeLabel
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
import tornadofx.runLater
import tornadofx.useMaxHeight
import tornadofx.useMaxWidth
import tornadofx.vbox
import tornadofx.vgrow
import tornadofx.visibleWhen
import wtf.metio.storageunits.model.Byte


internal class ClientFragment : Fragment("Client Fragment") {
    private val viewModel: ClientViewModel by inject()

    override val root = vbox {
        vgrow = ALWAYS
        add(
            find<DriveFragment>(
                DriveFragment::drive to viewModel.clientName,
                DriveFragment::nodes to viewModel.files
            )
        )
        hbox {
            val runnningBinding = viewModel.timeElapsed.greaterThan(-1)
            val currentBinding = with(viewModel) {
                sharedModel.currentRunning.isEqualTo(clientNumber)
            }
            button("Draw") {
                graphic = Image("$IMAGE_ASSETS/question-mark.png").asView()
                disableWhen(runnningBinding)
                action(viewModel::generateFile)
            }
            button("Queue") {
                graphic = Image("$IMAGE_ASSETS/plus.png").asView()
                disableWhen(or(viewModel.files.emptyProperty(), runnningBinding))
                action(viewModel::run)
            }
            progressbar(viewModel.status.progress) {
                useMaxHeight = true
                useMaxWidth = true
                hgrow = ALWAYS
                visibleWhen(viewModel.status.running)
            }
            label(viewModel.status.message) {
                useMaxHeight = true
                visibleWhen(viewModel.status.running)
                addClass(progressLabel)
            }
            label(viewModel.timeElapsed.asDurationString()) {
                useMaxHeight = true
                visibleWhen(and(viewModel.status.running, !currentBinding))
                addClass(elapsedTimeLabel)
            }
            label(viewModel.priorityRate.asString(US, "%.2f")) {
                graphic = Image("$IMAGE_ASSETS/up-arrow.png").asView()
                useMaxHeight = true
                visibleWhen(and(viewModel.status.running, !currentBinding))
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
    val priorityRate = SimpleDoubleProperty(1.0)
    val timeElapsed = SimpleIntegerProperty(-1)
    val sharedModel = scope.sharedModel
    val clientNumber = ++sharedModel.clientCount

    fun run() {
        timeElapsed.value++
        val resource = files.first()
        runAsync(true, status) {
            var progress = 0.0
            var bytesTransferred = 0L.asByte()
            updateStatus(progress)
            do {
                sleep(1000)
                val currentRunning = runLaterBlocking { sharedModel.currentRunning.get() }!!
                if (currentRunning != clientNumber) {
                    val elapsed = runLaterBlocking { ++timeElapsed.value }!!
                    val clientCount = runLaterBlocking {
                        sharedModel.clientPriority.keys.count() + (elapsed == 1).toInt() - (sharedModel.currentRunning.value != STATE_READY).toInt()
                    }!!
                    val priority = clientController.calculatePriority(clientCount, elapsed, resource.size)
                    val clientPriority = runLaterBlocking {
                        sharedModel.clientPriority += clientNumber to priority
                        sharedModel.clientPriority - currentRunning
                    }!!
                    val priorityFinal = clientController.calculatePriorityRate(priority, clientPriority)
                    runLater { priorityRate.value = priorityFinal }
                } else {
                    val sizeLeft = clientController.calculateSizeLeft(resource.size, bytesTransferred)
                    bytesTransferred += clientController.copyNextChunk(sizeLeft)
                    progress = bytesTransferred / resource.size
                    updateStatus(progress)
                }
            } while (progress < 1.0)
            sleep(500)
            val serverFiles = runLaterBlocking { sharedModel.serverFiles }!!
            clientController.findServerClientDirectory(serverFiles, clientName)
        }.ui {
            with(sharedModel) {
                timeElapsed.value = -1
                clientPriority.remove(clientNumber)
                currentRunning.set(STATE_READY)
            }
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
        timeElapsed.value = -1
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
        clientsTotal / sizeLeft.toDouble() * SIZE_PRIORITY_MULTIPLIER + timeElapsed / clientsTotal

    fun calculatePriorityRate(priority: Double, priorityTotal: Map<Int, Double>) = priority / priorityTotal.values.sum()

    fun copyNextChunk(size: Byte) = ceil(size.toDouble()).toLong()
        .coerceAtMost(DATA_TRANSFER_MAX_SIZE_B)
        .asByte()
}
