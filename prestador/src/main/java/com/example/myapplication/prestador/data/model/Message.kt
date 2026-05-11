package com.example.myapplication.prestador.data.model

/**
 *  Modelo de UI para representar un mensaje en la pantalla  del chat.
 *  Este modelo es lo que la pantalla "ve" - se construye a partir de MessageEntity (Room)
 *
 *  TIPOS DE MENSAJES
 *   TEXT              → Texto simple
 *   *  IMAGE             → Foto
 *   *  AUDIO             → Nota de voz
 *   *  LOCATION          → Ubicación GPS
 *   *  DOCUMENT          → Archivo adjunto
 *   *  BUDGET            → Presupuesto enviado por el prestador
 *   *  CALENDAR_INVITE   → Prestador envía su disponibilidad → cliente elige
 * día y hora
 *   *  APPOINTMENT_REQUEST → Cliente solicita un turno específico → prestador
 * acepta/rechaza
 */


data class Message(
    val id: String,
    val text: String? = null,
    val timestamp: Long,
    val isFromCurrentUser: Boolean,
    val type: MessageType = MessageType.TEXT,
    
    // Para mensajes de imagen
    val imageUrl: String? = null,
    
    // Para mensajes de audio
    val audioUrl: String? = null,
    val audioDuration: Int? = null,
    
    // Para mensajes de ubicación
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Para mensajes de documento
    val fileName: String? = null,
    val fileSize: Long? = null,


    // Para mensajes de presupuesto (solo strings, sin imagen)
    val budgetNumero: String? = null,
    val budgetTotal: Double? = null,
    val budgetSubtotal: Double? = null,
    val budgetImpuestos: Double? = null,
    val budgetItemsJson: String? = null,
    val budgetServiciosJson: String? = null,
    val budgetHonorariosJson: String? = null,
    val budgetGastosJson: String? = null,
    val budgetImpuestosJson: String? = null,
    val budgetNotas: String? = null,
    val budgetValidezDias: Int? = null,
    val budgetTituloTrabajo: String? = null,
    val budgetCategorias: String? = null,
    val budgetClienteId: String? = null,



    //invitación de calendario (CALENDAR_INVITE)
    //Prestador envia su disponibilidad; el cliente abre y elige
    val calendarStartDate: String? = null,
    val calendarEndDate: String? = null,
    val availabilityJson: String? = null,
    val bookedSlotsJson: String? = null,

    //El cliente elige fecha/hora del calendario y envia esta solicitud
    val appointmentId: String? = null,
    val appointmentTitle: String? = null,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentStatus: AppointmentProposalStatus? = null,
    val appointmentType: String? = null,   // 🔥 [NUEVO]
    val providerAddress: String? = null,   // 🔥 [NUEVO]
    val rejectionReason: String? = null,
    val calendarInviteMessageId: String? = null, //ID del CALENDAR_INVITE de origen

    // Comprobante de turno confirmado (APPOINTMENT_RECEIPT)
    val receiptService: String? = null,
    val receiptProviderName: String? = null,
    val receiptProviderType: String? = null, // "TECHNICAL" o "PROFESSIONAL"
    val receiptProfession: String? = null,
    val receiptAddress: String? = null,
    val receiptCode: String? = null,
    val receiptIsTechnician: Boolean = false,
    val receiptPrioritizeCompany: Boolean = false,
    val categoryId: String? = null,
    //Solicitudes de presupuesto del cliente (BUDGET_REQUEST)
    val budgetRequestDescription: String? = null,
    val budgetRequestClientAddress: String? = null,

    //Metadatos
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val isSynced: Boolean = false,

    // 🔥 [NUEVO] Campos para Respuestas (Reply) estilo WhatsApp
    val replyToId: String? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null
) {
    /**
     * Tipos de mensajes disponibles en el chat
     */
    enum class MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        LOCATION,
        DOCUMENT,
        VISIT,
        BUDGET,
        TENDER,
        SYSTEM,
        CALENDAR_INVITE,
        APPOINTMENT,
        APPOINTMENT_REQUEST,
        APPOINTMENT_RECEIPT,
        RESCHEDULE_NOTICE,
        COMPLETION_NOTICE,
        CANCELLATION_NOTICE,
        BUDGET_REQUEST
    }

    /**
     * Estado de una solicitud de turno enviada por el cliente
     * El prestador es quien acepta o rechaza
     */

    enum class AppointmentProposalStatus {
        PENDING,    // Esperando respuesta del cliente
        ACCEPTED,   // Cliente aceptó
        REJECTED    // Cliente rechazó
    }
}
