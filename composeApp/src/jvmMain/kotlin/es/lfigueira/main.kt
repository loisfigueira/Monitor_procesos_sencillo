package es.lfigueira

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        title = "Monitor de Procesos",
        onCloseRequest = ::exitApplication,
        state = WindowState(width = 1200.dp, height = 800.dp),
        resizable = true
    ) {
        val density = LocalDensity.current
        val scaleFactor = remember {
            val scale = java.awt.Toolkit.getDefaultToolkit().screenResolution / 96.0f
            scale.coerceIn(1f, 2f)
        }

        CompositionLocalProvider(
            LocalDensity provides Density(
                density.density * scaleFactor,
                density.fontScale * scaleFactor
            )
        ) {
            window.minimumSize = java.awt.Dimension(900, 750)
            UI()
        }
    }
}