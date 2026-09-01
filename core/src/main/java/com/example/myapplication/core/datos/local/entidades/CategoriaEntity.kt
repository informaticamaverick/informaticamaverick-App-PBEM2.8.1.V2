package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD DE CATEGORÍA (ROOM - v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 * Representa un rubro o servicio (ej: "Plomería", "Electricidad").
 */
@Keep
@Entity(
    tableName = "categorias",
    indices = [
        Index(value = ["id"]),
        Index(value = ["idSuperCategoria"])
    ]
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowid: Long = 0, // 🔥 [ELITE]: Necesario para compatibilidad perfecta con FTS4/5
    val id: String,      // 🔥 [ELITE]: Clave Semántica (ej: 'SALUD_PEDIATRA')
    val nombre: String,
    val icono: String,
    val idSuperCategoria: String,
    val descripcion: String = "",
    val esNueva: Boolean = false
)
