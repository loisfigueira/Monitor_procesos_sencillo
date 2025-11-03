package es.lfigueira

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material3.Text

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Monitor_procesos_sencillo",
    ) {
        Text("Sistema operativo detectado: ${detectOS()}")
    }
}