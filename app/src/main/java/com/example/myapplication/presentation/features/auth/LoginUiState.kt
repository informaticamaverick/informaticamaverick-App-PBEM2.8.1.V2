package com.example.myapplication.presentation.features.auth

/**
 * --- LOGIN UI STATE (MAVERICK ELITE) ---
 * Representa el estado de la pantalla de autenticación.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val passwordResetEmailSent: Boolean = false,
    
    // --- SECCIÓN: REESTRUCTURACIÓN MAVERICK ---
    val isRegisteringWithEmail: Boolean = false,
    val verificationCode: String = "",
    val isVerificationSent: Boolean = false,
    val timerValue: Int = 300, // 5 minutos en segundos
    val isTimerRunning: Boolean = false,
    
    // --- ESTADO DE PERMISOS ---
    val hasLocationPermission: Boolean = false,
    val hasNotificationsPermission: Boolean = false
)
