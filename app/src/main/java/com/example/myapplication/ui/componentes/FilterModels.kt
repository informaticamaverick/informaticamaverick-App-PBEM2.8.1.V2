package com.example.myapplication.ui.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * appFilterItem: Modelo unificado para filtros, ordenamiento y accesos directos.
 * [ELITE SSOT]: Este es el núcleo de datos para todo el sistema de filtrado v3.
 */
data class appFilterItem(
    val id: String,
    val label: String,
    val section: String? = null,
    val emoji: String? = null,
    val icon: ImageVector? = null,
    val color: Color = Color.White
) {
    /**
     * 🔥 ELITE: Convierte datos de filtro a items de control para el HUD.
     */
    fun toControlItem(): ControlItem {
        return ControlItem(
            label = this.label,
            icon = this.icon,
            emoji = this.emoji ?: "🔹",
            color = this.color,
            id = this.id
        )
    }
}

/**
 * ControlItem: Modelo de datos para elementos de filtrado y ordenamiento en el HUD.
 */
data class ControlItem(
    val label: String,
    val icon: ImageVector? = null,
    val emoji: String? = null,
    val color: Color,
    val id: String = label.lowercase()
)

// Retrocompatibilidad y simplicidad semántica
typealias DropdownItemData = appFilterItem
typealias FilterSortItem = appFilterItem


































