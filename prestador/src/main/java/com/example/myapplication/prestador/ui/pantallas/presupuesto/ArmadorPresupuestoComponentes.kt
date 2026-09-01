package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.ui.components.AutoSizeText
import java.util.Locale

@Composable
fun TarjetaBentoContenedor(
    modifier: Modifier = Modifier,
    borderColor: Color = ArmadorPresupuestoTema.BorderGlass,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp), // 🔥 LEY 4 OJOS: Altura mínima, nunca fija.
        shape = RoundedCornerShape(4.dp),
        color = ArmadorPresupuestoTema.SurfaceCard,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}

@Composable
fun FilaResumenMonto(
    etiqueta: String,
    monto: Double,
    destacado: Boolean = false,
    colorTexto: Color = ArmadorPresupuestoTema.TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            fontSize = 11.sp,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Medium,
            color = if (destacado) ArmadorPresupuestoTema.TextPrimary else ArmadorPresupuestoTema.TextSecondary,
            modifier = Modifier.weight(1f) // 🔥 LEY 4 OJOS: Permite que la etiqueta respire
        )
        AutoSizeText( // 🔥 LEY 4 OJOS: El monto no se corta
            text = "$ ${String.format(Locale.getDefault(), "%,.0f", monto)}",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            ),
            maxLines = 1,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun FilaInputDetalle(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 9.sp, color = ArmadorPresupuestoTema.TextSecondary, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
            cursorBrush = SolidColor(ArmadorPresupuestoTema.BrandOrange),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ArmadorPresupuestoTema.SurfaceInput, RoundedCornerShape(6.dp))
                        .border(1.dp, ArmadorPresupuestoTema.BorderGlass, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = ArmadorPresupuestoTema.TextMuted, fontSize = 13.sp)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDropdownBasico(
    etiqueta: String,
    opcionSeleccionada: String,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Column {
        Text(text = etiqueta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArmadorPresupuestoTema.TextSecondary)
        Spacer(modifier = Modifier.height(3.dp))
        ExposedDropdownMenuBox(
            expanded = expandido,
            onExpandedChange = { expandido = !expandido }
        ) {
            OutlinedTextField(
                value = opcionSeleccionada,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = ArmadorPresupuestoTema.TextPrimary),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArmadorPresupuestoTema.BrandOrange,
                    unfocusedBorderColor = ArmadorPresupuestoTema.BorderGlass,
                    focusedContainerColor = ArmadorPresupuestoTema.SurfaceInput,
                    unfocusedContainerColor = ArmadorPresupuestoTema.SurfaceInput
                )
            )
            ExposedDropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false },
                modifier = Modifier.background(ArmadorPresupuestoTema.SurfaceCardSolid)
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion, fontSize = 11.sp, color = ArmadorPresupuestoTema.TextPrimary) },
                        onClick = {
                            onSeleccionar(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilaInputSheet(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = ArmadorPresupuestoTema.TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = if (readOnly) ({}) else onValueChange,
            readOnly = readOnly,
            textStyle = TextStyle(
                color = if (readOnly) ArmadorPresupuestoTema.TextMuted else Color.White, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            cursorBrush = SolidColor(ArmadorPresupuestoTema.BrandOrange),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (readOnly) Color.Transparent else ArmadorPresupuestoTema.SurfaceCardSolid, 
                    RoundedCornerShape(4.dp)
                )
                .border(
                    1.dp, 
                    if (readOnly) ArmadorPresupuestoTema.BorderGlass.copy(alpha = 0.5f) else ArmadorPresupuestoTema.BorderGlass, 
                    RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        )
    }
}

@Composable
fun BotonOpcionImpuesto(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (seleccionado) ArmadorPresupuestoTema.BrandOrange.copy(alpha = 0.2f) else ArmadorPresupuestoTema.SurfaceInput,
        border = BorderStroke(
            1.dp,
            if (seleccionado) ArmadorPresupuestoTema.BrandOrange else ArmadorPresupuestoTema.BorderGlass
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (seleccionado) ArmadorPresupuestoTema.BrandOrange else ArmadorPresupuestoTema.TextSecondary
            )
        }
    }
}
