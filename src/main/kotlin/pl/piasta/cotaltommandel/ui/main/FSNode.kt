package pl.piasta.cotaltommandel.ui.main

import wtf.metio.storageunits.model.Byte

internal sealed class FSNode(val name: String) {
    open class File(filename: String, val size: Byte) : FSNode(filename)
    open class Directory(dirname: String, val children: MutableList<FSNode> = mutableListOf()) : FSNode(dirname) {
        class Root(dirname: String, children: MutableList<FSNode> = mutableListOf()) : Directory(dirname, children)
    }
}
