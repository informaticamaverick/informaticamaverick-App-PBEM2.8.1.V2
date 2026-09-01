package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * --- ENTIDAD: EXCEPCIÓN HORARIA (v2026.RESOURCES) ---
 * [PROPÓSITO]: Bloquear días o rangos específicos (vacaciones, feriados, emergencias).
 */
@Keep
@Entity(
    tableName = "excepciones_horarias",
    indices = [Index(value = ["idReferencia"])]
)
data class ExcepcionHorariaEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val idReferencia: String, // ID de Sucursal, Recurso o Personal
    val fechaLong: Long,      // Fecha del bloqueo
    val motivo: String = "",
    val estaCerrado: Boolean = true, // Cierre total del día
    val inicioParcial: String? = null,
    val finParcial: String? = null
)
