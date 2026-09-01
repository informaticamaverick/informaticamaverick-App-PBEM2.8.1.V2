package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Tipos de Eventos Maverick
 */
@Keep
enum class TipoEvento {
    VISITA_TECNICA,
    TURNO_CITA,
    ENVIO_FLETE,
    BLOQUEO_ADMIN
}

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Estados de Eventos Maverick
 */
@Keep
enum class EstadoEvento {
    SOLICITADO,    // Enviado por prestador, espera confirmación
    CONFIRMADO,    // Aceptado por cliente
    EN_CAMINO,     // Prestador en viaje
    EN_PROCESO,    // Ejecutándose
    COMPLETADO,    // Finalizado
    CANCELADO,     // Anulado
    REPROGRAMADO   // Cambio de fecha/hora
}

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Entidad de Eventos Unificada (SSOT 2026)
 * [PROPÓSITO]: Reemplazar a CalendarEventEntity para garantizar paridad y variables en español.
 */
@Keep
@Entity(
    tableName = "eventos",
    indices = [
        Index(value = ["fechaInicioUtc"]),
        Index(value = ["idSucursal"]),
        Index(value = ["idCliente"]),
        Index(value = ["idChat"]),
        Index(value = ["idCategoria"])
    ]
)
data class EventoEntity(
    @PrimaryKey val id: String = "", // Global UID

    // --- TIEMPO ---
    val fechaInicioUtc: Long = 0L,
    val fechaFinUtc: Long = 0L,

    // --- VÍNCULOS ---
    val idSucursal: String = "",
    val idPropietarioSucursal: String = "", // UID del prestador
    val idCliente: String = "",             // UID del cliente
    val idChat: String = "",                // Contexto de conversación
    val idRecurso: String? = null,      // ¿Qué habitación/cancha ocupa?
    val idCategoria: String? = null,    // 🔥 [ELITE]: Clave Semántica para filtros

    // --- INFORMACIÓN ---
    val tipo: TipoEvento = TipoEvento.TURNO_CITA,
    val estado: EstadoEvento = EstadoEvento.SOLICITADO,
    val titulo: String = "",
    val descripcion: String = "",
    val direccion: String = "",
    val precioTotal: Double = 0.0,

    // --- SECTOR: ESPECIALIZACIÓN v2026.ELITE ---
    val idRepresentante: String? = null,        // Técnico asignado (Visitas)
    val nombreRepresentante: String? = null,    // Nombre del técnico
    val idPresupuestoAsociado: String? = null,   // Link a presupuesto PDF
    val idDireccionOrigen: String? = null,      // Dirección sucursal (Turnos)
    val idDireccionDestino: String? = null,     // Dirección cliente (Visitas)

    // --- METADATOS ---
    val nombreRecurso: String? = null,
    val nombreSucursal: String? = null,
    val urlFotoSucursal: String? = null,

    // --- AUDITORÍA ---
    val ultimaModificacion: Long = System.currentTimeMillis(),
    val version: Int = 0
)


































