package com.example.myapplication.core.datos.local.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity

data class PrestadorConDireccionesRelacionesBD(
    @Embedded val prestador: IdentidadPrestadorEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "idPropietario"
    )
    val direcciones: List<DireccionEntity>
)
