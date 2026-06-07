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

    // Datos Embebidos (Costo Zero Sync)
    val budgetDataJson: String? = null,
    val rejectionReason: String? = null,
    val budgetRequestDescription: String? = null,
    val budgetRequestClientAddress: String? = null,

    // Datos de apoyo para visualización rápida (Turnos/Citas)
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentStatus: String? = null,
    val appointmentType: String? = null,
    val providerAddress: String? = null,
    val companyId: String? = null,
    val branchId: String? = null, // [DEPRECATED v7] Usar senderBranchId
    val categoryId: String? = null,

    // 🔥 [ELITE v7.5] SYMMETRIC TAGGED IDENTITY
    // senderBranchId -> Contexto de sucursal de quien envía el mensaje.
    // senderCompanyId -> Contexto corporativo de quien envía.
    // receiverBranchId -> Contexto de sucursal de quien recibe.
    // receiverCompanyId -> Contexto corporativo de quien recibe.
    val senderBranchId: String? = null,
    val senderCompanyId: String? = null,
    val receiverBranchId: String? = null,
    val receiverCompanyId: String? = null,

    // [SOBERANÍA LOCAL]: Tags de filtrado rápido para el DAO local.
    // Estos campos ayudan a que Room asigne el mensaje a la pestaña correcta del usuario local.
    val localBranchId: String? = null,
    val localCompanyId: String? = null,
    val remoteBranchId: String? = null,
    val remoteCompanyId: String? = null,

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
    val receiptPrioritizeCompany: Boolean? = null,

    // [NUEVO] Previsualización Elite (WhatsApp Style)
    val thumbnailBase64: String? = null,

    // Archivos Locales
    val imageLocalPath: String? = null,
    val audioLocalPath: String? = null,

    // Respuestas (Reply)
    val replyToId: String? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null,

    // Metadatos y Sincronización
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT", // SENT, READ, ERROR
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
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
