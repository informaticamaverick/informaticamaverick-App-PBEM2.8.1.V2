package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Entidad de Equipo de Trabajo (Persistence)
 * [PROPÓSITO]: Representar al personal humano asignado a una Sucursal en Room.
 * [FUNCIONAMIENTO INTERNO]: Almacena datos descriptivos del staff y vinculación con Sucursal.
 * [RELACIÓN]: Se mapea a 'EquipoTrabajo' (Domain) mediante 'EquipoTrabajoMappers'.
 */
@Keep
@Entity(
    tableName = "equipo_trabajo",
    indices = [
        Index(value = ["idPropietario"]),
        Index(value = ["idSucursal"])
    ]
)
data class EquipoTrabajoEntity(
    @PrimaryKey val id: String = "",
    val idPropietario: String = "",
    val idSucursal: String? = null,

    // --- SECTOR: DATOS BÁSICOS ---
    val nombre: String = "",
    val apellido: String = "",
    val cargo: String = "",
    val detalle: String = "",
    val avatarEmoji: String = "👤",
    val estaHabilitado: Boolean = true,
    val idRecursoVinculado: String? = null,

    // --- SECTOR: AUDITORÍA ---
    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)

