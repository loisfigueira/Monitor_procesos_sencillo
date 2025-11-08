package es.lfigueira

import java.io.BufferedReader
import java.io.InputStreamReader

import oshi.SystemInfo
import oshi.software.os.OSProcess

class ProcessManager {

    private val systemInfo = SystemInfo()
    private val os = systemInfo.operatingSystem

    // Para guardar ticks de CPU de la última medición
    private var previousCpuTimes: Map<Int, Long> = emptyMap()

    fun listProcesses(): List<ProcessInfo> {
        return when (detectOS()) {
            OperatingSystem.WINDOWS -> listWindowsProcesses()
            OperatingSystem.LINUX, OperatingSystem.MAC -> listUnixProcesses()
            else -> emptyList()
        }
    }



    private fun listWindowsProcesses(): List<ProcessInfo> {
        val processList = mutableListOf<ProcessInfo>()
        val oshiOs: oshi.software.os.OperatingSystem = systemInfo.operatingSystem

        // Obtener todos los procesos ordenados por PID
        val processes: List<OSProcess> = oshiOs.getProcesses(
            { true }, // filtro: todos los procesos
            Comparator.comparingInt { it.processID }, // orden por PID
            0 // sin límite
        )

        // Guardamos ticks actuales (user + kernel)
        val currentCpuTimes = processes.associate { it.processID to (it.userTime + it.kernelTime) }

        // Si no hay ticks anteriores, inicializamos y devolvemos CPU 0%
        if (previousCpuTimes.isEmpty()) {
            previousCpuTimes = currentCpuTimes
            return processes.map {
                ProcessInfo(
                    pid = it.processID.toString(),
                    name = it.name,
                    user = it.user,
                    cpu = 0.0,
                    memory = it.residentSetSize.toDouble() / 1024.0 / 1024.0 // MB
                )
            }
        }

        // Calculamos CPU %
        val cpuUsageList = processes.map { process ->
            val prevTicks = previousCpuTimes[process.processID] ?: 0L
            val currTicks = process.userTime + process.kernelTime
            val deltaTicks = currTicks - prevTicks

            val elapsedTimeMillis = 1000.0 // por ejemplo 1 segundo entre actualizaciones
            val cpuPercent = deltaTicks.toDouble() / (elapsedTimeMillis * Runtime.getRuntime().availableProcessors()) * 100.0


            ProcessInfo(
                pid = process.processID.toString(),
                name = process.name,
                user = process.user,
                cpu = cpuPercent,
                memory = process.residentSetSize.toDouble() / 1024.0 / 1024.0
            )
        }

        // Actualizamos ticks anteriores
        previousCpuTimes = currentCpuTimes
        return cpuUsageList
    }

    private fun listUnixProcesses(): List<ProcessInfo> {
        val processList = mutableListOf<ProcessInfo>()

        try {
            val processBuilder = ProcessBuilder("ps", "-eo", "pid,comm,user,%cpu,%mem", "--no-headers")
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            reader.forEachLine { line ->
                val parts = line.trim().split("\\s+".toRegex(), 5)
                if (parts.size == 5) {
                    val pid = parts[0]
                    val name = parts[1]
                    val user = parts[2]
                    val cpu = parts[3].toDoubleOrNull() ?: 0.0
                    val memory = parts[4].toDoubleOrNull() ?: 0.0
                    processList.add(ProcessInfo(pid, name, user, cpu, memory))
                }
            }
        } catch (e: Exception) {
            print("Error listando procesos en Linux/Mac: ${e.message}")
        }
        return processList
    }

    fun killProcess(pid: String): Boolean {
        return try {
            val command = when (detectOS()) {
                OperatingSystem.WINDOWS -> listOf("taskkill", "/PID", pid, "/F")
                OperatingSystem.LINUX, OperatingSystem.MAC -> listOf("kill", "-9", pid)
                else -> return false
            }
            ProcessBuilder(command).start().waitFor()
            true
        } catch (e: Exception) {
            print("Error al matar un proceso: ${e.message}")
            false
        }
    }
}