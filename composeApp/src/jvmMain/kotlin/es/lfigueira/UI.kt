package es.lfigueira

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UI() {
    val processManager = remember { ProcessManager() }
    var processes by remember { mutableStateOf(listOf<ProcessInfo>()) }
    var filterName by remember { mutableStateOf("") }
    var filterUser by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val accentColor = Color(0xFFA12B2B)
    val stopColor = accentColor.copy(alpha = 0.85f)

    fun refreshProcesses() {
        coroutineScope.launch {
            isLoading = true
            val result = withContext(Dispatchers.IO) { processManager.listProcesses() }
            processes = result
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshProcesses() }

    // Filtrado en tiempo real
    val filteredProcesses = processes.filter {
        it.name.contains(filterName, ignoreCase = true) &&
                it.user.contains(filterUser, ignoreCase = true)
    }

    val totalCpu = processes.filter { it.name.lowercase() != "idle" }.sumOf { it.cpu }
    val totalMemory = processes.sumOf { it.memory }

    val osBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
    val totalRamBytes = osBean.totalMemorySize
    val totalRamMB = totalRamBytes / (1024.0 * 1024.0)
    val usedMemoryPercent = (totalMemory / totalRamMB) * 100

    // Animación para los indicadores
    val animatedCpu by animateFloatAsState(targetValue = (totalCpu.toFloat() / 100f).coerceIn(0f, 1f))
    val animatedRam by animateFloatAsState(targetValue = (usedMemoryPercent.toFloat() / 100f).coerceIn(0f, 1f))

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // --- Header simplificado ---
            Text(
                text = "Monitor de Procesos",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = accentColor
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Filtros ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filterName,
                    onValueChange = { filterName = it },
                    label = { Text("Filtrar por nombre") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = accentColor.copy(alpha = 0.05f),
                        unfocusedContainerColor = accentColor.copy(alpha = 0.02f),
                        focusedIndicatorColor = accentColor,
                        unfocusedIndicatorColor = accentColor.copy(alpha = 0.6f),
                        cursorColor = accentColor
                    )
                )
                OutlinedTextField(
                    value = filterUser,
                    onValueChange = { filterUser = it },
                    label = { Text("Filtrar por usuario") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = accentColor.copy(alpha = 0.05f),
                        unfocusedContainerColor = accentColor.copy(alpha = 0.02f),
                        focusedIndicatorColor = accentColor,
                        unfocusedIndicatorColor = accentColor.copy(alpha = 0.6f),
                        cursorColor = accentColor
                    )
                )
                Button(
                    onClick = { refreshProcesses() },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White)
                ) { Text("Actualizar") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Cuerpo: resumen y lista ---
            Row(modifier = Modifier.fillMaxSize()) {
                // Zona izquierda: CPU y RAM
                Column(
                    modifier = Modifier.fillMaxHeight().weight(0.45f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // CPU
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CPU", style = MaterialTheme.typography.titleLarge)
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedCpu },
                                strokeWidth = 16.dp,
                                modifier = Modifier.size(300.dp),
                                trackColor = Color.Gray.copy(alpha = 0.3f),
                                color = Color(0xFFD94A2A) // Color sólido, no gradiente
                            )
                            Text("%.1f %%".format(totalCpu), style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    HorizontalDivider(color = Color.Gray, thickness = 2.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))

                    // RAM
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RAM", style = MaterialTheme.typography.titleLarge)
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedRam },
                                strokeWidth = 16.dp,
                                modifier = Modifier.size(300.dp),
                                trackColor = Color.Gray.copy(alpha = 0.3f),
                                color = Color(0xFFA12B5E) // Color sólido
                            )
                            Text("%.1f %%".format(usedMemoryPercent), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                VerticalDivider(color = Color.Gray, modifier = Modifier.fillMaxHeight().width(2.dp))

                // Zona derecha: lista de procesos
                Column(modifier = Modifier.fillMaxHeight().weight(0.55f).padding(start = 16.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredProcesses) { process ->
                            val backgroundColor = if (filteredProcesses.indexOf(process) % 2 == 0) Color(0xFFF5F5F5) else Color.White
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(process.pid, modifier = Modifier.weight(1f))
                                Text(process.name, modifier = Modifier.weight(2f))
                                Text(process.user, modifier = Modifier.weight(2f))
                                Text("%.1f%%".format(process.cpu), modifier = Modifier.weight(1f))
                                Text("%.1f MB".format(process.memory), modifier = Modifier.weight(1f))
                                Button(
                                    onClick = {
                                        val success = processManager.killProcess(process.pid)
                                        refreshProcesses()
                                        if (!success) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Error al finalizar proceso ${process.name}")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = stopColor,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp).hoverable(MutableInteractionSource())
                                ) { Text("Finalizar", style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}