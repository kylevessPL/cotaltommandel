package pl.piasta.cotaltommandel

import java.math.BigInteger
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import wtf.metio.storageunits.model.Byte

fun Long.asByte() = Byte.valueOf(BigInteger.valueOf(this))
fun Image.asView() = ImageView(this)
fun <K, V> MutableMap<K, V>.replaceAll(map: Map<K, V>) {
    this.clear()
    this.putAll(map)
}

operator fun Byte.plus(other: Byte) = (toLong() + other.toLong()).asByte()
operator fun Byte.minus(other: Byte) = (toLong() - other.toLong()).asByte()
operator fun Byte.div(other: Byte) = toDouble() / other.toDouble()
