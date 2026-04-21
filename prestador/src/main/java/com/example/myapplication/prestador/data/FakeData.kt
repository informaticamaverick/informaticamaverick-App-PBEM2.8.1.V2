
/**
package com.example.myapplication.prestador.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color

// --- DATA FROM CategorySampleDataFalso.kt ---
data class CategoryItem(
    val name: String,
    val icon: String,
    val color: Color,
    val superCategory: String,
    val providerIds: List<String> = emptyList()
)

object CategorySampleDataFalso {
    val categories = listOf(
        CategoryItem("Limpieza", "🧹", Color(0xFFFAD2E1), "Hogar", providerIds = listOf("2")),
        CategoryItem("Jardinería", "🌿", Color(0xFFE2F0CB), "Hogar", providerIds = listOf("2", "13"))
        // Add more categories as needed
    )
}

// --- DATA FROM PrestadorSampleDataFalso.kt ---
data class PPrestadorProfileFalso(
    val id: String,
    val name: String,
    val lastName: String,
    val profileImageUrl: Any?,
    val bannerImageUrl: Any?,
    val rating: Float,
    val services: List<String>,
    val companyName: String? = null,
    val address: String,
    val email: String,
    val phone: String = "",
    val doesHomeVisits: Boolean,
    val hasPhysicalLocation: Boolean,
    val works24h: Boolean,
    val galleryImages: List<String>,
    val isFavorite: Boolean = false,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isSubscribed: Boolean
)

object PPrestadorSampleDataFalso {
    val pprestadores = mutableStateListOf(
        PPrestadorProfileFalso(
            id = "1",
            name = "Maxi",
            lastName = "Nanterne",
            profileImageUrl = "", // Placeholder
            bannerImageUrl = "", // Placeholder
            rating = 5.0f,
            isVerified = true,
            isOnline = true,
            services = listOf("Informatica", "Electricidad", "Reparación", "Camaras de Seguridad"),
            companyName = "Maverick Informatica",
            address = "B. Matienzo 1339",
            email = "informaticamaverick@gmail.com",
            phone = "",
            doesHomeVisits = true,
            hasPhysicalLocation = true,
            works24h = false,
            galleryImages = listOf(""),
            isFavorite = true,
            isSubscribed = true
        )
        // Add more prestadores as needed
    )

    fun getPPrestadorById(id: String): PPrestadorProfileFalso? {
        return pprestadores.find { it.id == id }
    }
}
**/