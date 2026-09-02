package com.example.myapplication.prestador.ui.pantallas.register.componentes

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme

@Composable
fun RegisterSectionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = GestionTurnosTheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.CardBg,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Borde izquierdo de color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Cabecera clickeable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandChange() }
                        .padding(bottom = if (expanded) 16.dp else 0.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint = color
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column { content() }
                }
            }
        }
    }
}

@Composable
fun FloatingLabelTextField(
    valor: String,
    onValorCambio: (String) -> Unit,
    etiqueta: String,
    iconoPrimario: ImageVector?,
    iconoSecundario: ImageVector? = null,
    alClickIconoSecundario: (() -> Unit)? = null,
    habilitado: Boolean = true,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    transformacionVisual: VisualTransformation = VisualTransformation.None,
    prefijo: String? = null,
    modifier: Modifier = Modifier,
    fuenteInteraccion: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val colores = GestionTurnosTheme
    val enfocado by fuenteInteraccion.collectIsFocusedAsState()
    val tieneTexto = valor.isNotEmpty()

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambio,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            leadingIcon = iconoPrimario?.let { {
                Icon(it, contentDescription = null, tint = colores.TextMuted)
            }},
            trailingIcon = iconoSecundario?.let { {
                IconButton(onClick = { alClickIconoSecundario?.invoke() }) {
                    Icon(it, contentDescription = null, tint = colores.TextMuted)
                }
            }},
            label = {
                Text(
                    etiqueta,
                    color = if (enfocado) colores.BrandOrange else colores.TextMuted,
                    fontSize = if (enfocado || tieneTexto) 12.sp else 16.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colores.BrandOrange,
                unfocusedBorderColor = if (habilitado) colores.BorderGlass else Color.Transparent,
                focusedLabelColor = colores.BrandOrange,
                unfocusedLabelColor = colores.TextMuted,
                focusedTextColor = colores.TextPrimary,
                unfocusedTextColor = colores.TextPrimary,
                focusedContainerColor = colores.SurfaceInput,
                unfocusedContainerColor = colores.SurfaceInput,
                cursorColor = colores.BrandOrange
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            visualTransformation = transformacionVisual,
            interactionSource = fuenteInteraccion,
            singleLine = true,
            prefix = prefijo?.let { { Text(it, color = colores.TextSecondary)}}
        )
    }
}
