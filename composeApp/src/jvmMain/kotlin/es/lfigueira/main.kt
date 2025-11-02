package es.lfigueira

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Monitor_procesos_sencillo",
    ) {

    }
}