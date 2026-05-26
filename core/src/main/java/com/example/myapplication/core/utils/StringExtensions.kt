package com.example.myapplication.core.utils

import java.text.Normalizer

/**
 * Normaliza un String para usarlo como nombre de topic de Firebase Cloud Messaging.
 * Firebase solo permite: letras, números, guión, punto, guión_bajo, tilde, porcentaje.
 * Ej: "Plomería" → "plomeria", "Buenos Aires" → "buenos_aires"
 */
fun String.normalizeForTopic(): String {
    // 1. Quitar acentos y caracteres diacríticos
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    // 2. Minúsculas
    // 3. Espacios → guión bajo, quitar caracteres no válidos
    return normalized
        .lowercase()
        .trim()
        .replace(Regex("\\s+"), "_")
        .replace(Regex("[^a-z0-9\\-_.~%]"), "")
        .take(900) // Firebase topic max length is 900 chars
}
