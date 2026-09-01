package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO USUARIO COMPLETO (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Agrupa la identidad del cliente con sus direcciones personales.
 */
@Keep
data class UsuarioDominioCompleto(
    val perfil: UsuarioDominio,
    val direcciones: List<DireccionDominio> = emptyList()
)
