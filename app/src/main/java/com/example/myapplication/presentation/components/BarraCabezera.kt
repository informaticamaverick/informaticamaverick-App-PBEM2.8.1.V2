package com.example.myapplication.presentation.components

// === IMPORTS ===
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.shakeClick
import com.example.myapplication.presentation.designsystem.components.AutoSizeText

// === SECCIÓN: COMPONENTE PRINCIPAL ===

/**
 * Barra de Encabezado Maverick V5 (ELITE HUD).
 * Estilo: Obsidian Glass, Luminous Borders, Technical HUD components.
 * Optimizada para transiciones fluidas y estética de élite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraCabezera(
    title: String,
    subtitle: String,
    emoji: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onInfoClick: (() -> Unit)? = null,
    collapseFraction: Float = 0f,
    accentColor: Color = MaverickColors.ElectricCyan,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    backgroundBrush: Brush = Brush.verticalGradient(
        listOf(MaverickColors.V2DeepVoid.copy(alpha = 0.96f), MaverickColors.ROG_Dark_Bg.copy(alpha = 0.98f))
    ),
    infoTitle: String = "SISTEMA MAVERICK",
    infoDescription: String = "Acceso a funcionalidades de élite y gestión de recursos del sistema."
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    // --- CÁLCULOS DINÁMICOS (LERP) ---
    // Interpolación lineal entre estado expandido y colapsado
    val backButtonSize = lerp(42.dp, 34.dp, collapseFraction)
    val emojiFontSize = lerp(60.sp, 34.sp, collapseFraction)
    val emojiEndPadding = lerp(16.dp, 0.dp, collapseFraction)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CutCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(backgroundBrush)
            .drawBehind {
                // 1. EFECTO DE PROFUNDIDAD SUPERIOR
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                        startY = 0f,
                        endY = 85.dp.toPx()
                    )
                )

                // --- INTEGRACIÓN HUD ELITE (Dibujo técnico) ---
                val strokeWidth = 1.2.dp.toPx()
                val cornerSize = 16.dp.toPx()

                // Formatura del borde HUD (Base del componente)
                val path = Path().apply {
                    moveTo(0f, size.height - cornerSize)
                    lineTo(cornerSize, size.height)
                    lineTo(size.width - cornerSize, size.height)
                    lineTo(size.width, size.height - cornerSize)
                }

                // A. RESPLANDOR DE IDENTIDAD (Ambient Glow estilo V2) - COMENTADO POR REQUERIMIENTO
                /*
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(40.dp.toPx(), size.height / 2),
                        radius = size.width / 2
                    ),
                    center = Offset(40.dp.toPx(), size.height / 2),
                    radius = size.width / 2
                )
                */

                // B. EFECTO GLOW CENTRAL BASE - COMENTADO POR REQUERIMIENTO
                /*
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2, size.height),
                        radius = size.width / 1.5f
                    ),
                    alpha = 0.4f
                )
                */

                // C. LUMINOSIDAD GRADIENTE MAVERICK (Borde neón dinámico)
                val borderGradient = Brush.horizontalGradient(
                    0.0f to accentColor.copy(alpha = 0.05f),
                    0.2f to accentColor,
                    0.5f to MaverickColors.ElectricCyan,
                    0.8f to accentColor,
                    1.0f to accentColor.copy(alpha = 0.05f)
                )

                // D. LÍNEA SÓLIDA PRINCIPAL
                drawPath(
                    path = path,
                    brush = borderGradient,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )

                // E. GLOW TENUE ADAPTATIVO (Resplandor de borde)
                drawPath(
                    path = path,
                    brush = borderGradient,
                    style = Stroke(
                        width = strokeWidth * 3f,
                        cap = StrokeCap.Round
                    ),
                    alpha = 0.12f
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                // Altura dinámica del contenido interno según el colapso
                .height(80.dp * (1f - (collapseFraction * 0.4f))) 
        ) {
            // --- EMOJI DE FONDO (Diseño reactivo) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = emojiEndPadding)
                    .graphicsLayer { 
                        alpha = 1f 
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = emoji,
                    fontSize = emojiFontSize
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- BOTÓN VOLVER (Estilo Táctico) ---
                Box(
                    modifier = Modifier
                        .size(backButtonSize)
                        .clip(CutCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, borderColor, CutCornerShape(8.dp))
                        .shakeClick { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaverickIcons.Back,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(lerp(20.dp, 18.dp, collapseFraction))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    // --- SUBTÍTULO E INFO ICON (Data técnica) ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (collapseFraction < 0.6f) {
                            Text(
                                text = subtitle.uppercase(),
                                style = CyberTypography.MonospaceData.copy(
                                    color = accentColor.copy(alpha = 0.85f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                modifier = Modifier.graphicsLayer { alpha = 1f - (collapseFraction * 2.5f).coerceIn(0f, 1f) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // ICONO DE INFO (Tactical Alert)
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer { alpha = 1f - (collapseFraction * 2.5f).coerceIn(0f, 1f) }
                                    .shakeClick {
                                        if (onInfoClick != null) onInfoClick() else showInfoDialog = true 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("❗", fontSize = 11.sp)
                            }
                        }
                    }

                    // --- TÍTULO PRINCIPAL (AutoSize) ---
                    AutoSizeText(
                        text = title.uppercase(),
                        maxLines = 2,
                        textAlign = TextAlign.Start,
                        style = CyberTypography.TitleTech.copy(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            lineHeight = 20.sp
                        )
                    )
                }

                // --- ESPACIADOR DE SIMETRÍA ELIMINADO PARA ALINEACIÓN IZQUIERDA ---
            }
        }
    }

    // DIÁLOGO INFORMATIVO MAVERICK ELITE
    if (showInfoDialog) {
        MaverickInfoDialog(
            title = infoTitle,
            description = infoDescription,
            onDismiss = { showInfoDialog = false }
        )
    }
}

// === SECCIÓN: COMPONENTES DE APOYO ===

/**
 * MaverickInfoDialog: Diálogo informativo con estética técnica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaverickInfoDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(CutCornerShape(20.dp))
                .background(MaverickColors.ROG_Dark_Bg)
                .border(1.5.dp, MaverickColors.ElectricCyan.copy(alpha = 0.6f), CutCornerShape(20.dp))
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaverickColors.ElectricCyan.copy(alpha = 0.1f))
                        .border(1.dp, MaverickColors.ElectricCyan.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ℹ️", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = title.uppercase(),
                    style = CyberTypography.TitleTech.copy(
                        fontSize = 18.sp,
                        color = MaverickColors.ElectricCyan,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Divider neón sutil
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, MaverickColors.ElectricCyan, Color.Transparent)
                            )
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = description,
                    style = CyberTypography.BodyCyber.copy(
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Button Close
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaverickColors.ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, MaverickColors.ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .shakeClick { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ENTENDIDO",
                        style = CyberTypography.MonospaceData.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

// === SECCIÓN: PREVIEWS ===

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun BarraCabezeraV5Preview() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF05070A)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BarraCabezera(
            title = "Calendario de Actividades Elite y Gestión de Recursos Maverick",
            subtitle = "Agenda de compromisos",
            emoji = "🗓️",
            onBack = {},
            collapseFraction = 0f,
            infoDescription = "Gestiona tus citas y eventos con la precisión Maverick."
        )
        
        BarraCabezera(
            title = "Historial",
            subtitle = "Registro de actividades",
            emoji = "📜",
            onBack = {},
            collapseFraction = 1f,
            accentColor = MaverickColors.ElectricPurple
        )
    }
}
