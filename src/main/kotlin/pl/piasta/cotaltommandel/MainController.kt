package pl.piasta.cotaltommandel

import tornadofx.Controller

class MainController : Controller() {
    private var clientCount = 0

    fun newDriveName() = "Client ${++clientCount}"
}
