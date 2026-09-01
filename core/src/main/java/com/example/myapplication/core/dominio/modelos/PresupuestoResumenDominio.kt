package com.example.myapplication.core.dominio.modelos

import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto

/**
 * --- MODELO DE RESUMEN DE PRESUPUESTOS (Ley #3 - Shallow) ---
 * [ELITE 2026]: Evita cargar listas pesadas en memoria durante el scroll.
 * Se ubica en Core para ser compartido entre todos los módulos.
 */
data class PresupuestoResumenDominio(
    val idPresupuesto: String,
    val numeroPresupuesto: String?,
    val tituloTrabajo: String?,
    val totalGeneral: Double,
    val estado: EstadoPresupuesto,
    val fechaTimestamp: Long,
    val esLeido: Boolean,
    val idPrestador: String,
    val nombrePrestador: String,
    val fotoPrestador: Any?,
    val urlMiniatura: String? = null,
    val idConcurso: String? = null,
    val estaSuscrito: Boolean = false,
    val idCategoria: String? = null,
    val nombreCategoria: String? = null,
    val iconoCategoria: String? = null
)



































