package com.example.myapplication.prestador.data.model

import androidx.compose.ui.graphics.Color

/**
 * Representa una conversación/chat en la lista
 */
data class ChatConversation(
    val userId: String,           // ID único del cliente
    val name: String,             // Nombre del cliente
    val job: String,              // Servicio solicitado
    val avatarColor: Color,       // Color del avatar
    val lastMessage: String,      // Último mensaje enviado
    val lastMessageTime: String,  // "10:05", "Ayer", etc.
    val isOnline: Boolean = false // Estado en línea
)
