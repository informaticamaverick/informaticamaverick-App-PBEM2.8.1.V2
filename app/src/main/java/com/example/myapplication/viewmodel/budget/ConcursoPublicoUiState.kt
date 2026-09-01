package com.example.myapplication.viewmodel.budget

import androidx.compose.runtime.Immutable
import com.example.myapplication.core.dominio.modelos.ConcursoPublicoResumenDominio
import com.example.myapplication.core.dominio.filtros.FiltrosConcursoPublico
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3

/**
 * --- CONCURSO PÚBLICO UI STATE (ELITE SSOT v2026) ---
 * [PROPÓSITO]: Representar el estado único, atómico y tonto de la bandeja de concursos.
 * [LEY #1]: Pantallas Tontas. Una sola fuente de verdad para la composición.
 */
@Immutable
data class ConcursoPublicoUiState(
    val concursos: List<ConcursoPublicoResumenDominio> = emptyList(),
    val estaCargando: Boolean = true,
    val estaRefrescando: Boolean = false,
    val filtros: FiltrosConcursoPublico = FiltrosConcursoPublico(),
    
    // --- Sector: Menús y Filtros ---
    val itemsFiltro: List<DropdownItemData> = emptyList(),
    val itemsOrden: List<DropdownItemData> = emptyList(),
    val itemsCategoria: List<DropdownItemData> = emptyList(),
    val todasLasCategorias: List<CategoriaEntity> = emptyList(),
    
    // --- Sector: Perfiles e Identidad ---
    val perfiles: List<PerfilIdentidadV3> = emptyList(),
    val idPerfilSeleccionado: String = "personal",
    val mostrarMenuPerfil: Boolean = false,

    // --- Sector: Multiselección ---
    val estaMultiseleccion: Boolean = false,
    val idsSeleccionados: Set<String> = emptySet(),
    val totalItems: Int = 0 // 🔥 [NEW v2026.ELITE]: Para validación de Select All
)


