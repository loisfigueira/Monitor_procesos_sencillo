package es.lfigueira

enum class OperatingSystem {
    WINDOWS, LINUX, MAC, UNKNOWN
}

fun detectOS(): OperatingSystem {
    val osName = System.getProperty("os.name").lowercase()
    return  when {
        osName.contains("windows") -> OperatingSystem.WINDOWS
        osName.contains("linux") -> OperatingSystem.LINUX
        osName.contains("mac") || osName.contains("darwin") -> OperatingSystem.MAC
        else -> OperatingSystem.UNKNOWN
    }
}