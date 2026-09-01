package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.EventoDominio
import com.example.myapplication.core.utilidades.CalendarUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- EVENTO MAPPER (v2026.ELITE) ---
 * [PROPÓSITO]: Transformar la persistencia de Room en modelos de UI/Dominio.
 * [LEY #9]: Nombres consistentes en español.
 */
object EventoMappers {

    /**
     * Mapeo base desde Entidad a Modelo de Dominio.
     */
    fun aUiModel(
        entidad: EventoEntity,
        nombreLocal: String? = null
    ): EventoDominio {
        val (colorHex, emoji) = when (entidad.tipo) {
            TipoEvento.VISITA_TECNICA -> 0xFF00E5FF to "🧰"
            TipoEvento.TURNO_CITA -> 0xFFA78BFA to "🗓️"
            TipoEvento.ENVIO_FLETE -> 0xFFFACC15 to "📦"
            TipoEvento.BLOQUEO_ADMIN -> 0xFFEF4444 to "🔒"
        }

        return EventoDominio(
            id = entidad.id,
            titulo = entidad.titulo.ifBlank { obtenerTituloPorDefecto(entidad.tipo) },
            descripcion = entidad.descripcion,
            fechaTexto = CalendarUtils.formatIsoDate(entidad.fechaInicioUtc),
            horaTexto = CalendarUtils.formatTime(entidad.fechaInicioUtc),
            tipo = entidad.tipo,
            estado = entidad.estado,
            direccion = entidad.direccion,
            colorAcentoHex = colorHex,
            emojiTipo = emoji,
            idParticipante = entidad.idCliente,
            nombreParticipante = entidad.nombreSucursal ?: nombreLocal ?: "Sucursal",
            idChat = entidad.idChat,
            marcaTiempoUtc = entidad.fechaInicioUtc,
            marcaTiempoFinUtc = entidad.fechaFinUtc,
            idRepresentante = entidad.idRepresentante,
            nombreRepresentante = entidad.nombreRepresentante,
            idPresupuestoAsociado = entidad.idPresupuestoAsociado,
            direccionOrigen = entidad.idDireccionOrigen,
            direccionDestino = entidad.idDireccionDestino
        )
    }

    /**
     * 🔥 [ELITE]: Convierte un mensaje de tipo operacional (Turno/Visita/Envío)
     * en una entidad de evento para la agenda local.
     */
    fun deMensajeAEntidad(mensaje: MensajeEntity): EventoEntity? {
        val tipoEvento = when (mensaje.tipo) {
            TipoMensaje.TURNO -> TipoEvento.TURNO_CITA
            TipoMensaje.VISITA -> TipoEvento.VISITA_TECNICA
            TipoMensaje.ENVIO -> TipoEvento.ENVIO_FLETE
            else -> return null
        }

        val timestampInicio = CalendarUtils.convertToUtc(mensaje.fechaCita, mensaje.horaCita)
        val timestampFin = timestampInicio + (3600 * 1000) // Default 1 hora

        return EventoEntity(
            id = mensaje.id, // El ID del evento es el ID del mensaje para consistencia SSOT
            idSucursal = mensaje.idReferencia ?: "",
            idPropietarioSucursal = mensaje.idPropietarioEmisor,
            idCliente = mensaje.idPropietarioReceptor,
            idChat = mensaje.idChat,
            idRecurso = mensaje.idReferencia, // TODO: Refinar si idReferencia es Sucursal o Recurso
            tipo = tipoEvento,
            estado = mappingEstado(mensaje.estadoCita),
            titulo = mensaje.nombreRecurso ?: obtenerTituloPorDefecto(tipoEvento),
            descripcion = "Generado desde Chat",
            direccion = mensaje.direccionCitaOverride ?: "",
            precioTotal = mensaje.precioReferencia ?: 0.0,
            fechaInicioUtc = timestampInicio,
            fechaFinUtc = timestampFin,
            nombreRecurso = if (mensaje.tipo == TipoMensaje.VISITA) mensaje.nombreRecurso else null,
            idPresupuestoAsociado = mensaje.idPresupuestoAsociado
        )
    }

    private fun mappingEstado(estado: String?): EstadoEvento {
        return when (estado?.uppercase()) {
            "ACEPTADO", "ACCEPTED" -> EstadoEvento.CONFIRMADO
            "RECHAZADO", "REJECTED" -> EstadoEvento.CANCELADO
            else -> EstadoEvento.SOLICITADO
        }
    }

    private fun obtenerTituloPorDefecto(tipo: TipoEvento) = when (tipo) {
        TipoEvento.VISITA_TECNICA -> "Visita Técnica"
        TipoEvento.TURNO_CITA -> "Turno en Local"
        TipoEvento.ENVIO_FLETE -> "Envío de Producto"
        TipoEvento.BLOQUEO_ADMIN -> "Espacio Bloqueado"
    }
}
