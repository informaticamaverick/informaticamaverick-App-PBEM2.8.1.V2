package com.example.myapplication.core.dominio.modelos

import androidx.compose.runtime.Immutable

/**
 * --- CATEGORY UI STATE (ELITE SSOT v2026) ---
 * [PROPÓSITO]: Representar el estado único y atómico de la gestión de rubros.
 * [UBICACIÓN]: CORE - Para que tanto el Cliente como el Prestador tengan paridad de datos.
 * [LEY #1]: Pantallas Tontas. Una sola fuente de verdad.
 */
@Immutable
data class CategoryUiState(
    val estaHojaVisible: Boolean = false,
    val superCategoriaSeleccionada: SuperCategoriaDominio? = null,
    val superCategoriasFiltradas: List<FilaSuperCategoriaDominio> = emptyList(),
    val categoriasFiltradas: List<FilaCategoriaDominio> = emptyList(),
    val categoriasPlanas: List<CategoriaDominio> = emptyList(), 
    val cantidadResultados: Int = 0,
    val estaCargando: Boolean = true,
    val filtrosOrden: Set<String> = setOf("view_bento", "sort_hot"),
    val idsFavoritos: Set<String> = emptySet(),
    
    // 🔥 [v2026.ELITE]: Soporte para multiselección táctica
    val estaEnModoSeleccionSuper: Boolean = false,
    val idsSuperSeleccionados: Set<String> = emptySet(),

    // 🔥 [v2026.ELITE]: Gestión de detalles de categoría
    val categoriaParaDetalle: CategoriaDominio? = null
)
