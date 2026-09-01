package com.example.myapplication.core.dominio.modelos.discovery

import androidx.annotation.Keep
import com.example.myapplication.core.dominio.modelos.shallow.UsuarioShallowDominio

/**
 * --- ÍNDICE DE DESCUBRIMIENTO: CONCURSO SHALLOW (v2026.ELITE) ---
 * [PROPÓSITO]: Agrupar los datos del concurso con la identidad del autor para el índice.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class IndiceConcursoShallowDominio(
    val idConcurso: String = "",
    val idCliente: String = "", // 🔥 [ELITE] Duplicamos UID para Regras de Segurança
    val idPropietario: String = "", // 🔥 [ELITE] Alineación con IndiceBusqueda
    val tipoIdentidad: String = "CONCURSO", // 🔥 [ELITE] Alineación con IndiceBusqueda
    val titulo: String = "",
    val descripcion: String = "",
    val idCategoria: String = "",
    val estado: String = "ABIERTA", // 🔥 [ELITE] Estado de la licitación
    val urlImagenes: List<String> = emptyList(),
    val marcaTiempo: Long = System.currentTimeMillis(),
    val fechaFin: Long = 0L,
    val codigoPostal: String = "",
    val filtrosBusqueda: List<String> = emptyList(),
    
    // --- EL CORAZÓN: IDENTIDAD EMBEBIDA ---
    val autor: UsuarioShallowDominio = UsuarioShallowDominio()
)
