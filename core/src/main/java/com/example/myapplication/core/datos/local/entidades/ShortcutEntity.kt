package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- SHORTCUT ENTITY (SSOT 2026) ---
 * [LEY #9]: Estándar Mav.
 */
@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey val id: String,
    val contexto: String, // Cambiado a español (Legacy: context)
    val idDestino: String, // Cambiado a español (Legacy: targetId)
    val tipo: String,      // Cambiado a español (Legacy: type)
    val etiqueta: String? = null, // Cambiado a español (Legacy: label)
    val icono: String? = null,     // Cambiado a español (Legacy: icon)
    val marcaTiempo: Long = System.currentTimeMillis() // Cambiado a español (Legacy: timestamp)
)

































