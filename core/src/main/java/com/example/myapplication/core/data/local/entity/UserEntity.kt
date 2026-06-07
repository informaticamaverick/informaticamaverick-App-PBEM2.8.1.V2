package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.CompanyClient
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.core.utils.ImageUtils

/**
 * --- ENTIDAD DE USUARIO / CLIENTE (ROOM) ---
 * [ELITE v5.1]: Sin flags de prestador, sin categorías, sin banners.
 * Usa AddressUnico para paridad.
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
    val profileThumbnail: String? = null,
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),
    val personalAddresses: List<AddressUnico> = emptyList(),
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
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        uid = id,
        email = email,
        displayName = displayName,
        name = name,
        lastName = lastName,
        phoneNumber = phoneNumber,
        bio = bio,
        photoUrl = photoUrl,
        profileThumbnail = profileThumbnail,
        additionalEmails = additionalEmails,
        additionalPhones = additionalPhones,
        personalAddresses = personalAddresses,
        hasCompanyProfile = hasCompanyProfile,
        companies = companies,
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
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(u: User): UserEntity = UserEntity(
            id = u.uid,
            email = u.email,
            displayName = u.displayName,
            name = u.name,
            lastName = u.lastName,
            phoneNumber = u.phoneNumber,
            bio = u.bio,
            photoUrl = u.photoUrl,
            profileThumbnail = u.profileThumbnail,
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
            updatedAt = u.updatedAt
        )
    }
}
