package com.example.myapplication.ui.componentes.sistema.menu.v3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit

/**
 * --- 🧪 ÁTOMOS DE MENÚ V3 ---
 * [PROPÓSITO]: Definir los estilos de texto elásticos para los menús del sistema.
 * [LEY #11]: Garantiza que las opciones del menú sean legibles en cualquier escala.
 */

@Composable
fun TextoMenuTituloSeccionV3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SharedPalette.ElectricCyan
) {
    TextCompactoAutoFit(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        maxFontSize = 9.sp,
        minFontSize = 7.sp,
        fontWeight = FontWeight.Black,
        style = AppTypography.HeaderSubtitle.copy(
            letterSpacing = 1.5.sp
        )
    )
}

@Composable
fun TextoMenuItemLabelV3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    isSelected: Boolean = false
) {
    TextCompactoAutoFit(
        text = text.uppercase(),
        modifier = modifier,
        color = if (isSelected) SharedPalette.ElectricCyan else color,
        maxFontSize = 11.sp,
        minFontSize = 9.sp,
        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
        style = AppTypography.HeaderTitle.copy(
            letterSpacing = 0.5.sp
        )
    )
}

@Composable
fun TextoMenuInformativoV3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    TextCompactoAutoFit(
        text = text,
        modifier = modifier,
        color = color,
        maxFontSize = 10.sp,
        minFontSize = 8.sp,
        fontWeight = FontWeight.Medium,
        style = AppTypography.HeaderSubtitle
    )
}
