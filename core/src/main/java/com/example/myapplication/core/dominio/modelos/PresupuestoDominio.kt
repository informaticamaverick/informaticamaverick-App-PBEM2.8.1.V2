package com.example.myapplication.core.dominio.modelos

import com.example.myapplication.core.datos.local.entidades.*

/**
 * --- MODELO DE INTERFAZ DE PRESUPUESTO (DETALLE - v2026.ELITE) ---
 * [PROPÓSITO]: Proveer una estructura formateada para diálogos y pantallas de detalle.
 * [LEY #10]: UI Stateless.
 */
data class PresupuestoDominio(
    val id: String,
    val numero: String,
    val titulo: String,
    val prestadorNombre: String,
    val empresaNombre: String?,
    val idCategoria: String? = null, // 🔥 [ELITE]: Clave Semántica
    val categoria: String? = null, // Nombre visual (Opcional)
    val estado: EstadoPresupuesto,
    val articulos: List<ArticuloPresupuesto> = emptyList(),
    val servicios: List<ServicioPresupuesto> = emptyList(),
    val subtotalTexto: String,
    val totalTexto: String,
    val fechaTexto: String,
    val notaLegal: String?,
    val tipo: TipoPresupuesto = TipoPresupuesto.NUEVO,
    val esMio: Boolean = false
)


