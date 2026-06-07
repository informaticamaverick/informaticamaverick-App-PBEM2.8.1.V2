package com.example.myapplication.presentation.mapper

import com.example.myapplication.core.data.local.dao.ShallowProvider
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.utils.MaverickGeoUtils
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.data.model.BadgeDisplayData
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.data.model.ProviderType
import com.example.myapplication.presentation.features.profile.UserLocation
import java.util.Calendar

/**
 * --- PROVIDER DISPLAY MAPPER ---
 * Transforma los modelos de dominio del Core a modelos listos para la UI.
 * RESPETA estrictamente la estructura de Provider.kt y ProviderEntity.kt.
 */
object ProviderDisplayMapper {

    fun toDisplayModel(
        provider: Provider, 
        userLocation: UserLocation? = null
    ): ProviderDisplayModel {
        // [COMPATIBILIDAD MULTI-MÓDULO] Variables locales para smart casting
        val pAddr = provider.address
        val distance = if (userLocation != null && pAddr != null) {
            MaverickGeoUtils.calculateDistanceKm(
                userLocation.latitude, userLocation.longitude,
                pAddr.latitude, pAddr.longitude
            )
        } else null

        val displayTitle = if (provider.companies.isNotEmpty()) {
            provider.companies.first().name
        } else {
            "${provider.name} ${provider.lastName}".trim().ifEmpty { provider.displayName }
        }

        val availability = calculateAvailability(provider.workingHours)

        return ProviderDisplayModel(
            id = provider.uid,
            providerId = provider.uid,
            title = displayTitle,
            subtitle = if (provider.companies.isNotEmpty()) "Empresa Certificada" else "Profesional Independiente",
            photoUrl = ImageUtils.processImageSource(provider.profileThumbnail ?: provider.photoUrl),
            rating = provider.rating.toDouble(),
            email = provider.email,
            emails = provider.emails,
            phoneNumber = provider.phoneNumber,
            name = provider.name,
            lastName = provider.lastName,
            matricula = provider.matricula,
            profesion = provider.profesion,
            cuilCuit = provider.cuilCuit,
            description = provider.description,
            categories = provider.categories,
            workingHours = provider.workingHours,
            addresses = provider.addresses,
            companies = provider.companies,
            isVerified = provider.isVerified,
            isOnline = provider.isOnline,
            isFavorite = provider.isFavorite,
            isSubscribed = provider.isSubscribed,
            works24h = provider.works24h,
            hasPhysicalLocation = provider.hasPhysicalLocation,
            doesHomeVisits = provider.doesHomeVisits,
            doesShipping = provider.doesShipping,
            acceptsAppointments = provider.acceptsAppointments,
            trabajaConOtros = provider.trabajaConOtros,
            type = if (provider.companies.isNotEmpty()) ProviderType.COMPANY else ProviderType.INDIVIDUAL,
            distanceKm = distance,
            branchName = if (provider.companies.isNotEmpty()) provider.companies.first().name else "Profesional Independiente",
            displayAddress = provider.address?.fullString() ?: "Ubicación no disponible",
           // latitude = provider.latitude,
           // longitude = provider.longitude,
            typeEmoji = if (provider.companies.isNotEmpty()) "🏢" else "👨‍🔧",
            statusText = availability.first,
            statusColor = availability.second,
            badgeList = createBadgeList(provider)
        )
    }

    fun fromShallow(
        provider: ShallowProvider, 
        userLocation: UserLocation? = null,
        targetZipCode: String? = null
    ): List<ProviderDisplayModel> {
        val results = mutableListOf<ProviderDisplayModel>()

        // [ELITE] Si la entidad tiene empresas/sucursales, mapeamos cada una que coincida
        if (provider.companies.isNotEmpty()) {
            provider.companies.forEach { company ->
                val branch = company.branches.firstOrNull { 
                    targetZipCode == null || it.address.codigoPostal == targetZipCode 
                } ?: company.branches.firstOrNull()

                if (branch != null) {
                    val displayModel = createFromBranch(provider, company, branch, userLocation).copy(
                        nearbyBranchesCount = provider.extraBranches // Usamos el conteo calculado por SQL
                    )
                    results.add(displayModel)
                }
            }
        }

        // Si no hay resultados de sucursales, usamos el perfil principal
        if (results.isEmpty()) {
            val mainModel = createFromMain(provider, userLocation).copy(
                nearbyBranchesCount = provider.extraBranches
            )
            results.add(mainModel)
        }

        return results
    }

    private fun createFromMain(provider: ShallowProvider, userLocation: UserLocation?): ProviderDisplayModel {
        val pLat = provider.latitude
        val pLng = provider.longitude
        val distance = if (userLocation != null && pLat != null && pLng != null) {
            MaverickGeoUtils.calculateDistanceKm(userLocation.latitude, userLocation.longitude, pLat, pLng)
        } else null
        
        val availability = calculateAvailability(provider.workingHours)

        return ProviderDisplayModel(
            id = provider.id,
            providerId = provider.providerId,
            title = provider.displayName.ifBlank { "${provider.name} ${provider.lastName}".trim() },
            photoUrl = ImageUtils.processImageSource(provider.profileThumbnail ?: provider.photoUrl),
            rating = provider.rating.toDouble(),
            email = "", // Shallow no descarga contacto para ahorrar datos
            phoneNumber = "",
            name = provider.name,
            lastName = provider.lastName,
            workingHours = provider.workingHours,
            categories = provider.categories,
            isVerified = provider.isVerified,
            isOnline = provider.isOnline,
            isSubscribed = provider.isSubscribed,
            works24h = provider.works24h,
            hasPhysicalLocation = provider.hasPhysicalLocation,
            doesHomeVisits = provider.doesHomeVisits,
            doesShipping = provider.doesShipping,
            acceptsAppointments = provider.acceptsAppointments,
            type = if (provider.hasPhysicalLocation) ProviderType.COMPANY else ProviderType.INDIVIDUAL,
            latitude = pLat,
            longitude = pLng,
            distanceKm = distance,
            branchName = "CASA CENTRAL",
            displayAddress = "Dirección no disponible",
            statusText = availability.first,
            statusColor = availability.second,
            badgeList = createShallowBadges(provider)
        )
    }

