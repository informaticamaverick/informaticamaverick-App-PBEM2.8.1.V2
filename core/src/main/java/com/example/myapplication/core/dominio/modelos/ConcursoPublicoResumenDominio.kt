package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity

/**
 * --- MODELO DE RESUMEN DE CONCURSOS (CLIENTE - v2026.ELITE) ---
 * [PROPÓSITO]: Representar un concurso con sus estadísticas y metadatos de categoría para el cliente.
 * [LEY #10]: Desacoplado de Room (aunque mantiene la entidad para compatibilidad táctica).
 */
@Keep
data class ConcursoPublicoResumenDominio(
    val concursoRaw: ConcursoPublicoEntity,
    val nombreCategoria: String,
    val iconoCategoria: String,
    val totalOfertas: Int,
    val ofertasNoLeidas: Int
) {
    // 🔥 [ELITE]: Propiedades delegadas para facilitar el uso en la UI
    val idConcurso get() = concursoRaw.idConcurso
    val titulo get() = concursoRaw.titulo
    val estado get() = concursoRaw.estado
    val fechaInicio get() = concursoRaw.fechaInicio
    val fechaFin get() = concursoRaw.fechaFin
}
