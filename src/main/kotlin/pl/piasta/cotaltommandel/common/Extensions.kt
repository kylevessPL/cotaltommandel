package pl.piasta.cotaltommandel.common

import java.math.BigInteger
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import tornadofx.FXTask
import wtf.metio.storageunits.model.Byte

fun Long.asByte() = Byte.valueOf(BigInteger.valueOf(this))
fun Image.asView() = ImageView(this)
fun <K, V> MutableMap<K, V>.replaceAll(map: Map<K, V>) {
    clear()
    putAll(map)
}

fun <T> FXTask<T>.updateStatus(progress: Double) {
    val percent = (progress * 100).toInt()
    updateMessage("$percent%")
    updateProgress(progress, 1.0)
}

operator fun Byte.plus(other: Byte) = (toLong() + other.toLong()).asByte()
operator fun Byte.minus(other: Byte) = (toLong() - other.toLong()).asByte()
operator fun Byte.div(other: Byte) = toDouble() / other.toDouble()
