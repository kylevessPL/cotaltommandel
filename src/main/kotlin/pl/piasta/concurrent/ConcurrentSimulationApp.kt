package pl.piasta.concurrent

import tornadofx.App
import tornadofx.launch

class ConcurrentSimulationApp : App(MainView::class, Styles::class)

fun main() = launch<ConcurrentSimulationApp>()
