package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD DE SUPERCATEGORÍA (ROOM - v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Keep
@Entity(
    tableName = "super_categorias",
    indices = [
        Index(value = ["nombre"])
    ]
)
data class SuperCategoriaEntity(
    @PrimaryKey
    val id: String, // 🔥 [ELITE]: Clave Semántica (ej: 'SALUD')
    val nombre: String,
    val icono: String,
    val color: Long, 
    val descripcion: String = ""
)

































