package com.example.myapplication.prestador.viewmodel.profile

import com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo
import com.example.myapplication.core.dominio.modelos.CategoriaDominio

/**
 * --- ESTADO DE UI: PERFIL DEEP (v2026.ELITE) ---
 * [PROPÓSITO]: Representar la vista del perfil profesional con reactividad total.
 */
data class PerfilPrestadorDeepUiState(
    val ecosistema: PerfilPrestadorDeepModelo? = null,
    val estaCargando: Boolean = true,
    val hayCambiosPendientes: Boolean = false,
    val estaDetectandoGps: Boolean = false,
    val todasLasCategorias: List<CategoriaDominio> = emptyList(),
    val error: String? = null,
    val mensajeExito: String? = null
)
