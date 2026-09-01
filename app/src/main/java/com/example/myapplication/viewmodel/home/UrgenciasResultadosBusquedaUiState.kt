package com.example.myapplication.viewmodel.home

import androidx.compose.ui.graphics.Color
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio

/**
 * --- ESTADO DE UI: URGENCIAS RESULTADOS BÚSQUEDA (v2026.RADAR.PRO) ---
 * [PROPÓSITO]: Estado atómico para la pantalla de respuesta inmediata.
 * [LEY #1]: Pantalla Tonta.
 */

enum class SearchMode { IDLE, SEARCHING, RESULTS }
enum class ViewMode { RADAR, LIST, ASSISTANT }

data class ProviderUiModel(
    val id: String,
    val name: String,
    val rating: String,
    val reviewCount: Int,
    val distance: String,
    val eta: String,
    val estPrice: String,
    val category: String,
    val isOnline: Boolean,
    val badge: String,
    val imageUrl: Any? = null,
    val latOffsetDp: Float,
    val lonOffsetDp: Float,
    val tags: List<String>
)

data class UrgenciasResultadosBusquedaUiState(
    // --- MODOS DE VISTA Y BÚSQUEDA ---
    val searchMode: SearchMode = SearchMode.IDLE,
    val viewMode: ViewMode = ViewMode.RADAR,
    
    // --- DATOS DE CATEGORÍA Y FILTROS ---
    val rubroSeleccionado: CategoriaDominio? = null,
    val subcategoriaSeleccionada: String? = null,
    val filtrosActivos: Set<String> = emptySet(),
    
    // --- CONTEXTO DE USUARIO (Soberanía) ---
    val direccionActiva: DireccionDominio? = null,
    val estaGpsActivo: Boolean = false,
    val isCargandoUbicacion: Boolean = false,
    val fotoPerfil: Any? = null,
    val nombrePerfilActivo: String = "",
    val ecosistemaMaestro: com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario? = null,
    
    // --- RESULTADOS ---
    val prestadoresDetectados: List<PrestadorDominio> = emptyList(),
    val prestadoresRadar: List<ProviderUiModel> = emptyList(),
    val rubrosMasUsados: List<CategoriaDominio> = emptyList(), 
    val isCargandoRubros: Boolean = true,
    val consultaFiltro: String = "", // 🔥 [NEW]
    
    // --- INTERACCIÓN ---
    val mostrarMenuUbicacion: Boolean = false,
    val mostrarMenuPerfil: Boolean = false,
    val proveedorParaDetalle: ProviderUiModel? = null,
    val toastMensaje: Pair<String, String>? = null
)
