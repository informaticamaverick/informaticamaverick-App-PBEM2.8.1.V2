package com.example.myapplication.presentation.features.profile

import com.example.myapplication.core.domain.model.AddressClient
import com.example.myapplication.core.domain.model.CompanyClient

/**
 * --- PROFILE UI STATE ---
 * 
 * Representa el estado de la interfaz de usuario para la gestión del perfil del dueño.
 * Alineado con la nueva estructura de UserEntity.
 */
data class ProfileUiState(
    val uid: String = "",
    val displayName: String = "",
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "", // Solo para validaciones o re-autenticación
    val bio: String = "",
    val photoUrl: String = "",
    val coverPhotoUrl: String = "",
    
    // --- CAMPOS TEMPORALES PARA FORMULARIO DE DIRECCIÓN (CompleteProfile) ---
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",

    // --- ESTRUCTURAS DE DATOS SEGÚN NUEVA UserEntity ---
    val personalAddresses: List<AddressClient> = emptyList(),
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),
    val galleryImages: List<String> = emptyList(),
    
    // --- GESTIÓN DE EMPRESAS ---
    val isEmpresa: Boolean = false, // Vinculado a hasCompanyProfile
    val companies: List<CompanyClient> = emptyList(),

    // --- ESTADOS Y BANDERAS ---
    val isOnline: Boolean = false,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val isPublicProfile: Boolean = false,
    val isProfileComplete: Boolean = false,
    
    // --- SOCIAL Y REPUTACIÓN ---
    val rating: Float = 0f,
    val favoriteProviderIds: List<String> = emptyList(),

    // --- GEOLOCALIZACIÓN ---
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // --- ESTADOS DE CONTROL DE UI ---
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isComplete: Boolean = false,

    // ==========================================
    // SECCIÓN NUEVA: MODO EDICIÓN
    // ==========================================
    val isEditMode: Boolean = false
)









