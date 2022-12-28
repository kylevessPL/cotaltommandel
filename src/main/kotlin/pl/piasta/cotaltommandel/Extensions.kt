package pl.piasta.cotaltommandel

import java.math.BigInteger
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import wtf.metio.storageunits.model.Byte

fun Long.asByte() = Byte.valueOf(BigInteger.valueOf(this))
fun Image.asView() = ImageView(this)
