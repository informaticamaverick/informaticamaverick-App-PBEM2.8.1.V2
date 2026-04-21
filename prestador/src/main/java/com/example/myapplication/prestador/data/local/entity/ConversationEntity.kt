package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidad de conversación/casilla en la base de datos
 */

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["lastMessageTimestamp"])
    ]
)

data class ConversationEntity(
    @PrimaryKey
    val conversationId: String,

    //Información del usuario/cliente
    val userId: String,
    val userName: String,
    val userAvatarUrl: String? = null,

    //Información del trabajo/servicio
    val serviceType: String? = null,
    val jobDescription: String? = null,

    //Último mensaje
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = 0,
    val lastMessageType: String = "TEXT",

    //Contadores
    val unreadCount: Int = 0,
    val totalMessages: Int = 0,

    //Estados de la conversacón
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,

    //Configuraciones de privacidad
    val notificationsEnabled: Boolean = true,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val lockPassword: String? = null,

    //Metadatos
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    //Sincornización
    val isSynced: Boolean = false
)