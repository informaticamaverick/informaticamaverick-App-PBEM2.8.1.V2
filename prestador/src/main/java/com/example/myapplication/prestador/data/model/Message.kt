package com.example.myapplication.prestador.data.model

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
    
    // Para mensajes de cita
    val appointmentTitle: String? = null,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentId: String? = null,
    val appointmentStatus: AppointmentProposalStatus? = null,
    val rejectionReason: String? = null,

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
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val isSynced: Boolean = false,
) {
    enum class MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        LOCATION,
        DOCUMENT,
        APPOINTMENT,
        BUDGET
    }
    
    enum class AppointmentProposalStatus {
        PENDING,    // Esperando respuesta del cliente
        ACCEPTED,   // Cliente aceptó
        REJECTED    // Cliente rechazó
    }
}
