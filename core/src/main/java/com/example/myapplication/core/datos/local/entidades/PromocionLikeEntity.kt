package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity

/**
 * --- REACCIÓN DE LIKE (LOCAL SSOT) ---
 */
@Entity(
    tableName = "reacciones_promo",
    primaryKeys = ["idPromocion", "idUsuario"]
)
data class PromocionLikeEntity(
    val idPromocion: String, 
    val idUsuario: String,
    val leGusta: Boolean, 
    val ultimaActualizacion: Long = System.currentTimeMillis()
)
