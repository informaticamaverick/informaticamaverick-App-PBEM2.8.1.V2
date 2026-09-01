package com.example.myapplication.core.datos.local.entidades.vistas

import androidx.room.DatabaseView

/**
 * --- VISTA RESUMEN DE CATEGORÍAS (ELITE v2026) ---
 * [PROPÓSITO]: Unir categorías con sus supercategorías para obtener color y nombre
 * directamente desde el motor SQL, evitando mappers pesados en memoria.
 * [LEY #14]: El Embudo. Unión relacional en la fuente.
 */
@DatabaseView("""
    SELECT 
        c.*,
        sc.nombre as superCategoriaNombre,
        sc.color as superCategoriaColor
    FROM categorias c
    INNER JOIN super_categorias sc ON c.idSuperCategoria = sc.id
""")
data class CategoriaResumenSQLView(
    val rowid: Long,
    val id: String,
    val nombre: String,
    val icono: String,
    val idSuperCategoria: String,
    val descripcion: String,
    val esNueva: Boolean,
    
    // --- CAMPOS DE LA UNIÓN ---
    val superCategoriaNombre: String,
    val superCategoriaColor: Long
)
