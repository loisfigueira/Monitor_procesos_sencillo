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
        val pm = ProcessManager()
        val processes = pm.listProcesses()
        println("Se detectaron ${processes.size} procesos")
        processes.take(10).forEach { println(it) }
    }
}