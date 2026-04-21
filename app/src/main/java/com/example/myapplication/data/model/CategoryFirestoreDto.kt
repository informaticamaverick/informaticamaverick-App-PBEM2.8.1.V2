package com.example.myapplication.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- DTO PARA FIRESTORE (RELEBAMIENTO ACTUALIZADO) ---
 * Solo captura los datos esenciales requeridos por el plan de acción.
 */
@IgnoreExtraProperties
data class CategoryFirestoreDto(
    val name: String = "",
    val icon: String = "", // El emoji
    val description: String = "",
    val superCategory: String = "",
    val superCategoryIcon: String = "📂" // Icono por defecto si no viene de la nube
)