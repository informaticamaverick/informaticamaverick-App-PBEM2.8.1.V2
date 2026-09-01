package com.example.myapplication.uishared.estilos

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em

/**
 * APP TYPOGRAPHY - DEFINICIÓN UNIFICADA PARA TODO EL PROYECTO
 */
object AppTypography {
    val HeaderTitle = TextStyle(
        fontFamily = FontFamily.SansSerif, 
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        letterSpacing = 2.sp,
        color = Color.White
    )

    val HeaderSubtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 5.sp,
        color = SharedPalette.ElectricCyan
    )

    val BodyText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        color = Color.LightGray
    )
}

object AppBaseStyle {
    val OrbitronLike = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp,
        color = Color.White
    )

    val SubtitleStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 4.sp,
        color = SharedPalette.MagentaNeon
    )
}

fun TextStyle.asCompact(): TextStyle {
    return this.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both
        ),
        lineHeight = 1.em
    )
}

object CyberTypography {
    val TitleTech = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = 3.sp,
        color = Color.White
    )

    val MonospaceData = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 1.5.sp,
        color = SharedPalette.ElectricCyan
    )

    val BodyCyber = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        color = Color.LightGray
    )
}

object AppStyles {
    val SectionHeader = TextStyle(
        color = SharedPalette.NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp
    )
    
    val IntelligentTag = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    val ResultTitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = SharedPalette.TextMain
    )
}
