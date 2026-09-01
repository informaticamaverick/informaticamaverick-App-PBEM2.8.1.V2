package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * --- ENTIDAD DE PERSISTENCIA: MENSAJE (ROOM) ---
 * [ELITE v2026.FINAL]: Almacena el historial de chat localmente.
 * [LEY #9]: Nombres en español y tipos Maverick.
 */
enum class TipoMensaje {
    TEXTO, IMAGEN, AUDIO, UBICACION, PRESUPUESTO, TURNO, VISITA, ENVIO, 
    SYSTEM, TENDER_INVITATION, BUDGET_REQUEST, APPOINTMENT_RECEIPT,
    FINALIZACION_TRABAJO, PRODUCTO
}

enum class TipoAvisoEstado {
    COMPLETADO, CANCELADO, REPROGRAMADO
}

enum class EstadoMensaje {
    ENVIANDO, ENVIADO, ENTREGADO, LEIDO, ERROR
}

@Entity(
    tableName = "mensajes",
    indices = [
        Index(value = ["idChat"]),
        Index(value = ["marcaTiempo"])
    ]
)
data class MensajeEntity(
    @PrimaryKey val id: String,
    val idChat: String,
    val idEmisor: String,
    val idReceptor: String,
    val idPropietarioEmisor: String,
    val idPropietarioReceptor: String,
    val tipo: TipoMensaje,
    val contenido: String,
    
    // --- Sector: Multimedia y Ubicación ---
    val urlMedia: String? = null,
    val miniaturaBase64: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val direccionTexto: String? = null,
    val duracionSegundos: Int? = null,
    
    // --- Sector: Referencias y Negocio ---
    val idReferencia: String? = null, // ID de Recurso (Turno) o Propósito General
    val idPresupuestoAsociado: String? = null, // ID de Presupuesto vinculado
    val precioReferencia: Double? = null,
    val idCategoria: String? = null, // 🔥 [ELITE]: Clave Semántica
    val subtipoOperativo: String? = null, // 🔥 [NEW v2026.ELITE] PRODUCTO, SERVICIO, etc.
    
    // --- Sector: Citas y Turnos ---
    val estadoCita: String? = null,
    val fechaCita: String? = null,
    val horaCita: String? = null,
    val direccionCitaOverride: String? = null,
    val codigoVerificacion: String? = null,
    val nombreRecurso: String? = null,
    val urlFotoRecurso: String? = null, // 🔥 [NEW v2026.ELITE]
    val cargoRecurso: String? = null,    // 🔥 [NEW v2026.ELITE]
    val esVisitaTecnica: Boolean? = null,
    
    // --- Sector: Respuesta y Relación ---
    val nombreEmisorRespuesta: String? = null,
    val respondidoAId: String? = null,
    val respondidoAContenido: String? = null,
    
    // --- Sector: Estado y Control ---
    val estado: EstadoMensaje = EstadoMensaje.ENVIANDO,
    val marcaTiempo: Long = System.currentTimeMillis(),
    val esMio: Boolean = true
) {
    // --- Aliases de compatibilidad para UI ---
    val idRelacionado: String? get() = idReferencia
    val fechaEnvio: Long get() = marcaTiempo
    val esLeido: Boolean get() = estado == EstadoMensaje.LEIDO
    val estaSincronizado: Boolean get() = estado != EstadoMensaje.ENVIANDO && estado != EstadoMensaje.ERROR
}



































