package es.lfigueira

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = filterName,
                    onValueChange = { filterName = it },
                    label = { Text("Filtrar por nombre") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = filterUser,
                    onValueChange = { filterUser = it },
                    label = { Text("Filtrar por usuario") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { refreshProcesses() }) {
                    Text("Actualizar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Text(text = "Actualizando...")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredProcesses) { process ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(process.pid, modifier = Modifier.weight(1f))
                            Text(process.name, modifier = Modifier.weight(2f))
                            Text(process.user, modifier = Modifier.weight(2f))
                            Text("%.1f%%".format(process.cpu), modifier = Modifier.weight(1f))
                            Text("%.1f MB".format(process.memory), modifier = Modifier.weight(1f))

                            Button(onClick = {
                                val success = processManager.killProcess(process.pid)
                                refreshProcesses()
                                if (!success) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error al finalizar proceso ${process.name}")
                                    }
                                }
                            }) {
                                Text("Finalizar")
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}