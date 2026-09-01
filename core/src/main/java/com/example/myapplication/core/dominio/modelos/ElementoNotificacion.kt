package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELO DE NOTIFICACIÓN MAVERICK (V2026) ---
 * Representa una alerta o aviso dentro del ecosistema.
 */
data class ElementoNotificacion(
    val id: Long = 0,
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val fechaMs: Long = System.currentTimeMillis(),
    val leida: Boolean = false,
    val rutaAccion: String? = null
)

enum class TipoNotificacion(val etiqueta: String, val emoji: String) {
    MENSAJE("Mensajes", "💬"),
    CITA("Citas", "📅"),
    PRESUPUESTO("Presupuestos", "📋"),
    SOLICITUD("Solicitudes", "⚡"),
    LICITACION("Licitación", "📄"),
    SISTEMA("Sistema", "🖥️")
}


































