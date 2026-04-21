package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *Entidad que representa un referente/ integrandte del equipo de trabajo.
 * Puede estar asociado a na empresa (Casa Central) o a una sucursal.
 */

@Entity(tableName = "referentes")
data class ReferenteEntity(
    @PrimaryKey
    val id: String,
    val providerId: String,
    val nombre: String,
    val apellido: String? = null,
    val cargo: String? = null,
    val imageUrl: String? = null,
    val empresaId: String? = null,
    val sucursalId: String? = null,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)