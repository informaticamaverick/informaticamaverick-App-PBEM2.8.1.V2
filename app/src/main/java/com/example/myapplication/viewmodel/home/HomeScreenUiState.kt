package com.example.myapplication.viewmodel.home

import androidx.compose.runtime.Immutable
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.CategoryUiState
import com.example.myapplication.uishared.ui.modelos.AccordionBanner

/**
 * --- HOME SCREEN UI STATE (ELITE SSOT v2026) ---
 * [PROPÓSITO]: Representar el estado único, atómico y tonto de la HomeScreen.
 * [LEY #1]: Pantallas Tontas. Una sola fuente de verdad para la composición.
 */
@Immutable
data class HomeScreenUiState(
    // --- 1. SECTOR IDENTIDAD Y PERFIL ---
    val nombrePerfilActivo: String = "Detectando...",
    val fotoPerfilActivo: Any? = null,
    val idPerfilSeleccionado: String? = null,
    val estaVerificado: Boolean = false,
    val esSuscripto: Boolean = false,
    val conteoNoLeidos: Int = 0,

    // --- 2. SECTOR UBICACIÓN ---
    val direccionActiva: DireccionDominio? = null,
    val estaGpsActivado: Boolean = false,
    val estaCargandoUbicacion: Boolean = false,
    val direccionesDisponibles: List<DireccionDominio> = emptyList(),
    val mostrarMenuUbicacion: Boolean = false,

    // --- 3. SECTOR CLIMA ---
    val temperatura: String = "--°C",
    val emojiClima: String = "⌛",
    val descripcionClima: String = "Cargando...",
    val nombreCiudad: String = "Localizando...",
    val mostrarDetallesClima: Boolean = false,

    // --- 4. SECTOR DESCUBRIMIENTO (CATEGORÍAS) ---
    val categoriaState: CategoryUiState = CategoryUiState(),
    val estaRefrescando: Boolean = false,
    val animacionBusquedaFinalizada: Boolean = true,
    
    // --- 5. SECTOR MARKETING ---
    val itemsBanner: List<AccordionBanner> = emptyList(),

    // --- 6. SECTOR HUD Y ASISTENTE ---
    val estaBuscando: Boolean = false,
    val consultaBusqueda: String = "",
    val estaMenuLateralAbierto: Boolean = false,
    val estaPanelFavoritosAbierto: Boolean = false,

    // --- 7. SECTOR MULTISELECCIÓN (v2026) ---
    val modoMultiseleccion: Boolean = false,
    val idsSeleccionados: Set<String> = emptySet()
)
