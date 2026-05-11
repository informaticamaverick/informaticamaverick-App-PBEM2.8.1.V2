package com.example.myapplication.presentation.util

/**
 * MAVERICK CHAT ID HELPER
 * Genera y gestiona IDs de conversación consistentes y unificados.
 */
object ChatIdHelper {
    /**
     * Genera un chatId único y consistente entre dos usuarios.
     * Siempre produce el mismo resultado sin importar el orden de los parámetros.
     */
    fun generateChatId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    /**
     * Extrae el ID del otro participante a partir de un chatId y el ID del usuario actual.
     */
    fun extractOtherParticipantId(chatId: String, currentUserId: String): String {
        return chatId.split("_").firstOrNull { it != currentUserId } ?: ""
    }

    /**
     * Verifica si un usuario pertenece a una conversación específica.
     */
    fun isParticipant(chatId: String, userId: String): Boolean {
        return chatId.contains(userId)
    }
}