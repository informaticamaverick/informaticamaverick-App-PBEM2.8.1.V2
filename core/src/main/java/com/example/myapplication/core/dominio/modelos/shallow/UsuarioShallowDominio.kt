package com.example.myapplication.core.dominio.modelos.shallow

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO: USUARIO SHALLOW (v2026.ELITE) ---
 * [PROPÓSITO]: Identidad mínima de un cliente para ser embebida en concursos.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class UsuarioShallowDominio(
    val id: String = "",
    val nombreVisible: String = "",
    val urlMiniatura: String? = null,
    val reputacion: Float = 0f,
    val estaEnLinea: Boolean = false,
    val estaSuscrito: Boolean = false // 🔥 [ELITE]: Relevante para prioridad en concursos
)
