package com.example.myapplication.presentation.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Esta función ahora es un alias para getThemeColors() en Theme.kt
// Mantenida por compatibilidad con código existente
@Composable
fun getAppColors(): AppColors {
    return getThemeColors()
}









