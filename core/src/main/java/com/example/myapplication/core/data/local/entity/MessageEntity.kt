package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.core.domain.model.MessageType
import java.util.UUID

/**
 * --- ENTIDAD DE MENSAJE (ROOM) ---
 * Esta clase representa un mensaje individual almacenado localmente en el teléfono.
 * Es idéntica para Cliente y Prestador, lo que permite que el ChatRepository
 * compartido funcione en ambas aplicaciones sin cambios.
 */
@Entity(
    tableName = "messages",
    indices = [Index(value = ["chatId"])] // Optimización para búsquedas por conversación
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // ID de la conversación (ej: "clienteID_prestadorID")
    val chatId: String,

    // Identificación de participantes
    val senderId: String,
    val receiverId: String,

    // Tipo de mensaje (Texto, Imagen, Presupuesto, etc.)
    val type: MessageType,

    // Contenido del mensaje
    val content: String,
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAddress: String? = null,
    val durationSeconds: Int? = null,
    val relatedId: String? = null, // ID relacionado (ej: ID del Presupuesto)

    // Datos de apoyo para visualización rápida (Turnos/Citas)
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentStatus: String? = null,
    val appointmentType: String? = null,
    val providerAddress: String? = null,
    val companyId: String? = null,
    val categoryId: String? = null,

    // Invitaciones de Calendario
    val calendarStartDate: String? = null,
    val calendarEndDate: String? = null,
    val availabilityJson: String? = null,
    val bookedSlotsJson: String? = null,
    val calendarInviteMessageId: String? = null,

    // Comprobantes de Turno
    val receiptService: String? = null,
    val receiptProviderName: String? = null,
    val receiptIsTechnician: Boolean? = null,
    val receiptProfession: String? = null,
    val receiptAddress: String? = null,
    val receiptCode: String? = null,

    // Respuestas (Reply)
    val replyToId: String? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null,

    // Metadatos y Sincronización
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT", // SENT, READ, ERROR
    val isRead: Boolean = false,
    val isSynced: Boolean = false
) {
    // Constructor sin argumentos necesario para la deserialización de Firebase Realtime Database
    constructor() : this(
        chatId = "",
        senderId = "",
        receiverId = "",
        type = MessageType.TEXT,
        content = ""
    )
}
