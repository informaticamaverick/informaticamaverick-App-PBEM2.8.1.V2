package com.example.myapplication.data.utils

import java.text.Normalizer

/**
 * --- SEARCH UTILS ---
 * Utilidades para el procesamiento de strings y búsqueda inteligente.
 * Centraliza la lógica de normalización y matching para ser usada por los ViewModels de búsqueda.
 */
object SearchUtils {

    /**
     * Elimina acentos y caracteres diacríticos de un string.
     */
    fun String.removeAccents(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
    }

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
        // Split por espacios y paréntesis
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
}
