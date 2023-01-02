package pl.piasta.cotaltommandel.common

import java.util.concurrent.CountDownLatch
import javafx.beans.binding.Bindings.createStringBinding
import javafx.beans.binding.IntegerExpression
import javafx.beans.binding.StringBinding
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import tornadofx.FXTask
import tornadofx.runLater
import wtf.metio.storageunits.model.Byte

fun Long.asByte() = Byte.valueOf(toBigInteger())
fun Image.asView() = ImageView(this)
fun Boolean.toInt() = if (this) 1 else 0
fun IntegerExpression.asDurationString(): StringBinding {
    fun durationString(): String {
        val duration = value.seconds
        return when (duration.inWholeDays > 1) {
            true -> "${1.days}+"
            false -> duration.toString()
        }
    }

    return createStringBinding({ durationString() }, this)
}

fun <T> FXTask<T>.updateStatus(progress: Double) {
    val percent = (progress * 100).toInt()
    updateMessage("$percent%")
    updateProgress(progress, 1.0)
}

fun <T> runLaterBlocking(latch: CountDownLatch = CountDownLatch(1), op: () -> T): T? {
    var result: T? = null
    runLater {
        result = op()
        latch.countDown()
    }
    latch.await()
    return result
}

operator fun Byte.plus(other: Byte) = (toLong() + other.toLong()).asByte()
operator fun Byte.minus(other: Byte) = (toLong() - other.toLong()).asByte()
operator fun Byte.div(other: Byte) = toDouble() / other.toDouble()
