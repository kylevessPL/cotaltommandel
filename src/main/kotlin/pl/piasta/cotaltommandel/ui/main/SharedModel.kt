package pl.piasta.cotaltommandel.ui.main

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleMapProperty
import pl.piasta.cotaltommandel.common.Constants.STATE_READY
import tornadofx.Scope
import tornadofx.asObservable

internal class SharedModel {
    val serverFiles = mutableListOf<FSNode>().asObservable()
    val clientPriority = SimpleMapProperty(mutableMapOf<Int, Double>().asObservable())
    val currentRunning = SimpleIntegerProperty(STATE_READY)
    var clientCount = 0
}

internal class SharedDataScope(val sharedModel: SharedModel) : Scope()
