package com.example.myapplication.presentation.features.profile

import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.CompanyClient

/**
 * --- USER UI STATE (SSOT) ---
 * [ELITE v5.1]: Alineado con la nueva anatomía unificada (Costo Zero).
 */
data class UserUiState(
    val uid: String = "",
    val displayName: String = "",
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "", // Solo para validaciones o re-autenticación
    val bio: String = "",
    val photoUrl: String = "",
    val profileThumbnail: String = "", // [LEY #3] Carga instantánea
    
    // --- CAMPOS TEMPORALES PARA FORMULARIO ---
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",

    // --- ESTRUCTURAS DE DATOS UNIFICADAS ---
    val personalAddresses: List<AddressUnico> = emptyList(),
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),
    
    // --- GESTIÓN DE EMPRESAS (Sin categorías) ---
    val isEmpresa: Boolean = false, 
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
    val isEditMode: Boolean = false,
    val selectedProfileId: String? = null
)
