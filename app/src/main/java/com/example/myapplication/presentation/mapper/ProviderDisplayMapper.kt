package com.example.myapplication.presentation.mapper

import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.utils.MaverickGeoUtils
import com.example.myapplication.data.model.BadgeDisplayData
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.data.model.ProviderType
import com.example.myapplication.presentation.features.profile.UserLocation

/**
 * --- PROVIDER DISPLAY MAPPER ---
 * Transforma los modelos de dominio del Core a modelos listos para la UI.
 * Implementa las leyes de "Costo Zero" al pre-calcular distancias y etiquetas.
 */
object ProviderDisplayMapper {

    /**
     * Mapea un Provider a un ProviderDisplayModel.
     * Centraliza la lógica de títulos, imágenes y badges.
     */
    fun toDisplayModel(provider: Provider, userLocation: UserLocation? = null): ProviderDisplayModel {
        val distance = if (userLocation != null && provider.address != null) {
            MaverickGeoUtils.calculateDistanceKm(
                userLocation.latitude, userLocation.longitude,
                provider.address!!.latitude, provider.address!!.longitude
            )
        } else null

        // Lógica de Título: Priorizar Empresa si existe o si está configurado
        val displayTitle = if (provider.hasCompanyProfile && provider.companies.isNotEmpty()) {
            provider.companies.first().name
        } else {
            "${provider.name} ${provider.lastName}".trim().ifEmpty { provider.displayName }
        }

        return ProviderDisplayModel(
            id = provider.uid,
            title = displayTitle,
            subtitle = if (provider.hasCompanyProfile) "Empresa Certificada" else "Profesional Independiente",
            photoUrl = provider.profileImage,
            bannerImageUrl = provider.bannerImage,
            rating = provider.rating.toDouble(),
            isVerified = provider.isVerified,
            isOnline = provider.isOnline,
            isFavorite = provider.isFavorite,
            isSubscribed = provider.isSubscribed,
            type = if (provider.hasCompanyProfile) ProviderType.COMPANY else ProviderType.INDIVIDUAL,
            categories = provider.categories,
            works24h = provider.works24h,
            doesHomeVisits = provider.doesHomeVisits,
            hasPhysicalLocation = provider.hasPhysicalLocation,
            distanceKm = distance,
            typeEmoji = if (provider.hasCompanyProfile) "🏢" else "👨‍🔧",
            badgeList = listOfNotNull(
                if (provider.isVerified) BadgeDisplayData("verified", "🛡️", "Verificado", true) else null,
                if (provider.works24h) BadgeDisplayData("24h", "⚡", "24hs", true) else null,
                if (provider.doesHomeVisits) BadgeDisplayData("home", "🏠", "A Domicilio", true) else null
            )
        )
    }
}
