package com.example.myapplication.core

/**
 * --- CHAT ID HELPER (COMPARTIDO) ---
 * Asegura que el Cliente y el Prestador utilicen el mismo algoritmo para 
 * identificar sus conversaciones. Al ordenar alfabéticamente los UIDs, 
 * se garantiza que el ChatId sea idéntico para ambos participantes.
 */
object ChatIdHelper {
    
    /**
     * Genera un identificador de chat único entre dos usuarios.
     * @return String con el formato "uid_menor_uid_mayor"
     */
    fun generateChatId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    /**
     * Identifica quién es el interlocutor en una conversación.
     */
    fun extractOtherParticipantId(chatId: String, myUserId: String): String {
        return chatId.split("_").firstOrNull { it != myUserId } ?: ""
    }
}
