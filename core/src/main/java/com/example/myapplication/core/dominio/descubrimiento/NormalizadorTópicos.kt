package com.example.myapplication.core.dominio.descubrimiento

/**
 * --- NORMALIZADOR DE TÓPICOS (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Asegurar que los IDs Técnicos sean compatibles con Firebase FCM.
 * [LEY #17]: Ya no limpia nombres visuales. Solo sanitiza IDs.
 */
object NormalizadorTópicos {

    /**
     * Sanitiza un ID de Categoría o SuperCategoría para su uso en red.
     * Firebase Topic Regex: [a-zA-Z0-9-_.~%]{1,900}
     */
    fun normalizar(idTecnico: String): String {
        if (idTecnico.isBlank()) return ""
        
        // Los IDs técnicos suelen ser constantes (ej: SALUD_PEDIATRA)
        // Solo eliminamos cualquier carácter prohibido por seguridad.
        return idTecnico
            .trim()
            .replace(" ", "_") // Por si acaso algún ID trae espacios
            .replace(Regex("[^a-zA-Z0-9-_.~%]"), "") 
            .lowercase() // Estandarizamos a minúsculas por convención de red
            .take(900) 
    }
}
