package com.example.myapplication.core.dominio.modelos.shallow

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO: EMPRESA SHALLOW (v2026.ELITE) ---
 * [PROPÓSITO]: Identidad mínima de una marca comercial.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class EmpresaShallowDominio(
    val id: String = "",
    val idPropietario: String = "",
    val nombre: String = "",
    val urlMiniatura: String? = null,
    val reputacion: Float = 0f,
    val estaVerificada: Boolean = false,
    val estaSuscrito: Boolean = false, // 🔥 [ELITE]
    val idCategorias: List<String> = emptyList()
)
