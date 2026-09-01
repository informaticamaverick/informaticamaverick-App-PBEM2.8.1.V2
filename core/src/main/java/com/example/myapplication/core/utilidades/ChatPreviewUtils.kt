package com.example.myapplication.core.utilidades

/**
 * --- UTILIDADES DE VISTA PREVIA DE CHAT (v2026.ELITE) ---
 * [ELITE SSOT]: Centraliza la lógica de formateo para la lista de chats.
 */
object ChatPreviewUtils {

    /**
     * Mapea tipos de mensaje técnicos a etiquetas legibles en español con emojis.
     * Evita que el usuario vea contenido en bruto (JSON) en la bandeja de entrada.
     */
    fun obtenerTextoVistaPrevia(tipo: String, contenido: String): String {
        return when (tipo.uppercase()) {
            "TEXTO" -> contenido
            "IMAGEN" -> "📷 Imagen"
            "AUDIO" -> "🎤 Mensaje de voz"
            "UBICACION" -> "📍 Ubicación"
            "PRESUPUESTO" -> "💰 Presupuesto"
            "PRODUCTO" -> "📦 Producto"
            "TURNO", "VISITA" -> "🗓️ Propuesta de cita"
            "APPOINTMENT_RECEIPT" -> "✅ Cita confirmada"
            "FINALIZACION_TRABAJO" -> "🏁 Trabajo finalizado"
            "BUDGET_REQUEST" -> "❓ Solicitud de presupuesto"
            "SYSTEM" -> contenido
            else -> contenido
        }
    }
}
