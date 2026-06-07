package com.example.myapplication.core

/**
 * --- CHAT ID HELPER (COMPARTIDO) ---
 * Asegura que el Cliente y el Prestador utilicen el mismo algoritmo para 
 * identificar sus conversaciones. 
 * v4 Soporta claves compuestas para discriminar entre empresas y sucursales.
 * Mantiene la compatibilidad con el ordenamiento alfabético para el par de usuarios.
 */
object ChatIdHelper {

    /**
     * Genera un identificador de chat único y simétrico (Maverick Symmetric Identity v6).
     * Formato: uidA_uidB_branchA_branchB
     * Donde A es el usuario con UID menor.
     * 
     * @param uid1 Usuario 1
     * @param uid2 Usuario 2
     * @param b1 Sucursal del Usuario 1 en este contexto (null = Personal)
     * @param b2 Sucursal del Usuario 2 en este contexto (null = Personal)
     */
    fun generateChatId(
        uid1: String,
        uid2: String,
        c1: String? = null, // [DEPRECATED v6] Mantener firma por compatibilidad temporal
        b1: String? = null,
        c2: String? = null, // [DEPRECATED v6]
        b2: String? = null
    ): String {
        val isOrdered = uid1 < uid2
        val (uA, uB) = if (isOrdered) uid1 to uid2 else uid2 to uid1
        
        // 🔥 Normalización Maverick v6.1: null, "" y "none" son equivalentes a Personal
        fun normalize(b: String?) = b?.takeIf { it.isNotBlank() && it != "none" }
        
        val branchA = if (isOrdered) normalize(b1) else normalize(b2)
        val branchB = if (isOrdered) normalize(b2) else normalize(b1)

        return buildString {
            append(uA).append("_").append(uB).append("_")
            append(branchA ?: "none").append("_").append(branchB ?: "none")
        }
    }

    /**
     * Identifica quién es el interlocutor en una conversación.
     */
    fun extractOtherParticipantId(chatId: String, myUserId: String): String {
        val parts = chatId.split("_")
        if (parts.size < 2) return ""
        return if (parts[0] == myUserId) parts[1] else parts[0]
    }

    /**
     * Extrae el contexto de negocio asociado al usuario actual en el ChatId.
     * @return Triple(companyId, branchId, isComposite)
     */
    fun extractMyContext(chatId: String, myUserId: String): Triple<String?, String?, Boolean> {
        val parts = chatId.split("_")
        if (parts.size == 4) {
            // Soporte v6: uA_uB_bA_bB
            val isUserA = parts[0] == myUserId
            val b = if (isUserA) parts[2] else parts[3]
            return Triple(null, if (b == "none") null else b, b != "none")
        }
        
        if (parts.size >= 6) {
            // Soporte v5: uA_uB_cA_bA_cB_bB
            val isUserA = parts[0] == myUserId
            val (c, b) = if (isUserA) parts[2] to parts[3] else parts[4] to parts[5]
            return Triple(
                if (c == "none") null else c,
                if (b == "none") null else b,
                c != "none"
            )
        }

        // Soporte legado v4
        return extractEntityContext(chatId)
    }

    /**
     * Extrae el contexto de negocio asociado al OTRO participante.
     */
    fun extractOtherContext(chatId: String, myUserId: String): Triple<String?, String?, Boolean> {
        val parts = chatId.split("_")
        if (parts.size == 4) {
            // Soporte v6
            val isUserA = parts[0] == myUserId
            val b = if (isUserA) parts[3] else parts[2]
            return Triple(null, if (b == "none") null else b, b != "none")
        }

        if (parts.size >= 6) {
            // Soporte v5
            val isUserA = parts[0] == myUserId
            val (c, b) = if (isUserA) parts[4] to parts[5] else parts[2] to parts[3]
            return Triple(
                if (c == "none") null else c,
                if (b == "none") null else b,
                c != "none"
            )
        }
        
        return Triple(null, null, false)
    }

    /**
     * [LEGACY] Extrae el contexto de la entidad desde el ChatId v4.
     */
    fun extractEntityContext(chatId: String): Triple<String?, String?, Boolean> {
        val parts = chatId.split("_")
        return when (parts.size) {
            4 -> Triple(parts[2], parts[3], true)
            3 -> Triple(parts[2], null, true)
            else -> Triple(null, null, false)
        }
    }
}