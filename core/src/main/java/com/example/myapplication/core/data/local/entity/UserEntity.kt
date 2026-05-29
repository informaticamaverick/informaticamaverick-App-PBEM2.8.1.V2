package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.core.domain.model.AddressClient
import com.example.myapplication.core.domain.model.CompanyClient
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.core.utils.ImageUtils

/**
 * --- ENTIDAD DE USUARIO / CLIENTE (ROOM) ---
 * Almacena el perfil del dueño de la aplicación.
 * Centraliza datos personales, direcciones y empresas locales.
 */
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String = "",
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),
    val personalAddresses: List<AddressClient> = emptyList(),
    val hasCompanyProfile: Boolean = false,
    val companies: List<CompanyClient> = emptyList(),
    val isOnline: Boolean = false,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val isPublicProfile: Boolean = false,
    val isProfileComplete: Boolean = false,
    val rating: Float = 0f,
    val favoriteProviderIds: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) {
    /**
     * Nombre completo calculado para mostrar en UI.
     */
    fun getFullName(): String = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName

    /**
     * Convierte la entidad de Room al modelo de Dominio.
     * [ELITE SSOT]: Procesa las imágenes para consumo directo en UI.
     */
    fun toDomain(): User {
        val processedCompanies = companies.map { company ->
            company.copy(
                profileImage = ImageUtils.processImageSource(company.photoUrl),
                bannerImage = ImageUtils.processImageSource(company.bannerImageUrl),
                branches = company.branches.map { branch ->
                    branch.copy(
                        representatives = branch.representatives.map { rep ->
                            rep.copy(photoImage = ImageUtils.processImageSource(rep.photoUrl))
                        }
                    )
                }
            )
        }

        return User(
            uid = id,
            email = email,
            displayName = displayName,
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            bio = bio,
            photoUrl = photoUrl,
            bannerImageUrl = bannerImageUrl,
            profileImage = ImageUtils.processImageSource(photoUrl),
            bannerImage = ImageUtils.processImageSource(bannerImageUrl),
            additionalEmails = additionalEmails,
            additionalPhones = additionalPhones,
            personalAddresses = personalAddresses,
            hasCompanyProfile = hasCompanyProfile,
            companies = processedCompanies,
            isOnline = isOnline,
            isSubscribed = isSubscribed,
            isVerified = isVerified,
            notificationsEnabled = notificationsEnabled,
            isPublicProfile = isPublicProfile,
            isProfileComplete = isProfileComplete,
            rating = rating,
            favoriteProviderIds = favoriteProviderIds,
            latitude = latitude,
            longitude = longitude,
            createdAt = createdAt,
            isSynced = isSynced
        )
    }

    companion object {
        /**
         * Crea una entidad de Room a partir del modelo de Dominio.
         */
        fun fromDomain(u: User): UserEntity = UserEntity(
            id = u.uid,
            email = u.email,
            displayName = u.displayName,
            name = u.name,
            lastName = u.lastName,
            phoneNumber = u.phoneNumber,
            bio = u.bio,
            photoUrl = u.photoUrl,
            bannerImageUrl = u.bannerImageUrl,
            additionalEmails = u.additionalEmails,
            additionalPhones = u.additionalPhones,
            personalAddresses = u.personalAddresses,
            hasCompanyProfile = u.hasCompanyProfile,
            companies = u.companies,
            isOnline = u.isOnline,
            isSubscribed = u.isSubscribed,
            isVerified = u.isVerified,
            notificationsEnabled = u.notificationsEnabled,
            isPublicProfile = u.isPublicProfile,
            isProfileComplete = u.isProfileComplete,
            rating = u.rating,
            favoriteProviderIds = u.favoriteProviderIds,
            latitude = u.latitude,
            longitude = u.longitude,
            createdAt = u.createdAt,
            isSynced = u.isSynced
        )
    }
}
