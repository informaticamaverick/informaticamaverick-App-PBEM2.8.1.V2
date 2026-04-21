package com.example.myapplication.prestador.data.model

data class NotificacionItem(
    val id: Long = 0,
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val fechaMs: Long = System.currentTimeMillis(),
    val leida: Boolean = false,
    val accionRoute: String? = null
)

enum class TipoNotificacion(val label: String,
    val emoji: String ) {
    MENSAJE("Mensajes", "💬"),
    CITA("Citas", "📅"),
    PRESUPUESTO("Presupuestos", "📋"),
    SOLICITUD("Solicitudes", "⚡"),
    SISTEMA("Sistema", "🖥️")
}