package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import com.example.myapplication.prestador.viewmodel.presupuesto.SeccionPresupuesto
import com.example.myapplication.uishared.ui.components.AutoSizeText
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderMobileBar(
    codigoPresupuesto: String,
    onVolver: () -> Unit,
    onOpenPdf: () -> Unit
) {
    Surface(
        color = ArmadorPresupuestoTema.BackgroundDark,
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = ArmadorPresupuestoTema.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#EST-$codigoPresupuesto",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ArmadorPresupuestoTema.BrandOrange,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ArmadorPresupuestoTema.AccentEmerald)
                        )
                    }
                    Text(
                        text = "Presupuesto Eléctrico",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArmadorPresupuestoTema.TextPrimary
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenPdf,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.12f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "📄 Vista Previa",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArmadorPresupuestoTema.TextPrimary
                )
            }
        }
    }
}

@Composable
fun NavegadorPasosMobile(
    seccionActual: SeccionPresupuesto,
    cantidadItems: Int,
    onCambiarSeccion: (SeccionPresupuesto) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ItemTabPaso(
            titulo = "1. Cliente",
            icono = Icons.Default.Badge,
            activo = seccionActual == SeccionPresupuesto.IDENTIDAD,
            onClick = { onCambiarSeccion(SeccionPresupuesto.IDENTIDAD) },
            modifier = Modifier.weight(1f)
        )
        ItemTabPaso(
            titulo = "2. Ítems",
            icono = Icons.Default.Build,
            badge = "$cantidadItems",
            activo = seccionActual == SeccionPresupuesto.ITEMS,
            onClick = { onCambiarSeccion(SeccionPresupuesto.ITEMS) },
            modifier = Modifier.weight(1f)
        )
        ItemTabPaso(
            titulo = "3. Finanzas",
            icono = Icons.Default.Calculate,
            activo = seccionActual == SeccionPresupuesto.TOTALES,
            onClick = { onCambiarSeccion(SeccionPresupuesto.TOTALES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ItemTabPaso(
    titulo: String,
    icono: ImageVector,
    activo: Boolean,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (activo) ArmadorPresupuestoTema.BrandOrange.copy(alpha = 0.15f) else ArmadorPresupuestoTema.SurfaceCardSolid,
        border = BorderStroke(
            1.dp,
            if (activo) ArmadorPresupuestoTema.BrandOrange.copy(alpha = 0.4f) else ArmadorPresupuestoTema.BorderGlass
        )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (activo) ArmadorPresupuestoTema.BrandOrange else ArmadorPresupuestoTema.TextMuted,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = titulo,
                fontSize = 10.sp,
                fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (activo) ArmadorPresupuestoTema.BrandOrange else ArmadorPresupuestoTema.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ArmadorPresupuestoTema.BrandOrange)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun BarraFlotanteInferior(
    calculos: CalculadoraPresupuesto.ResultadoCalculo,
    seccionActual: SeccionPresupuesto,
    onSiguiente: () -> Unit,
    onEnviar: () -> Unit
) {
    val mostrarTotal = seccionActual != SeccionPresupuesto.IDENTIDAD

    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (mostrarTotal) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Tarjeta Glass: Total Estimado
                    Surface(
                        modifier = Modifier
                            .wrapContentSize()
                            .heightIn(min = 42.dp), // 🔥 LEY 4 OJOS: Altura mínima
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(
                                text = "TOTAL ESTIMADO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = ArmadorPresupuestoTema.BrandOrange,
                                letterSpacing = 0.5.sp
                            )
                            AutoSizeText( // 🔥 LEY 4 OJOS: Texto inteligente
                                text = "$ ${String.format(Locale.getDefault(), "%,.0f", calculos.totalGeneral)}",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    // Tarjeta Glass: Ahorro
                    if (calculos.montoDescuento > 0) {
                        Surface(
                            modifier = Modifier.wrapContentSize(),
                            color = ArmadorPresupuestoTema.AccentEmerald.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ArmadorPresupuestoTema.AccentEmerald.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = "AHORRO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ArmadorPresupuestoTema.AccentEmerald,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "-$ ${calculos.montoDescuento.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArmadorPresupuestoTema.AccentEmerald
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (seccionActual != SeccionPresupuesto.TOTALES) {
                IconButton(
                    onClick = onSiguiente,
                    modifier = Modifier
                        .sizeIn(minWidth = 54.dp, minHeight = 54.dp) // 🔥 LEY 4 OJOS
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ArmadorPresupuestoTema.BrandOrange, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Siguiente",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onEnviar,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArmadorPresupuestoTema.BrandOrange),
                    modifier = Modifier.heightIn(min = 54.dp).padding(horizontal = 8.dp) // 🔥 LEY 4 OJOS
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AutoSizeText( // 🔥 LEY 4 OJOS
                        text = "ENVIAR COTIZACIÓN",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

