package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- APP METADATA ENTITY (Táctica) ---
 * [PROPÓSITO]: Almacenar pares clave-valor internos de la base de datos.
 * Se utiliza para rastrear versiones de sembrado y estados de integridad.
 */
@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val clave: String,
    val valor: String,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

