    private fun createFromBranch(
        provider: ShallowProvider, 
        company: com.example.myapplication.core.domain.model.CompanyProvider, 
        branch: com.example.myapplication.core.domain.model.BranchProvider, 
        userLocation: UserLocation?
    ): ProviderDisplayModel {
        val distance = if (userLocation != null) {
            MaverickGeoUtils.calculateDistanceKm(
                userLocation.latitude, userLocation.longitude,
                branch.address.latitude, branch.address.longitude
            )
        } else null

        return ProviderDisplayModel(
            id = "${provider.id}_${company.id}_${branch.id}",
            providerId = provider.providerId,
            companyId = company.id,
            branchId = branch.id,
            title = "${company.name} - ${branch.name}",
            photoUrl = ImageUtils.processImageSource(company.thumbnailBase64 ?: company.photoUrl ?: provider.profileThumbnail ?: provider.photoUrl),
            rating = branch.rating.toDouble(),
            email = company.email,
            phoneNumber = "",
            name = provider.name,
            lastName = provider.lastName,
            workingHours = branch.workingHours,
            categories = company.categories.ifEmpty { provider.categories },
            isVerified = company.isVerified,
            isOnline = provider.isOnline,
            isSubscribed = provider.isSubscribed,
            works24h = branch.works24h,
            hasPhysicalLocation = branch.hasPhysicalLocation,
            doesHomeVisits = branch.doesHomeVisits,
            doesShipping = branch.doesShipping,
            acceptsAppointments = branch.acceptsAppointments,
            type = ProviderType.COMPANY,
            latitude = branch.address.latitude,
            longitude = branch.address.longitude,
            distanceKm = distance,
            branchName = branch.name,
            displayAddress = branch.address.fullString(),
            fullAddress = provider.fullAddress ?: branch.address.fullString(),
            statusText = calculateAvailability(branch.workingHours).first,
            statusColor = calculateAvailability(branch.workingHours).second,
            badgeList = createBranchBadges(provider, branch)
        )
    }

    private fun createBadgeList(p: Provider): List<BadgeDisplayData> = listOfNotNull(
        if (p.isVerified) BadgeDisplayData("verified", "🛡️", "Verificado", true) else null,
        if (p.works24h) BadgeDisplayData("24h", "🚨", "Urgencias 24h", true) else null,
        if (p.doesHomeVisits) BadgeDisplayData("home", "🏠", "A Domicilio", true) else null,
        if (p.doesShipping) BadgeDisplayData("shipping", "📦", "Envíos", true) else null,
        if (p.isSubscribed) BadgeDisplayData("elite", "👥", "Equipo Elite", true) else null
    )

    private fun createShallowBadges(p: ShallowProvider): List<BadgeDisplayData> = listOfNotNull(
        if (p.isVerified) BadgeDisplayData("verified", "🛡️", "Verificado", true) else null,
        if (p.works24h) BadgeDisplayData("24h", "🚨", "Urgencias 24h", true) else null,
        if (p.isSubscribed) BadgeDisplayData("elite", "👥", "Equipo Elite", true) else null
    )

    private fun createBranchBadges(p: ShallowProvider, b: com.example.myapplication.core.domain.model.BranchProvider): List<BadgeDisplayData> = listOfNotNull(
        if (p.isVerified) BadgeDisplayData("verified", "🛡️", "Verificado", true) else null,
        if (b.works24h) BadgeDisplayData("24h", "🚨", "Urgencias 24h", true) else null,
        if (b.doesHomeVisits) BadgeDisplayData("home", "🏠", "A Domicilio", true) else null
    )

    private fun calculateAvailability(schedule: String): Pair<String, Long> {
        if (schedule.isBlank()) return "Sin Horarios" to 0xFF9E9E9E
        return try {
            val now = Calendar.getInstance()
            val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val parts = schedule.split("-")
            if (parts.size >= 2) {
                val start = parts[0].trim().split(":")
                val end = parts[1].trim().split(":")
                val startM = start[0].toInt() * 60 + start[1].take(2).toInt()
                val endM = end[0].toInt() * 60 + end[1].take(2).toInt()
                
                // Segmento tarde hardcoded (Elite Standard)
                val isOpen = (currentMinutes in startM..endM) || (currentMinutes in (16 * 60)..(20 * 60))
                if (isOpen) "ABIERTO AHORA ✅" to 0xFF4ADE80 else "CERRADO 🔴" to 0xFFF87171
            } else "Horario Especial" to 0xFFFBBF24
        } catch (_: Exception) { "Horario Especial" to 0xFFFBBF24 }
    }
}
