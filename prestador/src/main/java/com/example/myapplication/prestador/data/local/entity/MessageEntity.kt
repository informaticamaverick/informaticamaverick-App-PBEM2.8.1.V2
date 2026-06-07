/*
package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * --- OBSOLETO (MAVERICK ELITE v4.0) ---
 * Esta clase ha sido inactivada en favor de com.example.myapplication.core.data.local.entity.MessageEntity.
 * Se mantiene comentada por referencia histórica según auditoría.
 */
/*
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
    
    // Solicitud de turno
    val appointmentId: String? = null,
    val appointmentTitle: String? = null,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentStatus: String? = null, // PENDING, CONFIRMED, REJECTED
    val appointmentType: String? = null,   // 🔥 [NUEVO] TECHNICAL_VISIT o LOCAL_APPOINTMENT
    val providerAddress: String? = null,   // 🔥 [NUEVO] Dirección del local del prestador
    val companyId: String? = null,         // 🔥 [NUEVO] Contexto Multi-Perfil
    val categoryId: String? = null,        // 🔥 [NUEVO] Rubro del servicio
    val rejectionReason: String? = null,
    //Campos para presupuestos
    val budgetDataJson: String? = null,

    // Nuevos campos agregados en migración 39->40
    val calendarStartDate: String? = null,
    val calendarEndDate: String? = null,
    val availabilityJson: String? = null,
    val bookedSlotsJson: String? = null,
    val calendarInviteMessageId: String? = null,
    
    // Campos para comprobante de turno confirmado (APPOINTMENT_RECEIPT)
    val receiptService: String? = null,
    val receiptProviderName: String? = null,
    val receiptProfession: String? = null,
    val receiptAddress: String? = null,
    val receiptCode: String? = null,
    val receiptIsTechnician: Boolean = false,
    val receiptPrioritizeCompany: Boolean = false,
    
    // Estado de sincronización
    val isSynced: Boolean = false,
    val syncError: String? = null,
    //Campos de para la solicitud de presupuesto
    val budgetRequestDescription: String? = null,
    val budgetRequestClientAddress: String? = null,

    // 🔥 [NUEVO] Campos para Respuestas (Reply) estilo WhatsApp
    val replyToId: String? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null
)
*/
*/