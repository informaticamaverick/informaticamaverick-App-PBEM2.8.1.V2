package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidadd de base de datos: Sucursal
 * Guarda las sucursales del negocio del prestaor
 */
@Entity(
    tableName = "sucursales",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["businessId"])]
)

data class SucursalEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val nombre: String,
    val telefono: String? = null,
    val email: String? = null,
    val horario: String? = null,

    //Direccion ( referencia a DireccionEntity)
    val direccionId: String? = null,

    //Refrente de la sucursal (referencia a ReferenteEntity)
    val referenteId: String? = null,

    val isActive: Boolean = true,

    // Características de la sucursal (Replicado de BranchProvider)
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val rating: Float = 0f,
    val galleryImages: String = "[]", // JSON list of strings

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()

)