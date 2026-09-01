package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELO DE USUARIO PARA BÚSQUEDA / CHAT (Atómico) ---
 * 
 * [PROPÓSITO]: Representar los datos mínimos necesarios para que un Prestador
 * identifique a un Cliente en su lista de mensajes o licitaciones (Carga Shallow).
 */
data class UsuarioBusqueda(
    val id: String, // UID de Firebase
    val nombreVisible: String,
    val urlMiniatura: String? = null,
    val estaEnLinea: Boolean = false,
    
    // --- CONTROL DE PROFUNDIDAD ---
    val esCargaCompleta: Boolean = false
)

































