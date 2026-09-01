package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import com.example.myapplication.uishared.ui.components.AutoSizeText
import java.util.Locale

@Composable
fun TarjetaCondicionPagoMobile(
    condicionPago: String,
    onCondicionPagoChange: (String?) -> Unit
) {
    var editandoPago by remember { mutableStateOf(false) }

    TarjetaBentoContenedor {
        Text(
            text = "FORMA DE PAGO Y FINANCIACIÓN",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = ArmadorPresupuestoTema.AccentAmber,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Seleccionar o escribir método", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArmadorPresupuestoTema.TextSecondary)
        Spacer(Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = ArmadorPresupuestoTema.SurfaceInput,
            border = BorderStroke(1.dp, ArmadorPresupuestoTema.BorderGlass)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, null, tint = ArmadorPresupuestoTema.AccentAmber, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    
                    if (editandoPago) {
                        BasicTextField(
                            value = condicionPago,
                            onValueChange = onCondicionPagoChange,
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            modifier = Modifier.weight(1f),
                            cursorBrush = SolidColor(ArmadorPresupuestoTema.AccentAmber)
                        )
                    } else {
                        Text(text = condicionPago.ifBlank { "Selecciona o escribe..." }, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
                    }

                    IconButton(onClick = { editandoPago = !editandoPago }, modifier = Modifier.size(24.dp)) {
                        Icon(if (editandoPago) Icons.Default.Check else Icons.Default.Edit, null, tint = ArmadorPresupuestoTema.TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
                
                if (!editandoPago) {
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("50% Anticipo — 50% Final", "100% Contado", "3 Cuotas s/ Interés").forEach { t ->
                            SuggestionChip(
                                onClick = { onCondicionPagoChange(t) },
                                label = { Text(t, fontSize = 9.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (condicionPago == t) ArmadorPresupuestoTema.AccentAmber.copy(alpha = 0.2f) else Color.Transparent,
                                    labelColor = if (condicionPago == t) ArmadorPresupuestoTema.AccentAmber else ArmadorPresupuestoTema.TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaAjustesFinancieros(
    alicuotaIva: Double,
    onCambiarAlicuota: (Double) -> Unit,
    descuento: String,
    onDescuentoChange: (String) -> Unit,
    observaciones: String,
    onObservacionesChange: (String?) -> Unit
) {
    TarjetaBentoContenedor {
        Text(
            text = "IMPUESTOS Y DESCUENTOS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = ArmadorPresupuestoTema.BrandOrange,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Alícuota de Impuesto / IVA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ArmadorPresupuestoTema.TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonOpcionImpuesto(
                texto = "21%",
                seleccionado = alicuotaIva == 21.0,
                onClick = { onCambiarAlicuota(21.0) },
                modifier = Modifier.weight(1f)
            )
            BotonOpcionImpuesto(
                texto = "10.5%",
                seleccionado = alicuotaIva == 10.5,
                onClick = { onCambiarAlicuota(10.5) },
                modifier = Modifier.weight(1f)
            )
            BotonOpcionImpuesto(
                texto = "0% Exento",
                seleccionado = alicuotaIva == 0.0,
                onClick = { onCambiarAlicuota(0.0) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descuento,
            onValueChange = onDescuentoChange,
            label = { Text("Descuento Especial ($)", fontSize = 11.sp) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ArmadorPresupuestoTema.BrandOrange,
                unfocusedBorderColor = ArmadorPresupuestoTema.BorderGlass,
                focusedContainerColor = ArmadorPresupuestoTema.SurfaceInput,
                unfocusedContainerColor = ArmadorPresupuestoTema.SurfaceInput
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = observaciones,
            onValueChange = onObservacionesChange,
            label = { Text("Observaciones / Nota al Pie", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ArmadorPresupuestoTema.BrandOrange,
                unfocusedBorderColor = ArmadorPresupuestoTema.BorderGlass,
                focusedContainerColor = ArmadorPresupuestoTema.SurfaceInput,
                unfocusedContainerColor = ArmadorPresupuestoTema.SurfaceInput
            )
        )
    }
}

@Composable
fun TarjetaRentabilidadHudMobile(
    subtotal: Double,
    costoEstimado: Double
) {
    val gananciaNeta = (subtotal - costoEstimado).coerceAtLeast(0.0)
    val porcentajeGanancia = if (subtotal > 0) ((gananciaNeta / subtotal) * 100).toInt() else 0

    TarjetaBentoContenedor(borderColor = ArmadorPresupuestoTema.AccentEmerald.copy(alpha = 0.3f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = ArmadorPresupuestoTema.AccentEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rentabilidad Neta Est.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArmadorPresupuestoTema.TextPrimary
                )
            }
            Text(
                text = "$porcentajeGanancia% Ganancia",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = ArmadorPresupuestoTema.AccentEmerald
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (porcentajeGanancia / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = ArmadorPresupuestoTema.AccentEmerald,
            trackColor = ArmadorPresupuestoTema.SurfaceInput,
        )
    }
}

@Composable
fun TarjetaResumenCalculosMobile(
    calculos: CalculadoraPresupuesto.ResultadoCalculo
) {
    TarjetaBentoContenedor {
        Text(
            text = "RESUMEN DE MONTOS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = ArmadorPresupuestoTema.BrandOrange,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        FilaResumenMonto("Subtotal Materiales", calculos.totalMateriales)
        FilaResumenMonto("Subtotal Mano de Obra", calculos.totalManoObra)
        FilaResumenMonto("Subtotal Bruto", calculos.subtotal, destacado = true)
        FilaResumenMonto("Impuestos", calculos.montoImpuestos, colorTexto = ArmadorPresupuestoTema.BrandOrangeLight)
        FilaResumenMonto("Descuento", -calculos.montoDescuento, colorTexto = ArmadorPresupuestoTema.AccentEmerald)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = ArmadorPresupuestoTema.BorderGlass
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL ESTIMADO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = ArmadorPresupuestoTema.TextPrimary
            )
            AutoSizeText( // 🔥 LEY 4 OJOS
                text = "$ ${String.format(Locale.getDefault(), "%,.0f", calculos.totalGeneral)}",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = ArmadorPresupuestoTema.BrandOrange
                ),
                maxLines = 1
            )
        }
    }
}

