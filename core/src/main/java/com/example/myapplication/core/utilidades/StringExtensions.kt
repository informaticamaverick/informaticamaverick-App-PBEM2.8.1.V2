package com.example.myapplication.core.utilidades

import java.text.Normalizer
import java.util.Locale

/**
 * --- app STRING EXTENSIONS (CORE) ---
 * Centralización de toda la lógica de manipulación de cadenas de la plataforma.
 */

// --- SECCIÓN 1: NORMALIZACIÓN Y LIMPIEZA ---

/**
 * Elimina acentos y caracteres diacríticos de un string.
 */
fun String.removeAccents(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
}

/**
 * Formatea un texto para visualización estándar: minúsculas con primera letra en mayúscula.
 */
fun String.formatearTexto(quitarAcentos: Boolean = false): String {
    if (this.isBlank()) return this
    val texto = this.trim().lowercase().replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
    return if (quitarAcentos) texto.removeAccents() else texto
}

/**
 * [ELITE] Normalización profunda para búsquedas: quita acentos, símbolos, paréntesis y normaliza a minúsculas.
 */
fun String.normalizeFull(): String {
    return try {
        this.removeAccents()
            .lowercase()
            .replace("(", " ")
            .replace(")", " ")
            .replace("[^a-z0-9 ]".toRegex(), " ") // Elimina cualquier símbolo no alfanumérico
            .replace("\\s+".toRegex(), " ")     // Colapsa múltiples espacios
            .trim()
    } catch (e: Exception) {
        this.lowercase().trim()
    }
}

/**
 * [AUDITORÍA]: Filtro de texto refinado para el Asistente Be.
 * Cumple con la discriminación (omisión) de acentos, paréntesis y mayúsculas.
 */
fun String.filtroDeTexto(query: String): Boolean {
    if (query.isBlank()) return true
    val normalizedSource = this.normalizeFull()
    val normalizedQuery = query.normalizeFull()

    if (normalizedQuery.isEmpty()) return true

    // Implementación de búsqueda por tokens (todas las palabras de la query deben estar presentes)
    val queryTokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
    return queryTokens.all { token ->
        normalizedSource.contains(token)
    }
}

// --- SECCIÓN 2: BÚSQUEDA INTELIGENTE ---

/**
 * Prepara un string para la búsqueda: quita acentos, pasa a minúsculas y recorta espacios.
 */
fun String.prepareForSearch(): String = this.removeAccents().lowercase().trim()

/**
 * Verifica si alguna palabra del texto comienza con la query de forma inteligente.
 */
fun String.wordStartsWithSmart(query: String): Boolean {
    if (query.isEmpty()) return false
    val normQuery = query.prepareForSearch()
    return this.prepareForSearch().split(" ", "(", ")").any { it.startsWith(normQuery) }
}

/**
 * Realiza un matching inteligente: todas las palabras de la query deben ser encontradas
 * como prefijos de alguna palabra en el texto.
 */
fun String.matchesSmart(query: String): Boolean {
    if (query.isEmpty()) return false
    val normQuery = query.prepareForSearch()
    val textWords = this.prepareForSearch().split(" ", "(", ")").filter { it.isNotEmpty() }
    val queryWords = normQuery.split(" ", "(", ")").filter { it.isNotEmpty() }
    
    return queryWords.all { qw ->
        textWords.any { tw -> tw.startsWith(qw) }
    }
}

// --- SECCIÓN 3: INFRAESTRUCTURA (FCM) ---

/**
 * Normaliza un String para usarlo como nombre de topic de Firebase Cloud Messaging.
 * Firebase solo permite: letras, números, guión, punto, guión_bajo, tilde, porcentaje.
 */
fun String.normalizeForTopic(): String {
    return this.removeAccents()
        .replace(Regex("\\s+"), " ") // [ELITE]: Colapsar múltiples espacios antes de convertir
        .trim()
        .replace(" ", "_")
        .replace("(", "")
        .replace(")", "")
        .replace(Regex("[^a-zA-Z0-9-_.~%]"), "")
        .lowercase()
        .take(900) // Firebase topic max length is 900 chars
}


































