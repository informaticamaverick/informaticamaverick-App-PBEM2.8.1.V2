package com.example.myapplication.core.dominio.modelos

import androidx.compose.runtime.Immutable

/**
 * --- MODELO DE DOMINIO SUPER CATEGORÍA (SSOT 2026) ---
 * [LEY #3]: Ligero para carga inicial ultra-rápida.
 */
@Immutable
data class SuperCategoriaDominio(
    val id: String, // 🔥 [ELITE]: Clave Semántica (ej: 'SALUD')
    val titulo: String,
    val icono: String,
    val color: Long,
    val totalItems: Int,
    val tieneFavoritos: Boolean = false
)

/**
 * --- MODELO DE DOMINIO CATEGORÍA (SSOT 2026) ---
 * [LEY #10]: Detalle completo del rubro cargado bajo demanda.
 */
@Immutable
data class CategoriaDominio(
    val id: String, // 🔥 [ELITE]: Clave Semántica (ej: 'SALUD_PEDIATRA')
    val nombre: String,
    val icono: String,
    val idSuperCategoria: String,
    val superCategoria: String,
    val descripcion: String = "",
    val esNueva: Boolean = false,
    val color: Long = 0xFF1A1F26 // Heredado de Supercategoría
)

data class FilaSuperCategoriaDominio(val elementos: List<SuperCategoriaDominio>)
data class FilaCategoriaDominio(val elementos: List<CategoriaDominio>)
