package pl.piasta.cotaltommandel.ui.shared

import pl.piasta.cotaltommandel.ui.FSNode
import tornadofx.Scope
import tornadofx.asObservable

internal class SharedModel {
    val serverFiles = mutableListOf<FSNode>().asObservable()
    val clientPriority = mutableMapOf<Int, Double>()
    var clientCount = 0
}

internal class SharedDataScope(val sharedModel: SharedModel) : Scope()
