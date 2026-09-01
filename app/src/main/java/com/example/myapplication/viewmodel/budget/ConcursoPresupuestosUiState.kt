package com.example.myapplication.viewmodel.budget

import androidx.compose.runtime.Immutable
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.dominio.modelos.ConcursoPublicoResumenDominio
import com.example.myapplication.core.dominio.filtros.FiltrosConcursoPublico
import com.example.myapplication.ui.componentes.DropdownItemData

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * --- CONCURSO PRESUPUESTOS UI STATE (ELITE SSOT v2026) ---
 * [PROPÓSITO]: Estado atómico para la pantalla de presupuestos recibidos por licitación.
 * [LEY #1]: Pantalla Tonta. Una sola fuente de verdad.
 */
@Immutable
data class ConcursoPresupuestosUiState(
    val presupuestos: List<PresupuestoResumenDominio> = emptyList(),
    val presupuestosPaginados: Flow<PagingData<PresupuestoResumenDominio>> = flowOf(PagingData.empty()),
    val concursoInfo: ConcursoPublicoResumenDominio? = null,
    val estaCargando: Boolean = true,
    val estaRefrescando: Boolean = false,
    
    // --- Sector: Menús y Filtros ---
    val filtrosActivos: Set<String> = emptySet(),
    val itemsFiltro: List<DropdownItemData> = emptyList(),
    val itemsOrden: List<DropdownItemData> = emptyList(),
    val itemsRubros: List<DropdownItemData> = emptyList(),
    val menuFiltrosAbierto: String? = null,

    // --- Sector: Multiselección ---
    val estaMultiseleccion: Boolean = false,
    val idsSeleccionados: Set<String> = emptySet(),
    val totalItems: Int = 0 // 🔥 [NEW v2026.ELITE]: Para validación de Select All
)
