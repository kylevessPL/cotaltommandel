package pl.piasta.cotaltommandel.ui

import wtf.metio.storageunits.model.Byte

internal sealed interface FSNode {
    open class File(val filename: String, val size: Byte) : FSNode
    open class Directory(val dirname: String, val children: MutableList<FSNode> = mutableListOf()) : FSNode {
        class Root(dirname: String, children: MutableList<FSNode> = mutableListOf()) : Directory(dirname, children)
    }
}
