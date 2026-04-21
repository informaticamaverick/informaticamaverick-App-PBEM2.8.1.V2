package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plantillas_presupuesto")
data class PlantillaPresupuestoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val itemsJson: String = "",
    val serviciosJson: String = "",
    val createdAT: Long = System.currentTimeMillis()
)