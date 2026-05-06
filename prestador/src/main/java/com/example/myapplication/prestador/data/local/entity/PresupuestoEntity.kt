package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "presupuestos",
    // TEMPORALMENTE DESACTIVADO: Foreign Keys causan crash si no existe el Provider
    // TODO: Re-activar cuando implementemos login y tengamos providerId real
    /*
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["prestadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    */
    indices = [
        Index(value = ["clienteId"]),
        Index(value = ["prestadorId"]),
        Index(value = ["clienteId", "estado"])
    ]
)
data class PresupuestoEntity(
    @PrimaryKey val id: String,
    val numeroPresupuesto: String,
    val clienteId: String,
    val prestadorId: String,
    val fecha: String,
    val validezDias: Int = 7,
    val subtotal: Double,
    val impuestos: Double,
    val total: Double,
    val estado: String,
    val notas: String = "",
    val tituloTrabajo: String = "",
    val itemsJson: String = "",
    val serviciosJson: String = "",
    val honorariosJson: String = "",
    val gastosJson: String = "",
    val impuestosJson: String = "",
    val appointmentId: String? = null,
    val firestoreId: String? = null,
    val syncedAt: Long? = null,
    val categorias: String = "",
    val providerCompanyName: String? = null, // 🔥 [NUEVO] Nombre legible de la empresa
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)