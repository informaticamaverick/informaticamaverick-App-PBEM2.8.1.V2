package com.example.myapplication.presentation.mapper

import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.utils.MaverickGeoUtils
import com.example.myapplication.data.model.BadgeDisplayData
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.data.model.ProviderType

/**
 * --- PROVIDER DISPLAY MAPPER (APP CLIENTE) ---
 * Transforma el modelo de dominio puro del Core en el modelo de visualización (DisplayModel).
 * Implementa la "Lógica Táctica" de qué mostrar al cliente sin ensuciar el ViewModel.
 */
object ProviderDisplayMapper {

    fun toDisplayModel(
        provider: Provider, 
        userLat: Double? = null, 
        userLon: Double? = null
    ): ProviderDisplayModel {
        val mainCompany = provider.companies.firstOrNull()
        val allBranches = provider.companies.flatMap { it.branches }
        
        val pLat = provider.address?.latitude ?: allBranches.firstOrNull()?.address?.latitude
        val pLon = provider.address?.longitude ?: allBranches.firstOrNull()?.address?.longitude
        
        val distance = if (userLat != null && userLon != null && pLat != null && pLon != null) {
            MaverickGeoUtils.calculateDistanceKm(userLat, userLon, pLat, pLon)
        } else null

        val isCompany = mainCompany != null
        
        val badges = listOf(
            BadgeDisplayData("24h", "🕒", "Atención 24hs", provider.works24h || allBranches.any { it.works24h }),
            BadgeDisplayData("loc", "🏪", "Local Físico", provider.hasPhysicalLocation || allBranches.any { it.hasPhysicalLocation }),
            BadgeDisplayData("visit", "🚚", "Visitas a Domicilio", provider.doesHomeVisits || allBranches.any { it.doesHomeVisits }),
            BadgeDisplayData("env", "📦", "Realiza Envíos", provider.doesShipping || allBranches.any { it.doesShipping }),
            BadgeDisplayData("date", "📅", "Turnos Online", provider.acceptsAppointments || allBranches.any { it.acceptsAppointments }),
            BadgeDisplayData("serv", "🛠️", "Servicios", provider.doesService || allBranches.any { it.doesService }),
            BadgeDisplayData("prod", "🛍️", "Venta Productos", provider.doesProduct)
        )

        val fullName = "${provider.name} ${provider.lastName}".trim()
        val displayTitle = if (provider.priorizarEmpresa && mainCompany != null) {
            mainCompany.name 
        } else {
            fullName.ifEmpty { provider.displayName }
        }

        return ProviderDisplayModel(
            id = provider.id,
            title = displayTitle,
            subtitle = if (isCompany) "Empresa" else "Independiente",
            photoUrl = if (provider.priorizarEmpresa && mainCompany != null) (mainCompany.photoUrl ?: "") else (provider.photoUrl ?: ""),
            rating = (if (provider.priorizarEmpresa && mainCompany != null) mainCompany.rating else provider.rating).toDouble(),
            isVerified = if (provider.priorizarEmpresa && mainCompany != null) mainCompany.isVerified else provider.isVerified,
            isOnline = provider.isOnline,
            isFavorite = provider.isFavorite,
            type = if (isCompany) ProviderType.COMPANY else ProviderType.INDIVIDUAL,
            works24h = provider.works24h || allBranches.any { it.works24h },
            hasPhysicalLocation = provider.hasPhysicalLocation || allBranches.any { it.hasPhysicalLocation },
            doesHomeVisits = provider.doesHomeVisits || allBranches.any { it.doesHomeVisits },
            doesService = provider.doesService || allBranches.any { it.doesService },
            doesProduct = provider.doesProduct,
            doesShipping = provider.doesShipping || allBranches.any { it.doesShipping },
            acceptsAppointments = provider.acceptsAppointments || allBranches.any { it.acceptsAppointments },
            isSubscribed = provider.isSubscribed,
            categoryId = provider.categories.firstOrNull(),
            companyId = mainCompany?.id,
            categories = provider.categories,
            displayAddress = provider.address?.fullString() ?: allBranches.firstOrNull()?.address?.fullString(),
            branchName = allBranches.firstOrNull()?.name,
            latitude = pLat,
            longitude = pLon,
            distanceKm = distance,
            typeEmoji = if (isCompany) "🏢" else "👨‍🔧",
            typeLabel = if (isCompany) "Empresa Certificada" else "Profesional Independiente",
            badgeList = badges
        )
    }
}
