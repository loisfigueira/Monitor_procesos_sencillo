package es.lfigueira

data class ProcessInfo (
    val pid: String,
    val name: String,
    val user: String,
    val cpu: Double,
    val memory: Double
)