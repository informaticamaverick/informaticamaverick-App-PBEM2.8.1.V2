package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.*
import java.util.Locale

@Composable
fun HeaderAccionItems(
    cantidadItems: Int,
    onAgregarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DESGLOSE DE TRABAJO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ArmadorPresupuestoTema.TextPrimary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "$cantidadItems ítems cotizados",
                fontSize = 10.sp,
                color = ArmadorPresupuestoTema.TextSecondary
            )
        }

        Button(
            onClick = onAgregarClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ArmadorPresupuestoTema.BrandOrange),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Añadir",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }
    }
}

@Composable
fun SeccionExpandibleMobile(
    titulo: String,
    icono: ImageVector,
    colorAcento: Color,
    conteo: Int,
    expandido: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    if (conteo == 0) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorAcento.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, null, tint = colorAcento, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = titulo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = ArmadorPresupuestoTema.TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "$conteo", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colorAcento)
                }
            }
            
            Icon(
                imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = ArmadorPresupuestoTema.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        if (expandido) {
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
fun SeccionListadoArticulosMobile(
    articulos: List<ArticuloPresupuesto>,
    onEliminar: (ArticuloPresupuesto) -> Unit,
    onEditar: (ArticuloPresupuesto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        articulos.forEach { item ->
            ItemCardMobile(
                titulo = item.descripcion,
                subtitulo = "${item.cantidad} un. x $ ${item.precioUnitario.toInt()}",
                total = (item.precioUnitario * item.cantidad) - (item.montoDescuento * item.cantidad) - ((item.precioUnitario * item.cantidad - item.montoDescuento * item.cantidad) * (item.porcentajeDescuento / 100.0)),
                esMaterial = true,
                precioOriginal = item.precioUnitario,
                porcentajeDescuento = item.porcentajeDescuento,
                montoDescuentoFijo = item.montoDescuento,
                cantidad = item.cantidad,
                onEliminar = { onEliminar(item) },
                onClick = { onEditar(item) }
            )
        }
    }
}

@Composable
fun SeccionListadoServiciosMobile(
    servicios: List<ServicioPresupuesto>,
    onEliminar: (ServicioPresupuesto) -> Unit,
    onEditar: (ServicioPresupuesto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        servicios.forEach { item ->
            ItemCardMobile(
                titulo = item.descripcion,
                subtitulo = "Mano de obra especializada",
                total = item.total,
                esMaterial = false,
                precioOriginal = item.precioUnitario,
                porcentajeDescuento = item.porcentajeDescuento,
                montoDescuentoFijo = item.montoDescuento,
                onEliminar = { onEliminar(item) },
                onClick = { onEditar(item) }
            )
        }
    }
}

@Composable
fun SeccionListadoGastosMobile(
    gastos: List<GastoVarioPresupuesto>,
    onEliminar: (GastoVarioPresupuesto) -> Unit,
    onEditar: (GastoVarioPresupuesto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        gastos.forEach { item ->
            ItemCardMobile(
                titulo = item.descripcion,
                subtitulo = "Gasto logístico / extraordinario",
                total = item.monto,
                esMaterial = false,
                iconOverride = Icons.Default.LocalShipping,
                iconColorOverride = ArmadorPresupuestoTema.AccentAmber,
                precioOriginal = item.precioUnitario,
                porcentajeDescuento = item.porcentajeDescuento,
                montoDescuentoFijo = item.montoDescuento,
                onEliminar = { onEliminar(item) },
                onClick = { onEditar(item) }
            )
        }
    }
}

@Composable
private fun ItemCardMobile(
    titulo: String,
    subtitulo: String,
    total: Double,
    esMaterial: Boolean,
    iconOverride: ImageVector? = null,
    iconColorOverride: Color? = null,
    precioOriginal: Double = 0.0,
    porcentajeDescuento: Double = 0.0,
    montoDescuentoFijo: Double = 0.0,
    cantidad: Int = 1,
    onEliminar: () -> Unit,
    onClick: () -> Unit
) {
    val tieneDescuento = porcentajeDescuento > 0 || montoDescuentoFijo > 0

    TarjetaBentoContenedor {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconColor = iconColorOverride ?: if (esMaterial) ArmadorPresupuestoTema.BrandOrange else ArmadorPresupuestoTema.AccentCyan
                val icon = iconOverride ?: if (esMaterial) Icons.Default.Inventory2 else Icons.Default.Build
                
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = titulo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArmadorPresupuestoTema.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitulo,
                        fontSize = 10.sp,
                        color = ArmadorPresupuestoTema.TextSecondary
                    )
                    if (tieneDescuento) {
                        val textoDescuento = if (montoDescuentoFijo > 0) {
                            "Descuento: -$ ${montoDescuentoFijo.toInt()}"
                        } else {
                            "Descuento: ${porcentajeDescuento.toInt()}%"
                        }
                        Text(
                            text = textoDescuento,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArmadorPresupuestoTema.AccentEmerald,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                    if (tieneDescuento) {
                        val baseUnit = precioOriginal * cantidad
                        Text(
                            text = "$ ${baseUnit.toInt()}",
                            fontSize = 9.sp,
                            color = ArmadorPresupuestoTema.TextMuted,
                            textDecoration = TextDecoration.LineThrough,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                    Text(
                        text = "$ ${String.format(Locale.getDefault(), "%,.0f", total)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tieneDescuento) ArmadorPresupuestoTema.AccentEmerald else ArmadorPresupuestoTema.TextPrimary,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = { onEliminar() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = ArmadorPresupuestoTema.TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
