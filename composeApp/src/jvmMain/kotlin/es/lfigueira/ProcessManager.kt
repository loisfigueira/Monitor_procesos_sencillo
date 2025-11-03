package es.lfigueira

import java.io.BufferedReader
import java.io.InputStreamReader

class ProcessManager {

    fun listProcesses(): List<ProcessInfo> {
        return when (detectOS()) {
            OperatingSystem.WINDOWS -> listWindowsProcesses()
            OperatingSystem.LINUX, OperatingSystem.MAC -> listUnixProcesses()
            else -> emptyList()
        }
    }

    private fun listWindowsProcesses(): List<ProcessInfo> {
        val processList = mutableListOf<ProcessInfo>()

        try {
            val processBuilder = ProcessBuilder("tasklist", "/FO", "CSV", "/V")
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            reader.readLine()
            reader.forEachLine {
                line -> val parts = line.split("\",\"").map { it.trim('"') }

                if (parts.size >= 9) {
                    val pid = parts[1]
                    val name = parts[0]
                    val user = parts[6]
                    val cpu = 0.0
                    val memory = (parts[4].replace("[^0-9]".toRegex(), "").toDoubleOrNull() ?: 0.0) / 1024
                    processList.add(ProcessInfo(pid, name, user, cpu, memory))
                }
            }
        } catch (e: Exception) {
            print("Error listando procesos en Windows: ${e.message}")
        }
        return processList
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
}