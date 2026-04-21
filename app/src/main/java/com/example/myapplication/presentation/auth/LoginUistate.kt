package com.example.myapplication.presentation.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val passwordResetEmailSent: Boolean = false,
    
    // --- NUEVOS ESTADOS PARA LA REESTRUCTURACIÓN MAVERICK ---
    val isRegisteringWithEmail: Boolean = false,
    val verificationCode: String = "",
    val isVerificationSent: Boolean = false,
    val timerValue: Int = 300, // 5 minutos en segundos
    val isTimerRunning: Boolean = false
)
