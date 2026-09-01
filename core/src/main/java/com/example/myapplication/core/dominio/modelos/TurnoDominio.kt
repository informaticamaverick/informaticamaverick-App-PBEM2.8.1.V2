package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import com.example.myapplication.core.datos.local.entidades.EstadoEvento

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Modelo de Turno (UiModel)
 * [PROPÓSITO]: Representar una solicitud de servicio técnico o cita programada para la UI.
 * [FUNCIONAMIENTO INTERNO]: Estándar Mav (Idioma Español). Estructura inmutable coordinada entre cliente y prestador.
 * [RELACIÓN]: Se vincula con 'EventoEntity' para persistencia.
 */
@Keep
data class TurnoDominio(
    val id: String = "",
    val idCliente: String = "",
    val idPrestador: String = "",
    val nombreCliente: String = "",
    val nombrePrestador: String = "",
    val fechaSolicitud: Long = 0L, // Timestamp UTC
    val horaSolicitud: String = "",
    val notas: String = "",
    val estado: EstadoTurno = EstadoTurno.PENDIENTE,
    val fechaPropuesta: Long? = null,
    val horaPropuesta: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val idChat: String = ""
)

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Estado del Turno
 * [PROPÓSITO]: Enumeración de estados posibles para un turno.
 */
@Keep
enum class EstadoTurno {
    PENDIENTE,      // Esperando respuesta del prestador
    ACEPTADO,       // Confirmado por ambas partes
    RECHAZADO,      // Cancelado o rechazado
    REPROGRAMADO;   // El prestador propuso una fecha alternativa

    fun aEstadoEvento(): EstadoEvento {
        return when (this) {
            PENDIENTE -> EstadoEvento.SOLICITADO
            ACEPTADO -> EstadoEvento.CONFIRMADO
            RECHAZADO -> EstadoEvento.CANCELADO
            REPROGRAMADO -> EstadoEvento.SOLICITADO
        }
    }
}

