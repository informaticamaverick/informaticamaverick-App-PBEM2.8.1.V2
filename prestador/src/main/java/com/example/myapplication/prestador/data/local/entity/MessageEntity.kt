package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad de mensaje individual en la base de datos
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["conversationId"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val conversationId: String,
    
    // Contenido del mensaje
    val text: String? = null,
    
    // Metadatos
    val timestamp: Long,
    val isFromCurrentUser: Boolean,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    
    // Tipo de mensaje
    val messageType: String = "TEXT", // TEXT, IMAGE, AUDIO, LOCATION, DOCUMENT, APPOINTMENT
    
    // Campos para imagen
    val imageUrl: String? = null,
    val imageLocalPath: String? = null,
    
    // Campos para audio
    val audioUrl: String? = null,
    val audioLocalPath: String? = null,
    val audioDuration: Int? = null, // en segundos
    
    // Campos para ubicación
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAddress: String? = null,
    
    // Campos para documento
    val documentUrl: String? = null,
    val documentLocalPath: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val fileMimeType: String? = null,
    
    // Campos para cita/appointment
    val appointmentId: String? = null,
    val appointmentTitle: String? = null,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentStatus: String? = null, // PENDING, CONFIRMED, REJECTED
    val rejectionReason: String? = null,
    //Campos para presupuestos
    val budgetDataJson: String? = null,
    
    // Estado de sincronización
    val isSynced: Boolean = false,
    val syncError: String? = null
)