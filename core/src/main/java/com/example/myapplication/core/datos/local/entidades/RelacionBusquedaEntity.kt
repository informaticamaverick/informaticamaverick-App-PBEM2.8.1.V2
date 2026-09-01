package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.annotation.Keep

/**
 * --- ENTIDAD DE RELACIÓN DE BÚSQUEDA (v2026.ELITE) ---
 * [PROPÓSITO]: Vincular una consulta específica (idConsulta) con los resultados shallow.
 * [LEY #6]: Soberanía del Cliente. Esta tabla reside en Core para evitar colisiones Room.
 */
@Keep
@Entity(
    tableName = "relaciones_busqueda",
    primaryKeys = ["idConsulta", "idPrestador"],
    indices = [Index(value = ["idConsulta"])]
)
data class RelacionBusquedaEntity(
    val idConsulta: String,  // Ej: Huella P_CP_Rubro o G_Hash_Rubro
    val idPrestador: String, // ID del experto o sucursal
    val ordenRanking: Int = 0,
    val fechaCreacion: Long = System.currentTimeMillis()
)
