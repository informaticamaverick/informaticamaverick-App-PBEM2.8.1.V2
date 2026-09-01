package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELO DE DOMINIO: RESEÑA MAVERICK (Ley #9) ---
 * Representa una opinión certificada entre miembros del ecosistema.
 */
data class ReseñaDominio(
    val id: String = "",
    val idAutor: String = "",
    val nombreAutor: String = "",
    val fotoAutorUrl: String? = null,
    val calificacion: Float = 5f,
    val comentario: String = "",
    val fechaUtc: Long = System.currentTimeMillis(),
    val respuestaPrestador: String? = null
)

































