package com.example.myapplication.prestador.utils

object ChatIdHelper {
    fun generateChatId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }
}