package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Entidad de Recurso (Persistence)
 * [PROPÓSITO]: Almacenamiento local de activos (Canchas, Consultorios, Personal) en Room.
 */
@Keep
@Entity(
    tableName = "recursos",
    indices = [
        Index(value = ["idPropietario"]),
        Index(value = ["idSucursal"])
    ]
)
data class RecursoEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val idPropietario: String = "",
    val idSucursal: String? = null,

    // --- SECTOR: DATOS DEL RECURSO ---
    val nombre: String = "",
    val descripcion: String = "",
    val precioBase: Double = 0.0,
    val tipoRecurso: String = "GENERICO",
    val capacidadMaxima: Int = 1,
    
    // --- SECTOR: CONTROL TÁCTICO (SUPREME v2026.RESOURCES) ---
    val estaHabilitado: Boolean = true, // Switch ON/OFF solicitado por el usuario
    val requiereHorarioPropio: Boolean = false, // True = Usa su propio HorarioEntity
    val visibilidad: VisibilidadRecurso = VisibilidadRecurso.PRIVADO, // 🔥 [ELITE_SCHEDULING]

    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)

