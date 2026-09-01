package com.example.myapplication.viewmodel.home

import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.UsuarioDominio

/**
 * --- ESTADO DE UI: RESULTADO BÚSQUEDA PRESTADOR (v2026.ELITE) ---
 * [PROPÓSITO]: Representar la "foto" completa de lo que el usuario ve en la pantalla.
 * [LEY #1]: Pantalla Tonta. Este objeto contiene todo lo necesario para renderizar.
 */
data class ResultadoBusquedaPrestadorUiState(
    // --- DATOS DEL RUBRO ---
    val idRubro: String? = null,
    val rubroInfo: CategoriaDominio? = null,
    
    // --- CONTEXTO DEL USUARIO (Soberanía) ---
    val perfilUsuario: UsuarioDominio? = null,
    val direccionSeleccionada: DireccionDominio? = null,
    val codigoPostalActual: String = "",
    val direccionVisible: String = "Cargando ubicación...",
    val estaGpsActivo: Boolean = false,
    val isCargandoUbicacion: Boolean = false,
    
    // --- ESTADOS DE CARGA Y ERROR ---
    val estaCargando: Boolean = false,
    val mensajeError: String? = null,
    
    // --- FILTRADO TÁCTICO ---
    val filtros: FiltrosBúsqueda = FiltrosBúsqueda(),
    
    // --- ESTADOS VISUALES (INTERACCIÓN) ---
    val mostrarMenuPerfil: Boolean = false,
    val mostrarMenuUbicacion: Boolean = false,
    val menuFiltrosAbierto: String? = null, // "filtros", "ordenar" o null
    
    // --- FAVORITOS ---
    val idsFavoritos: Set<String> = emptySet()
)

/**
 * Representa los interruptores de filtrado táctico.
 */
data class FiltrosBúsqueda(
    val solo24h: Boolean = false,
    val soloVerificados: Boolean = false,
    val conEnvio: Boolean = false,
    val estaOnline: Boolean = false,
    val orden: String = "reciente" // reciente, reputacion, cercania
)

