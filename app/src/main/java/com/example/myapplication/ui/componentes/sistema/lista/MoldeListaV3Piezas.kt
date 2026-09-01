package com.example.myapplication.ui.componentes.sistema.lista

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.uishared.estilos.AppIcons
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.ui.componentes.sistema.shakeClick
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * MoldeListaV3Piezas.kt
 * Propósito: Definir los átomos visuales (piezas) del sistema de listas V3.
 * Funcionamiento: Componentes stateless optimizados con lectura diferida de estado.
 * Relación: Parte fundamental del Protocolo Maverick Elite para UI reactiva.
 */

// ==================================================================================
// --- PIEZA 1: BURBUJA DE PERFIL ELITE V3 ---
// ==================================================================================

// [REEMPLAZADO POR BurbujaPerfilElite en sistema/perfil/BurbujaPerfil.kt]

// ==================================================================================
// --- PIEZA 2: CONTADOR DE RESULTADOS V3 ---
// ==================================================================================

@Composable
fun ContadorResultadosV3(
    modifier: Modifier = Modifier,
    cantidad: Int,
    proveedorColapso: () -> Float = { 0f },
    colorAcento: Color = SharedPalette.ElectricCyan
) {
    val fraccionColapso = proveedorColapso()
    
    val tamanoFuenteNumero by animateFloatAsState(
        targetValue = if (fraccionColapso < 0.6f) 30f else 18f,
        label = "TamanoNumeroContadorV3"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = cantidad.toString().padStart(2, '0'),
            style = AppTypography.HeaderTitle.copy(
                fontSize = tamanoFuenteNumero.sp,
                color = colorAcento,
                fontWeight = FontWeight.Black,
                lineHeight = (tamanoFuenteNumero * 0.85f).sp
            )
        )
        // La etiqueta desaparece suavemente al colapsar
        Text(
            text = "RESULT",
            style = AppTypography.HeaderSubtitle.copy(
                fontSize = 7.sp,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            ),
            modifier = Modifier
                .offset(y = (-3).dp)
                .graphicsLayer {
                    alpha = (1f - fraccionColapso * 3f).coerceIn(0f, 1f)
                }
        )
    }
}

// ==================================================================================
// --- PIEZA 3: BOTÓN DE ACCIÓN V3 ---
// ==================================================================================

@Composable
fun BotonAccionV3(
    modifier: Modifier = Modifier,
    alHacerClick: () -> Unit,
    icono: ImageVector,
    colorIcono: Color = SharedPalette.NeonCyan,
    proveedorColapso: () -> Float = { 0f }
) {
    val fraccionColapso = proveedorColapso()
    
    val tamanoBoton by animateDpAsState(
        targetValue = if (fraccionColapso > 0.7f) 28.dp else 32.dp,
        label = "TamanoBotonV3"
    )
    
    Box(
        modifier = modifier
            .size(tamanoBoton)
            .clip(CircleShape)
            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
            .border(0.8.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .shakeClick { alHacerClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(if (fraccionColapso > 0.7f) 14.dp else 16.dp)
        )
    }
}

// ==================================================================================
// --- PREVIEWS (LEY #10: SCREEN ANATOMY) ---
// ==================================================================================

@Preview(name = "Piezas V3 - Vista General", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewPiezasV3() {
    PBEMTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SharedPalette.V2VantaBlack)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PIEZAS ATÓMICAS V3", color = Color.Gray, style = AppTypography.HeaderSubtitle)
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                MoldeBurbujaPerfilV3(
                    perfil = PerfilIdentidadV3("1", "App", "AP", colorAcento = SharedPalette.ElectricCyan, conteoNoLeidos = 5),
                    tamanoBase = 48.dp
                )
                
                MoldeBurbujaPerfilV3(
                    perfil = PerfilIdentidadV3("2", "Cyberdyne", "CD", colorAcento = SharedPalette.NeonCyan),
                    tamanoBase = 48.dp
                )
            }
            
            ContadorResultadosV3(cantidad = 42)
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BotonAccionV3(icono = AppIcons.Add, colorIcono = SharedPalette.AcidGreen, alHacerClick = {})
                BotonAccionV3(icono = AppIcons.Delete, colorIcono = SharedPalette.DeepRed, alHacerClick = {})
                BotonAccionV3(icono = AppIcons.Refresh, alHacerClick = {})
            }
        }
    }
}

@Preview(name = "Burbuja Perfil - Estados", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewBurbujaPerfilV3Estados() {
    PBEMTheme {
        Row(
            modifier = Modifier.padding(16.dp).background(SharedPalette.VantaBlack),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium
            MoldeBurbujaPerfilV3(
                perfil = PerfilIdentidadV3("1", "Empresa A", "EA", colorAcento = SharedPalette.AcidGreen, conteoNoLeidos = 2, esSuscripto = true),
                tamanoBase = 48.dp
            )
            // Estándar
            MoldeBurbujaPerfilV3(
                perfil = PerfilIdentidadV3("2", "Cyberdyne", "CD", colorAcento = SharedPalette.ElectricCyan),
                tamanoBase = 48.dp
            )
            // Verificado
            MoldeBurbujaPerfilV3(
                perfil = PerfilIdentidadV3("3", "Empresa C", "EC", colorAcento = SharedPalette.MagentaNeon, estaVerificado = true),
                tamanoBase = 48.dp
            )
        }
    }
}
