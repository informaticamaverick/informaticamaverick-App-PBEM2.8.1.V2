package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD PARA PERSISTENCIA DE CATEGORÍAS FAST (v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Entity(tableName = "uso_categorias_fast")
data class CategoriaRapidaEntity(
    @PrimaryKey 
    val id: String, // 🔥 [ELITE]: Clave Semántica
    val idSuperCategoria: String,
    val conteoUso: Int = 1,
    val marcaTiempoUltimoUso: Long = System.currentTimeMillis()
)
