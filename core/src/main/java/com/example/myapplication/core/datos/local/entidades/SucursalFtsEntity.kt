package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4

/**
 * --- ÍNDICE DE BÚSQUEDA TÁCTICA: SUCURSALES (v2026.ELITE) ---
 */
@Keep
@Fts4(contentEntity = SucursalEntity::class)
@Entity(tableName = "sucursales_fts")
data class SucursalFtsEntity(
    val nombre: String,
    val descripcion: String
)
