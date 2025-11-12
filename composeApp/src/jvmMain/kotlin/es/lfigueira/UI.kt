package es.lfigueira

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Toolkit

@Composable
fun UI() {
    val processManager = remember { ProcessManager() }
    var processes by remember { mutableStateOf(listOf<ProcessInfo>()) }
    var filterName by remember { mutableStateOf("") }
    var filterUser by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val accentColor = Color(0xFFA12B2B)
    val stopColor = accentColor.copy(alpha = 0.85f)

    // --- Monitor de hardware en tiempo real ---
    val systemInfo = remember { oshi.SystemInfo() }
    val hardware = systemInfo.hardware
    val cpu = hardware.processor
    val memory = hardware.memory

    var cpuUsage by remember { mutableStateOf(0f) }
    var ramUsage by remember { mutableStateOf(0f) }

    // 🔁 Actualización en tiempo real
    LaunchedEffect(Unit) {
        var prevTicks = cpu.systemCpuLoadTicks
        while (true) {
            delay(1000L)
            val newTicks = cpu.systemCpuLoadTicks
            val load = cpu.getSystemCpuLoadBetweenTicks(prevTicks)
            prevTicks = newTicks

            cpuUsage = (load * 100).toFloat()

            val totalMemory = memory.total.toFloat()
            val usedMemory = totalMemory - memory.available.toFloat()
            ramUsage = (usedMemory / totalMemory * 100)

            delay(3000L)
        }
    }

    fun refreshProcesses() {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) { processManager.listProcesses() }
            processes = result
        }
    }

    LaunchedEffect(Unit) { refreshProcesses() }

    // Filtrado en tiempo real
    val filteredProcesses = processes.filter {
        it.name.contains(filterName, ignoreCase = true) &&
                it.user.contains(filterUser, ignoreCase = true)
    }

    // --- Animaciones para los indicadores ---
    val animatedCpu by animateFloatAsState(targetValue = (cpuUsage / 100f).coerceIn(0f, 1f))
    val animatedRam by animateFloatAsState(targetValue = (ramUsage / 100f).coerceIn(0f, 1f))

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Título
                Row(verticalAlignment = CenterVertically) {
                    Text(
                        text = "Monitor de procesos",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Línea divisoria debajo
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 2.dp, color = accentColor.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // --- Filtros ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filterName,
                    onValueChange = { filterName = it },
                    label = { Text("Filtrar por nombre") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = accentColor.copy(alpha = 0.05f),
                        unfocusedContainerColor = accentColor.copy(alpha = 0.02f),
                        focusedIndicatorColor = accentColor,
                        unfocusedIndicatorColor = accentColor.copy(alpha = 0.6f),
                        cursorColor = accentColor,
                        focusedLabelColor = accentColor,      // mantiene el color cuando está flotando
                        unfocusedLabelColor = accentColor,    // mantiene el color cuando no está enfocado
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = filterUser,
                    onValueChange = { filterUser = it },
                    label = { Text("Filtrar por usuario") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = accentColor.copy(alpha = 0.05f),
                        unfocusedContainerColor = accentColor.copy(alpha = 0.02f),
                        focusedIndicatorColor = accentColor,
                        unfocusedIndicatorColor = accentColor.copy(alpha = 0.6f),
                        cursorColor = accentColor,
                        focusedLabelColor = accentColor,      // mantiene el color cuando está flotando
                        unfocusedLabelColor = accentColor,    // mantiene el color cuando no está enfocado
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { refreshProcesses() },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
                    modifier = Modifier.align(CenterVertically)
                ) { Text("Actualizar") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Cuerpo: resumen y lista ---
            Row(modifier = Modifier.fillMaxSize()) {
                // Zona izquierda: CPU y RAM
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.30f)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    // Tamaño adaptativo
                    val screenSize = Toolkit.getDefaultToolkit().screenSize
                    val circleSize: Dp = when {
                        screenSize.height > 1440 -> 320.dp
                        screenSize.height > 1080 -> 280.dp
                        else -> 220.dp
                    }

                    // --- CPU ---
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("CPU", style = MaterialTheme.typography.titleLarge)
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedCpu },
                                strokeWidth = 16.dp,
                                modifier = Modifier.size(circleSize),
                                trackColor = accentColor.copy(alpha = 0.05f),
                                color = Color(0xFFD94A2A)
                            )
                            Text("%.1f %%".format(cpuUsage), style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    HorizontalDivider(
                        color = stopColor,
                        thickness = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // --- RAM ---
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("RAM", style = MaterialTheme.typography.titleLarge)
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedRam },
                                strokeWidth = 16.dp,
                                modifier = Modifier.size(circleSize),
                                trackColor = accentColor.copy(alpha = 0.05f),
                                color = Color(0xFFA12B5E)
                            )
                            Text("%.1f %%".format(ramUsage), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                VerticalDivider(color = stopColor, thickness = 2.dp, modifier = Modifier.fillMaxHeight().width(2.dp))

                // Zona derecha: lista de procesos
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.70f)
                        .padding(start = 16.dp)
                ) {
                    // --- Cabecera de la lista ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor.copy(alpha = 0.9f))
                            .padding(vertical = 8.dp, horizontal = 8.dp), // padding uniforme
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("PID", color = Color.White, modifier = Modifier.weight(1f))
                        Text("Nombre", color = Color.White, modifier = Modifier.weight(2f))
                        Text("Usuario", color = Color.White, modifier = Modifier.weight(2f))
                        Text("CPU", color = Color.White, modifier = Modifier.weight(1f))
                        Text("Memoria", color = Color.White, modifier = Modifier.weight(1f))
                        Text("", color = Color.White, modifier = Modifier.weight(1.2f))
                    }

                    HorizontalDivider(color = accentColor.copy(alpha = 0.8f), thickness = 2.dp)

                    // --- Lista de procesos ---
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredProcesses) { process ->
                            val backgroundColor = if (filteredProcesses.indexOf(process) % 2 == 0) Color(0xFFF5F5F5) else Color.White
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(process.pid, modifier = Modifier.weight(1f))
                                Text(process.name, modifier = Modifier.weight(2f))
                                Text(process.user, modifier = Modifier.weight(2f))
                                Text("%.1f%%".format(process.cpu), modifier = Modifier.weight(1f))
                                Text("%.1f MB".format(process.memory), modifier = Modifier.weight(1f))
                                FinalizarButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val result = processManager.killProcess(process.pid)
                                            if (result.isSuccess) {
                                                refreshProcesses()
                                            } else {
                                                val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                                                snackbarHostState.showSnackbar(
                                                    "No se pudo finalizar '${process.name}': $errorMessage"
                                                )
                                            }
                                        }
                                    },
                                    accentColor = stopColor
                                )
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
fun FinalizarButton(
    onClick: () -> Unit,
    accentColor: Color
) {
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f)
    val bgColor = if (isHovered) accentColor.copy(alpha = 0.95f) else accentColor

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .height(36.dp)
            .width(100.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        tryAwaitRelease()
                    }
                )
            }
            .hoverable(
                interactionSource = remember { MutableInteractionSource() },
                enabled = true
            )
    ) {
        Text("Finalizar", fontWeight = FontWeight.Bold)
    }
}