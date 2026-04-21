package com.example.myapplication.presentation.util

object ChatIdHelper {
    /**
     * Genera un chatId único y consistente entre dos usuarios.
     * Siempre produce el mismo resultado sin importar el orden de los parámetros.
     * Ejempl: generateChayId
     */

    fun generateChat(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString ("_")
    }
}