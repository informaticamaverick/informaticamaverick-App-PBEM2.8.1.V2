package com.example.myapplication.core.datos.local.entidades.vistas

import androidx.room.Embedded
import com.example.myapplication.core.datos.local.entidades.PromocionEntity

/**
 * --- DTO PARA PROMOCIÓN CON ESTADO DE REACCIÓN ---
 */
data class PromocionDetalle(
    @Embedded val promocion: PromocionEntity,
    val reaccionada: Boolean
)
