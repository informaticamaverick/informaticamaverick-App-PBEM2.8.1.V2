package com.example.myapplication.uishared.estilos

import androidx.compose.ui.graphics.Color

/**
 * Bridge object to provide colors using SharedPalette definitions.
 */
object AppColors {
    val Slate50 = SharedPalette.Slate50
    val Slate100 = SharedPalette.Slate100
    val Slate200 = SharedPalette.Slate200
    val Slate300 = SharedPalette.Slate300
    val Slate400 = SharedPalette.Slate400
    val Slate600 = SharedPalette.Slate600
    val Slate800 = SharedPalette.Slate800

    val appBlueStart = SharedPalette.BlueStart
    val appBlueEnd = SharedPalette.BlueEnd
    val appA4Gradient = SharedPalette.A4Gradient

    // Additional colors used in TarjetasPromocionesCompartidas
    val ElectricCyan = SharedPalette.ElectricCyan
    val DarkBg = SharedPalette.DarkBg
    val EliteSurface = SharedPalette.EliteSurface
    val RogCrimson = SharedPalette.RogCrimson

    // --- ALIAS DE COMPATIBILIDAD ---
    val surfaceColor = EliteSurface
    val accentBlue = appBlueStart
    val backgroundColor = SharedPalette.AbsoluteBlack
}

































