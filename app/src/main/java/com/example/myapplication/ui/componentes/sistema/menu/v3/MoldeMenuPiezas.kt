package com.example.myapplication.ui.componentes.sistema.menu.v3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.componentes.sistema.DepthDividerHorizontal

import androidx.compose.ui.tooling.preview.Preview

/**
 * --- 🧩 PIEZAS DE MENÚ ELITE V3 ---
 * [PROPÓSITO]: Componentes interactivos y decorativos para menús.
 */

/**
 * MenuGrupoV3: Agrupador lógico de items de menú.
 * [v2026.ELITE]: Se eliminó la Surface interna para evitar el efecto de "tarjeta dentro de tarjeta".
 * Ahora es transparente y hereda el fondo del Molde principal.
 */
@Composable
fun MenuGrupoV3(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content
    )
}

@Composable
fun MenuItemEliteV3(
    label: String,
    emoji: String? = null,
    leadingImage: Any? = null,
    isSelected: Boolean = false,
    showCheckbox: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) SharedPalette.ElectricCyan.copy(alpha = 0.08f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showCheckbox) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SharedPalette.ElectricCyan,
                        uncheckedColor = Color.White.copy(alpha = 0.2f),
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.size(18.dp)
                )
            }

            if (leadingImage != null) {
                com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3(
                    perfil = com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3(
                        id = "iden",
                        nombre = label,
                        iniciales = label.take(1),
                        photoUrl = leadingImage
                    ),
                    tamanoBase = 24.dp,
                    mostrarBadges = false
                )
            } else if (emoji != null) {
                Text(text = emoji, fontSize = 16.sp)
            }

            TextoMenuItemLabelV3(
                text = label,
                isSelected = isSelected,
                modifier = Modifier.weight(1f)
            )

            if (isSelected && !showCheckbox) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SharedPalette.ElectricCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun MenuSectionHeaderV3(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 8.dp, bottom = 4.dp)) {
        TextoMenuTituloSeccionV3(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun MenuDividerV3(
    modifier: Modifier = Modifier
) {
    DepthDividerHorizontal(
        modifier = modifier.padding(vertical = 4.dp),
        thickness = 0.5.dp
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewPiezasMenuV3() {
    com.example.myapplication.ui.estilos.PBEMTheme {
        Column(
            modifier = Modifier.padding(16.dp).width(240.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuSectionHeaderV3("ACCIONES")
            MenuItemEliteV3(label = "Editar Perfil", emoji = "📝", onClick = {})
            MenuItemEliteV3(label = "Verificado", emoji = "✅", isSelected = true, onClick = {})
            
            MenuDividerV3()
            
            MenuSectionHeaderV3("CAMBIAR CUENTA")
            MenuItemEliteV3(
                label = "Maxi Nanterne",
                leadingImage = null,
                isSelected = true,
                onClick = {}
            )
            MenuItemEliteV3(
                label = "Maverick Corp",
                leadingImage = null,
                emoji = "🏢",
                onClick = {}
            )
        }
    }
}
