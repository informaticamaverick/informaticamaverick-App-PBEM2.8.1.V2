package com.example.myapplication.core.datos.local.entidades.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity

/**
 * --- RELACIÓN: CONCURSO PÚBLICO CON PRESUPUESTOS (v2026.ELITE) ---
 * [PROPÓSITO]: Agrupar un folder (Concurso) con sus archivos (Presupuestos).
 * [NOTA]: Uso exclusivo para la App Azul (Cliente) para gestión de ofertas.
 */
data class ConcursoPublicoConPresupuestos(
    @Embedded val concurso: ConcursoPublicoEntity,
    @Relation(
        parentColumn = "idConcurso",
        entityColumn = "idConcurso"
    )
    val presupuestos: List<PresupuestoFinalEntity>
)

