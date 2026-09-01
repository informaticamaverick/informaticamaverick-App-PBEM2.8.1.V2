package com.example.myapplication.ui.modelos.comercial

import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity

/**
 * --- MODELO DE DOMINIO: CONCURSO CON OFERTAS (v2026.CLIENTE) ---
 * [PROPÓSITO]: Agrupar un proyecto con sus presupuestos para la vista de detalle.
 * [LEY #12]: Soberanía de Datos. Este archivo NO se comparte con el Prestador.
 */
data class ConcursoPublicoConOfertas(
    val concurso: ConcursoPublicoEntity,
    val presupuestos: List<PresupuestoFinalEntity> = emptyList()
)

