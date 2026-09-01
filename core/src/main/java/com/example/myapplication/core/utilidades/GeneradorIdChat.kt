package com.example.myapplication.core.utilidades

/**
 * --- GENERADOR DE ID DE CHAT (Simétrico) ---
 * [PROPÓSITO]: Asegurar que el ID de chat entre dos actores sea determinista.
 */
object GeneradorIdChat {
    fun generar(id1: String, id2: String): String {
        return if (id1 < id2) "${id1}_$id2" else "${id2}_$id1"
    }
}

































