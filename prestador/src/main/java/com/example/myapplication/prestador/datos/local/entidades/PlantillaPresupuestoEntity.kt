package com.example.myapplication.prestador.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.Keep

/**
 * --- CABECERA DE PLANTILLA - COCINA PRIVADA (v2026.ELITE) ---
 */
@Keep
@Entity(tableName = "plantillas_presupuesto")
data class PlantillaPresupuestoEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val nombre: String = "",
    val idCategoria: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)
