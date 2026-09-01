package com.example.myapplication.core.utilidades

/**
 * --- CHAT ID HELPER (PROTOCOL 2026) ---
 * Normaliza la generación de IDs de conversación para asegurar que emisor y receptor
 * compartan el mismo hilo de chat sin colisiones.
 */
object ChatIdHelper {
    
    /**
     * Genera un ID determinista basado en los dos participantes.
     * Siempre coloca el ID menor primero para que el resultado sea el mismo
     * independientemente de quién inicie la conversación.
     */
    fun generateChatId(id1: String, id2: String): String {
        return if (id1 < id2) "${id1}_${id2}" else "${id2}_${id1}"
    }

    /**
     * Extrae el ID del otro participante dado el chatId y el ID propio.
     */
    fun getOtherParticipantId(chatId: String, myId: String): String {
        val parts = chatId.split("_")
        return if (parts[0] == myId) parts.getOrElse(1) { "" } else parts[0]
    }
}

































