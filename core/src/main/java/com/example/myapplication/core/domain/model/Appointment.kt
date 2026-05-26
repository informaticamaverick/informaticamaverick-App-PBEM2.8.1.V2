package com.example.myapplication.core.domain.model

/**
 * --- MODELO DE DOMINIO: Appointment (TURNO / CITA) ---
 * Representa una solicitud de servicio técnico o cita programada.
 * Este objeto viaja entre el cliente y el prestador para coordinar agendas.
 */
data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val providerId: String = "",
    val clientName: String = "",
    val providerName: String = "",
    val requestDate: Long = 0L, // Timestamp en milisegundos
    val requestTime: String = "",
    val notes: String = "",
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val proposedDate: Long? = null,
    val proposedTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val chatId: String = ""
)

/**
 * Estados posibles de una cita.
 */
enum class AppointmentStatus {
    PENDING,        // Esperando respuesta del prestador
    ACCEPTED,       // Confirmado por ambas partes
    REJECTED,       // Cancelado o rechazado
    RESCHEDULED     // El prestador propuso una fecha alternativa
}
