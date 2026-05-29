package com.example.myapplication.core.domain.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- MODELO DE DOMINIO: User (PERFIL DEL CLIENTE / USUARIO) ---
 * Este objeto es el "lenguaje común" para representar al cliente en todo el ecosistema.
 * Contiene datos personales, direcciones y empresas asociadas.
 */
@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    
    // --- DATOS PERSONALES ---
    var name: String = "",
    var lastName: String = "",
    var phoneNumber: String = "",
    var bio: String = "",
    var photoUrl: String? = null,
    var bannerImageUrl: String? = null,

    /** [ELITE SSOT]: Imágenes listas para consumo UI (String o ByteArray) */
    val profileImage: Any? = null,
    val bannerImage: Any? = null,

    // --- CONTACTOS ADICIONALES ---
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),

    // --- DIRECCIONES ---
    val personalAddresses: List<AddressClient> = emptyList(),

    // --- EMPRESAS ---
    @get:PropertyName("hasCompanyProfile")
    @set:PropertyName("hasCompanyProfile")
    var hasCompanyProfile: Boolean = false,
    val companies: List<CompanyClient> = emptyList(),
    
    // --- ESTADOS Y BANDERAS ---
    @get:PropertyName("isProfileComplete")
    @set:PropertyName("isProfileComplete")
    var isProfileComplete: Boolean = false,

    @get:PropertyName("isSubscribed")
    @set:PropertyName("isSubscribed")
    var isSubscribed: Boolean = false,

    @get:PropertyName("isVerified")
    @set:PropertyName("isVerified")
    var isVerified: Boolean = false,

    @get:PropertyName("isOnline")
    @set:PropertyName("isOnline")
    var isOnline: Boolean = false,

    @get:PropertyName("notificationsEnabled")
    @set:PropertyName("notificationsEnabled")
    var notificationsEnabled: Boolean = false,

    @get:PropertyName("isPublicProfile")
    @set:PropertyName("isPublicProfile")
    var isPublicProfile: Boolean = false,
    
    // --- REPUTACIÓN ---
    val rating: Float = 0f,
    val favoriteProviderIds: List<String> = emptyList(),

    // --- GEOLOCALIZACIÓN ---
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    
    @get:PropertyName("isSynced")
    @set:PropertyName("isSynced")
    var isSynced: Boolean = true
) {
    /**
     * Nombre completo calculado para mostrar en UI.
     */
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName
        
    /**
     * Obtiene la dirección marcada como principal.
     */
    val mainAddress: AddressClient?
        get() = personalAddresses.firstOrNull()

    /**
     * --- MAPEO INTELIGENTE (LEY 2: READY-TO-CONSUME) ---
     * Aplana todas las direcciones disponibles (Personales + Empresas) en una lista única de AddressInfo.
     * Centraliza la lógica de nombres y fotos para que la UI sea puramente reactiva.
     */
    fun toAddressInfoList(): List<AddressInfo> {
        val list = mutableListOf<AddressInfo>()

        // 1. Direcciones Personales
        personalAddresses.forEach { addr ->
            list.add(AddressInfo(
                id = addr.id,
                ownerId = null, // Usuario Principal
                companyOrUserName = fullName,
                profilePhoto = profileImage,
                branchName = addr.label.ifBlank { "Mi Domicilio" },
                streetAndNumber = "${addr.calle} ${addr.numero}".trim(),
                locality = addr.localidad,
                province = addr.provincia,
                country = addr.pais,
                postalCode = addr.codigoPostal,
                isCompany = false,
                lat = addr.latitude,
                lng = addr.longitude
            ))
        }

        // 2. Direcciones de Empresas
        companies.forEach { company ->
            company.branches.forEach { branch ->
                list.add(AddressInfo(
                    id = branch.id,
                    ownerId = company.id, // ID de la Empresa para cambio de perfil
                    companyOrUserName = company.name,
                    profilePhoto = company.profileImage,
                    branchName = branch.name,
                    streetAndNumber = "${branch.address.calle} ${branch.address.numero}".trim(),
                    locality = branch.address.localidad,
                    province = branch.address.provincia,
                    country = branch.address.pais,
                    postalCode = branch.address.codigoPostal,
                    isCompany = true,
                    lat = branch.address.latitude,
                    lng = branch.address.longitude
                ))
            }
        }
        return list
    }
}
