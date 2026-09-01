package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO EMPRESA (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa la entidad legal pura para lógica de negocio.
 */
@Keep
data class EmpresaDominio(
    val id: String = "",
    val idPropietario: String = "",
    val nombre: String = "",
    val razonSocial: String = "",
    val descripcion: String = "",
    val cuit: String = "",
    val correoContacto: String = "",
    val urlFoto: Any? = null,
    val urlMiniatura: Any? = null,
    val idCategorias: List<String> = emptyList(),
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosRealizados: Int = 0,
    val nivelElite: Int = 0,
    val estaVerificada: Boolean = false,
    val estaSuscrito: Boolean = false
)
