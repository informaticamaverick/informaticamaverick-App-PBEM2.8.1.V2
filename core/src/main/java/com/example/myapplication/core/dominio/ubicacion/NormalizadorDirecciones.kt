package com.example.myapplication.core.dominio.ubicacion

import com.example.myapplication.core.utilidades.removeAccents

/**
 * --- NORMALIZADOR DE DIRECCIONES (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Purificar entradas de ubicación para evitar divergencias.
 * [LEY #9]: Estándar Maverick en Español.
 */
object NormalizadorDirecciones {

    /**
     * Limpia el Código Postal para que sea numérico puro.
     * [EJEMPLO]: "B4000" -> "4000"
     */
    fun limpiarCodigoPostal(cp: String): String {
        if (cp.isBlank()) return ""
        return cp.replace(Regex("[^0-9]"), "")
    }

    /**
     * Formatea el nombre de una calle para búsqueda (sin acentos, minúsculas).
     */
    fun normalizarCalle(calle: String): String {
        return calle.removeAccents().lowercase().trim()
    }
}
