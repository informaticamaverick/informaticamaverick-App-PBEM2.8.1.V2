package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO PRESTADOR COMPLETO (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Agrupa la identidad del profesional con sus activos personales.
 */
@Keep
data class PrestadorDominioCompleto(
    val perfil: PrestadorDominio,
    val direcciones: List<DireccionDominio> = emptyList(),
    val horario: HorarioDominio? = null,
    val reseñas: List<ReseñaDominio> = emptyList()
)

