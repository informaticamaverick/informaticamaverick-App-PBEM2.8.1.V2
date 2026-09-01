package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.myapplication.core.dominio.modelos.RangoHorarioDominio

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Entidad de Horario (Persistence)
 * [PROPÓSITO]: Representar la estructura de datos para el almacenamiento local de horarios de atención o recursos en Room.
 */
@Keep
@Entity(
    tableName = "horarios",
    indices = [
        Index(value = ["idPropietario"]),
        Index(value = ["idSucursal"]),
        Index(value = ["idReferencia"])
    ]
)
data class HorarioEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val idPropietario: String = "",
    val idSucursal: String? = null,
    val idReferencia: String = "",       // ID del dueño (Sucursal o Recurso) - Compatibilidad
    val idReferenciaPadre: String? = null, // ID de la Sucursal si es horario de recurso - Compatibilidad
    val tipo: TipoHorario = TipoHorario.Horario_Atencion,

    // --- SECTOR: RANGOS POR DÍA ---
    val lunes: List<RangoHorarioDominio> = emptyList(),
    val martes: List<RangoHorarioDominio> = emptyList(),
    val miercoles: List<RangoHorarioDominio> = emptyList(),
    val jueves: List<RangoHorarioDominio> = emptyList(),
    val viernes: List<RangoHorarioDominio> = emptyList(),
    val sabado: List<RangoHorarioDominio> = emptyList(),
    val domingo: List<RangoHorarioDominio> = emptyList(),

    val zonaHoraria: String = "America/Argentina/Buenos_Aires",
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)

