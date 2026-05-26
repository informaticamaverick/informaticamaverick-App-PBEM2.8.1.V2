package com.example.myapplication.core.data.remote

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- DTO PARA FIRESTORE: CATEGORÍAS ---
 * Representa la estructura de los datos tal cual residen en la colección 
 * de Firebase. Se utiliza para la sincronización inicial del catálogo.
 */
@IgnoreExtraProperties
data class CategoryFirestoreDto(
    val name: String = "",
    val icon: String = "",
    val description: String = "",
    val superCategory: String = "",
    val superCategoryIcon: String = "📂"
)
