package com.example.myapplication.presentation.designsystem.components
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.features.home.AppNavigation















import com.example.myapplication.presentation.features.auth.*

import com.example.myapplication.presentation.features.home.*

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull.content

// ==========================================================================================
// 🔘 SECCIÓN 1: BOTONES RECTANGULARES (MAVERICK BASE)
// ==========================================================================================

/**
 * Botón base con estilo Maverick: Cristal, bordes brillantes y animación shake.
 */
@Composable
fun MaverickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    backgroundColor: Color = MaverickColors.BentoDarkGlassBackground,
    accentColor: Color = MaverickColors.NeonCyan,
    textColor: Color = Color.White,
    height: Dp = 56.dp,
    cornerRadius: Dp = 16.dp,
    fontSize: TextUnit = 16.sp,
    paddingHorizontal: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = accentColor,
                ambientColor = Color.Transparent
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .background(MaverickColors.BentoGlassBrush)
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(cornerRadius))
            .shakeClick { onClick() }
            .padding(horizontal = paddingHorizontal),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = (fontSize.value + 4).sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Versión del botón rectangular SIN NEÓN (sin sombra brillante).
 */
@Composable
fun MaverickButtonSimple(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    backgroundColor: Color = MaverickColors.BentoDarkGlassBackground,
    accentColor: Color = MaverickColors.NeonCyan,
    textColor: Color = Color.White,
    height: Dp = 56.dp,
    cornerRadius: Dp = 16.dp,
    fontSize: TextUnit = 16.sp,
    paddingHorizontal: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .background(MaverickColors.BentoGlassBrush)
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(cornerRadius))
            .shakeClick { onClick() }
            .padding(horizontal = paddingHorizontal),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = (fontSize.value + 4).sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Variantes rápidas de botones rectangulares
 */
object BotonesPremium {
    @Composable
    fun Aceptar(onClick: () -> Unit, modifier: Modifier = Modifier, text: String = "ACEPTAR") =
        MaverickButton(text, onClick, modifier, emoji = "✅", accentColor = MaverickColors.AcidGreen)

    @Composable
    fun Cancelar(onClick: () -> Unit, modifier: Modifier = Modifier, text: String = "CANCELAR") =
        MaverickButton(text, onClick, modifier, emoji = "❌", accentColor = MaverickColors.DeepRed)

    @Composable
    fun Enviar(onClick: () -> Unit, modifier: Modifier = Modifier, text: String = "ENVIAR") =
        MaverickButton(text, onClick, modifier, emoji = "🚀", accentColor = MaverickColors.GeminiAccent)

    @Composable
    fun Generico(text: String, emoji: String, accentColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) =
        MaverickButton(text, onClick, modifier, emoji = emoji, accentColor = accentColor)

    @Composable
    fun Simple(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, emoji: String? = null, accentColor: Color = MaverickColors.NeonCyan) =
        MaverickButtonSimple(text, onClick, modifier, emoji = emoji, accentColor = accentColor)
}

// ==========================================================================================
// 🔵 SECCIÓN 2: BOTONES CIRCULARES (ROUND & BENTO ACTIONS)
// ==========================================================================================

/**
 * Botón redondo base con estilo Maverick: Cristal, bordes brillantes y etiqueta opcional.
 */
@Composable
fun MaverickRoundButton(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaverickColors.NeonCyan,
    buttonSize: Dp = 64.dp,
    emojiSize: TextUnit = 28.sp,
    labelColor: Color = Color.White.copy(alpha = 0.8f),
    labelFontSize: TextUnit = 12.sp,
    showLabel: Boolean = true,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = accentColor,
                    ambientColor = Color.Transparent
                )
                .clip(CircleShape)
                .background(MaverickColors.BentoDarkGlassBackground)
                .background(MaverickColors.BentoGlassBrush)
                .border(1.5.dp, MaverickColors.BentoBorderBrush, CircleShape)
                .background(accentColor.copy(alpha = 0.05f))
                .shakeClick { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (content != null) {
                content()
            } else {
                Text(text = emoji, fontSize = emojiSize)
            }
        }

        if (showLabel && label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = labelColor,
                fontSize = labelFontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MaverickRoundButtonSimple(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaverickColors.NeonCyan,
    buttonSize: Dp = 64.dp,
    emojiSize: TextUnit = 28.sp,
    labelColor: Color = Color.White.copy(alpha = 0.8f),
    labelFontSize: TextUnit = 12.sp,
    showLabel: Boolean = true,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    // ==========================================================================================
    // 🛡️ SECCIÓN: CONFIGURACIÓN TÁCTICA HUD (MAVERICK SUTIL STYLE V2)
    // ==========================================================================================
    // Borde más fino y gradiente sutil para no sobrecargar el HUD
    val borderGradient = Brush.linearGradient(
        listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(2.dp) // Reducido para máxima compacidad en HUD
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                // --- CAPA 1: BLUR DE COLOR (BASADO EN EL ICONO) ---
                // Reemplazamos la sombra blanca/plateada por un glow suave del color referido
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent),
                            center = center,
                            radius = size.maxDimension / 1.2f
                        ),
                        radius = size.maxDimension / 1.2f
                    )
                }
                .clip(CircleShape)
                // --- CAPA 2: FONDO MAVERICK CRISTAL (IGUAL QUE APPNAVIGATION) ---
                // Fondo oscuro bento + efecto cristal vertical
                .background(MaverickColors.ROG_Dark_Bg)
                .background(MaverickColors.BentoGlassBrush)
                // --- CAPA 3: BORDE SUTIL (Fino 0.5dp) ---
                .border(BorderStroke(0.5.dp, borderGradient), CircleShape)
                .background(accentColor.copy(alpha = 0.01f)) // Tinte mínimo casi imperceptible
                .shakeClick { onClick() }, // Efecto Shake al tocar (Estilo AppNavigation)
            contentAlignment = Alignment.Center
        ) {
            // Capa extra de profundidad (Sutil Glow Interior)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(accentColor.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
            )

            if (content != null) {
                content()
            } else {
                Text(text = emoji, fontSize = emojiSize)
            }
        }

        // ==========================================================================================
        // 📝 SECCIÓN: ETIQUETA SUTIL
        // ==========================================================================================
        if (showLabel && label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = labelColor,
                fontSize = labelFontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Colección de acciones circulares predefinidas (Iconos Bento)
 */
object IconosAccion {
    @Composable fun Presupuesto(showLabel: Boolean = true, onClick: () -> Unit) =
        CarcasaAccionBento("💰", if (showLabel) "PRESUPUESTO" else "", onClick = onClick, accentColor = MaverickColors.GoldPremium)

    @Composable fun Aceptar(showLabel: Boolean = true, onClick: () -> Unit) =
        CarcasaAccionBento("✅", if (showLabel) "ACEPTAR" else "", onClick = onClick, accentColor = MaverickColors.AcidGreen)

    @Composable fun Cancelar(showLabel: Boolean = true, onClick: () -> Unit) =
        CarcasaAccionBento("❌", if (showLabel) "CERRAR" else "", onClick = onClick, accentColor = MaverickColors.DeepRed)

    @Composable fun Editar(showLabel: Boolean = true, onClick: () -> Unit) =
        CarcasaAccionBento("✏️", if (showLabel) "EDITAR" else "", onClick = onClick, accentColor = MaverickColors.ElectricViolet)

    @Composable fun Eliminar(showLabel: Boolean = true, onClick: () -> Unit) =
        CarcasaAccionBento("🗑️", if (showLabel) "BORRAR" else "", onClick = onClick, accentColor = MaverickColors.Garnet)

    @Composable fun Mensaje(showLabel: Boolean = true, onClick: () -> Unit) =
        CarcasaAccionBento("💬", if (showLabel) "MENSAJE" else "", onClick = onClick, accentColor = MaverickColors.NeonCyan)

    @Composable fun Generico(emoji: String, label: String = "", accentColor: Color = Color.White, onClick: () -> Unit) =
        CarcasaAccionBento(emoji, label, onClick = onClick, accentColor = accentColor)
}

@Composable
fun CarcasaAccionBento(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White,
    onClick: () -> Unit,
    size: Dp = 64.dp,
    emojiSize: TextUnit = 28.sp,
    showLabel: Boolean = true
) {
    MaverickRoundButton(
        emoji = emoji,
        label = label,
        onClick = onClick,
        modifier = modifier,
        accentColor = accentColor,
        buttonSize = size,
        emojiSize = emojiSize,
        showLabel = showLabel
    )
}

// ==========================================================================================
// 🌪️ SECCIÓN 3: BOTONES TÁCTICOS Y HUD (ASISTENTE BE)
// ==========================================================================================

@Composable
fun MaverickTacticalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    isActive: Boolean = true,
    accentColor: Color? = null,
    backgroundColor: Color = Color(0xFF1A1F26),
    content: @Composable BoxScope.() -> Unit
) {
    // --- 📐 SECCIÓN: CONFIGURACIÓN DE FORMA TÁCTICA (CASI CUADRADA) ---
    val tacticalShape = CutCornerShape(4.dp)

    val borderGradient = if (accentColor != null && isActive) {
        Brush.linearGradient(listOf(accentColor.copy(alpha = 0.8f), Color.Transparent))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent))
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isActive) 8.dp else 4.dp,
                shape = tacticalShape,
                ambientColor = Color.Transparent
            )
            .clip(tacticalShape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderGradient), tacticalShape)
            .shakeClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.alpha(if (isActive) 1f else 0.4f),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/** HELPER: Envuelve los botones con una etiqueta técnica pequeña */
@Composable
fun HUDActionItem(
    label: String,
    active: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(IntrinsicSize.Min)
    ) {
        Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
            content()
        }
        Text(
            text = label.uppercase(),
            style = CyberTypography.MonospaceData.copy(
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) MaverickColors.NeonCyan else Color.Gray.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ---------------------------------------------------------
// BOTONES DE CONTROL DE VISTA Y FILTRO HUD
// ---------------------------------------------------------

/**
 * BOTÓN DE VISTAS (Grupos / Grilla)
 */
@Composable
fun BotonVista(
    isBentoView: Boolean,
    isActive: Boolean,
    onToggleView: () -> Unit
) {
    MaverickTacticalButton(
        isActive = isActive,
        onClick = onToggleView,
        accentColor = Color(0xFF2197F5)
    ) {
        Text(text = if (isBentoView) "🍱" else "📱", fontSize = 20.sp)
    }
}

/**
 * BOTÓN FILTRO SUSCRITOS PREMIUM (👑 / 👥)
 * Controla el filtrado de prestadores suscriptos con emojis dinámicos y animaciones tácticas.
 */
@Composable
fun BotonFiltroSuscritosPremium(
    isActive: Boolean,
    onClick: () -> Unit
) {
    MaverickTacticalButton(
        isActive = isActive,
        accentColor = Color(0xFF2197F5),
        onClick = onClick
    ) {
        Text(text = if (isActive) "👑" else "👥", fontSize = 20.sp)
    }
}

@Composable
fun BeSmallActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    isSelected: Boolean = false,
    tint: Color = Color.White
) {
    // --- DETERMINAMOS EL COLOR DE ACENTO BASADO EN LA SELECCIÓN ---
    val accentColor = if (isSelected) MaverickColors.NeonCyan else tint

    // ==========================================================================================
    // 🛡️ SECCIÓN: BOTÓN SIN CONTENEDOR (SOLO EMOJI + TEXTO)
    // ==========================================================================================
    // Se elimina MaverickRoundButtonSimple para dejar solo el icono y el texto solicitado.
    // Se añade .shakeClick {} de EfectosAnimaciones.kt para el efecto de movimiento al pulsar.
    Column(
        modifier = modifier
            .shakeClick { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- SECCIÓN: ICONO O EMOJI ---
        if (emoji != null) {
            Text(
                text = emoji,
                fontSize = 26.sp, // Tamaño ajustado para visibilidad sin círculo
                modifier = Modifier.padding(bottom = 1.dp) // Separación de 1dp solicitada
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 1.dp) // Separación de 1dp solicitada
            )
        }

        // --- SECCIÓN: TEXTO DESCRIPTIVO ---
        Text(
            text = label.uppercase(),
            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.8f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BeActionDivider() {
    PremiumVerticalDivider(modifier = Modifier.padding(horizontal = 2.dp), height = 36.dp)
}

/**
 * 🛠️ SECCIÓN: NUEVO COMPONENTE HUD COMPACTO
 * Botón con etiqueta integrada en semi-esfera inferior.
 */
@Composable
fun MaverickCompactHudButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    accentColor: Color = MaverickColors.NeonCyan,
    buttonSize: Dp = 52.dp,
    contentColor: Color = Color.White
) {
    val borderGradient = Brush.linearGradient(
        listOf(accentColor.copy(alpha = 0.5f), Color.Transparent)
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(MaverickColors.ROG_Dark_Bg)
            .background(MaverickColors.BentoGlassBrush)
            .border(BorderStroke(0.5.dp, borderGradient), CircleShape)
            .shakeClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // --- CAPA 1: ICONO / EMOJI (Elevado y Grande - Efecto "Dentro del sobre") ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp), // Elevamos el icono
            contentAlignment = Alignment.TopCenter
        ) {
            if (emoji != null) {
                Text(text = emoji, fontSize = (buttonSize.value * 0.6f).sp)
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) accentColor else contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size((buttonSize.value * 0.55f).dp)
                )
            }
        }

        // --- CAPA 2: SEMI-ESFERA INFERIOR (EL SOBRE / ETIQUETA) ---
        // Ocupa la parte inferior, es opaca para tapar el icono y dar efecto de profundidad
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.38f) // Área reducida para el texto
                .background(MaverickColors.ROG_Dark_Bg) // Fondo sólido para ocultar el icono detrás
                .background(
                    brush = Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.25f), accentColor.copy(alpha = 0.5f))
                    )
                )
                .drawBehind {
                    // Línea divisoria marcada (Borde superior del sobre)
                    drawLine(
                        color = accentColor.copy(alpha = 0.7f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.2.dp.toPx()
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.uppercase(),
                color = Color.White,
                fontSize = 7.5.sp, // Ligeramente más pequeño para el nuevo espacio
                style = TextStyle(fontWeight = FontWeight.Black, letterSpacing = 0.4.sp),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PreviewMaverickCompactHudButton() {
    Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MaverickCompactHudButton("SCAN", {}, emoji = "🔍")
        MaverickCompactHudButton("LOCK", {}, icon = Icons.Default.Lock, accentColor = MaverickColors.DeepRed)
        MaverickCompactHudButton("USER", {}, icon = Icons.Default.Person, isSelected = true, accentColor = MaverickColors.GoldPremium)
    }
}

// ==========================================================================================
// 💎 SECCIÓN 4: COMPONENTES BENTO (PILL & ACTION)
// ==========================================================================================

/**
 * BtnEliteAndroid13: Botón con estética Android 13/14 y sombra 3D profunda.
 */
@Composable
fun BtnEliteAndroid13(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaverickColors.ROG_Dark_Bg,
    contentColor: Color = Color.White,
    accentColor: Color = MaverickColors.NeonCyan
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ElevationAnim"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "ScaleAnim"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(20.dp),
                spotColor = accentColor.copy(alpha = 0.5f),
                ambientColor = Color.Black
            )
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = contentColor
            )
        )
    }
}

@Composable
fun BentoActionButton(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    color: Color = MaverickColors.GeminiAccent,
    fontSize: TextUnit = 16.sp,
    emojiSize: TextUnit = 20.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(MaverickColors.BentoDarkGlassBackground)
            .background(MaverickColors.BentoGlassBrush)
            .background(color.copy(alpha = 0.15f))
            .border(1.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .shakeClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = emojiSize)
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun BentoPillMenu(
    items: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(35.dp, RoundedCornerShape(50), ambientColor = Color.Black, spotColor = MaverickColors.GeminiAccent)
            .clip(RoundedCornerShape(50))
            .background(MaverickColors.BentoDarkGlassBackground)
            .background(MaverickColors.BentoGlassBrush)
            .border(1.5.dp, MaverickColors.BentoBorderBrush, RoundedCornerShape(50))
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Text(
                text = item.first,
                fontSize = 24.sp,
                modifier = Modifier.shakeClick { item.second() }
            )
            if (index < items.lastIndex) {
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.White.copy(alpha = 0.2f)))
            }
        }
    }
}

// ==========================================================================================
// 📱 SECCIÓN 5: BOTONES MATERIAL 3 ADAPTATIVOS
// ==========================================================================================

@Composable
fun MaverickM3AdaptiveIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaverickColors.GlassWhite,
    iconColor: Color = Color.White,
    size: Dp = 48.dp,
    padding: Dp = 12.dp,
    onClick: (() -> Unit)? = null
) {
    val baseModifier = modifier
        .size(size)
        .clip(CircleShape)
        .background(containerColor)
        .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)

    val finalModifier = if (onClick != null) baseModifier.shakeClick { onClick() } else baseModifier

    Box(
        modifier = finalModifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
}



// ==========================================================================================
// 🎛️ SECCIÓN 6: BOTONES DE ACCIÓN VECTORIALES (3 ESTILOS)
// ==========================================================================================

// --- 1. ACEPTAR ---
@Composable fun BtnAcceptRog(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).clip(CutCornerShape(8.dp)).background(CyberColorsV3.SuccessGreen.copy(alpha = 0.1f)).border(1.dp, CyberColorsV3.SuccessGreen, CutCornerShape(8.dp)).clickable { onClick() }) { androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) { val path = Path().apply { moveTo(0f, size.height/2); lineTo(size.width/3, size.height); lineTo(size.width, 0f) }; drawPath(path, CyberColorsV3.SuccessGreen, style = Stroke(width = 2.dp.toPx())) } } }
@Composable fun BtnAcceptStealth(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, contentDescription = "Accept", tint = CyberColorsV3.SuccessGreen) } }
@Composable fun BtnAcceptEmoji(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).border(1.dp, CyberColorsV3.SuccessGreen.copy(alpha = 0.5f), RectangleShape).background(Color.Black).clickable { onClick() }, contentAlignment = Alignment.Center) { Text("✅", fontSize = 16.sp) } }

// --- 2. CANCELAR ---
@Composable fun BtnCancelRog(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).clip(CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)).background(CyberColorsV3.WarningRed.copy(alpha = 0.1f)).clickable { onClick() }) { androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) { drawLine(CyberColorsV3.WarningRed, Offset(0f, 0f), Offset(size.width, size.height), 2.dp.toPx()); drawLine(CyberColorsV3.WarningRed, Offset(size.width, 0f), Offset(0f, size.height), 2.dp.toPx()) } } }
@Composable fun BtnCancelStealth(modifier: Modifier = Modifier, onClick: () -> Unit = {}) { Box(modifier = modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)).border(0.5.dp, MaverickColors.AsPanelBorder, CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, contentDescription = "Cancel", tint = CyberColorsV3.WarningRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp)) } }
@Composable fun BtnCancelEmoji(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).border(2.dp, CyberColorsV3.WarningRed.copy(alpha = 0.3f), RectangleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Text("❌", fontSize = 14.sp) } }

// --- 3. LIMPIAR ---
@Composable fun BtnClearRog(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).border(1.dp, CyberColorsV3.ElectricCyan, CutCornerShape(bottomEnd = 12.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) { drawLine(CyberColorsV3.ElectricCyan, Offset(0f, size.height/2), Offset(size.width, size.height/2), 2.dp.toPx()); drawLine(CyberColorsV3.ElectricCyan, Offset(size.width*0.6f, 0f), Offset(size.width, size.height/2), 2.dp.toPx()); drawLine(CyberColorsV3.ElectricCyan, Offset(size.width*0.6f, size.height), Offset(size.width, size.height/2), 2.dp.toPx()) } } }
@Composable fun BtnClearStealth(modifier: Modifier = Modifier, onClick: () -> Unit = {}) { Box(modifier = modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)).border(0.5.dp, CyberColorsV3.ElectricCyan.copy(alpha = 0.4f), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = CyberColorsV3.ElectricCyan, modifier = Modifier.size(16.dp)) } }
@Composable fun BtnClearEmoji(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).background(CyberColorsV3.DeepVoid).border(1.dp, Color.DarkGray).clickable { onClick() }, contentAlignment = Alignment.Center) { Text("🧹", fontSize = 16.sp) } }

// --- 4. BORRAR ---
@Composable fun BtnDeleteRog(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).background(CyberColorsV3.WarningRed.copy(alpha = 0.05f)).border(1.dp, CyberColorsV3.WarningRed, CutCornerShape(topStart = 8.dp, topEnd = 8.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) { drawRect(CyberColorsV3.WarningRed, Offset(size.width*0.2f, size.height*0.3f), Size(size.width*0.6f, size.height*0.7f), style = Stroke(2.dp.toPx())); drawLine(CyberColorsV3.WarningRed, Offset(0f, size.height*0.3f), Offset(size.width, size.height*0.3f), 2.dp.toPx()); drawLine(CyberColorsV3.WarningRed, Offset(size.width*0.4f, 0f), Offset(size.width*0.6f, 0f), 2.dp.toPx()) } } }
@Composable fun BtnDeleteStealth(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberColorsV3.WarningRed.copy(alpha = 0.6f)) } }
@Composable fun BtnDeleteEmoji(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).background(Color.Black).clickable { onClick() }, contentAlignment = Alignment.Center) { Text("[ 🗑️ ]", style = CyberTypography.MonospaceData.copy(color = CyberColorsV3.WarningRed)) } }

// --- 5. ENVIAR ---
@Composable fun BtnSendRog(onClick: () -> Unit = {}) { Box(modifier = Modifier.width(60.dp).height(40.dp).clip(CutCornerShape(bottomEnd = 16.dp)).background(CyberColorsV3.NeonMagenta).clickable { onClick() }, contentAlignment = Alignment.Center) { androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) { val path = Path().apply { moveTo(0f, 0f); lineTo(size.width, size.height/2); lineTo(0f, size.height); lineTo(size.width*0.3f, size.height/2); close() }; drawPath(path, Color.White) } } }
@Composable fun BtnSendStealth(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(44.dp).background(Brush.radialGradient(listOf(CyberColorsV3.ElectricCyan.copy(alpha=0.2f), Color.Transparent))), contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = CyberColorsV3.ElectricCyan, modifier = Modifier.clickable { onClick() }) } }
@Composable fun BtnSendEmoji(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(44.dp).border(1.dp, CyberColorsV3.TechPurple, RectangleShape).background(CyberColorsV3.RogDarkGray).clickable { onClick() }, contentAlignment = Alignment.Center) { Text("🚀", fontSize = 18.sp) } }

// --- 6. PERFIL ---
@Composable fun BtnProfileRog(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(48.dp).drawBehind { val path = Path().apply { moveTo(size.width/2, 0f); lineTo(size.width, size.height*0.25f); lineTo(size.width, size.height*0.75f); lineTo(size.width/2, size.height); lineTo(0f, size.height*0.75f); lineTo(0f, size.height*0.25f); close() }; drawPath(path, CyberColorsV3.TechPurple, style = Stroke(2.dp.toPx())) }.clickable { onClick() }, contentAlignment = Alignment.Center) { androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) { drawCircle(CyberColorsV3.NeonMagenta, radius = size.width/3, center = Offset(size.width/2, size.height/3)); drawArc(CyberColorsV3.NeonMagenta, 0f, 180f, false, Offset(0f, size.height*0.6f), Size(size.width, size.height), style = Stroke(2.dp.toPx())) } } }
@Composable fun BtnProfileStealth(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.LightGray) } }
@Composable fun BtnProfileEmoji(onClick: () -> Unit = {}) { Box(modifier = Modifier.size(40.dp).background(CyberColorsV3.AbsoluteBlack).border(1.dp, CyberColorsV3.ElectricCyan.copy(alpha=0.3f), CutCornerShape(4.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { Text("👾", fontSize = 20.sp) } }

// ==========================================================================================
// 📋 SECCIÓN 7: OTROS COMPONENTES (DATA ROWS & CHAT)
// ==========================================================================================

@Composable
fun MaverickDataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    isEditMode: Boolean = false,
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = false,
    isGoogleAccount: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    if (!isEditMode && value.isEmpty()) return
    Row(modifier = modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
            if (isGoogleAccount) Icon(painter = painterResource(R.drawable.ic_google_logo), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(22.dp))
            else if (emoji != null) Text(text = emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaverickColors.TextMuted, fontWeight = FontWeight.Bold)
            if (isEditMode && !readOnly) {
                BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), cursorBrush = SolidColor(MaverickColors.GeminiAccent), decorationBox = { innerTextField ->
                    Column { Box { if (value.isEmpty()) Text("Completar...", color = Color.Gray, fontSize = 14.sp); innerTextField() }; Spacer(modifier = Modifier.height(4.dp)); HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.White.copy(alpha = 0.1f)) }
                })
            } else {
                Text(text = value.ifEmpty { "No especificado" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (trailingIcon != null) trailingIcon()
    }
}

@Composable
fun ChatBubbleRogElite(text: String, onCloseClick: () -> Unit = {}) {
    Column(modifier = Modifier.padding(start = 16.dp)) {
        Box(modifier = Modifier.widthIn(max = 280.dp).clip(CutCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)).background(Brush.linearGradient(listOf(CyberColorsV3.TechPurple, CyberColorsV3.NeonMagenta)))) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Text(text, color = Color.White, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(24.dp).clickable { onCloseClick() }) { androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) { drawLine(Color.White, Offset(0f, 0f), Offset(size.width, size.height), 2.dp.toPx()); drawLine(Color.White, Offset(size.width, 0f), Offset(0f, size.height), 2.dp.toPx()) } }
            }
        }
    }
}

// ==========================================================================================
// 🖼️ SECCIÓN 8: PREVIEW COMPLETA (LARGA PARA TODOS LOS BOTONES)
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14, heightDp = 2500)
@Composable
fun PreviewMaverickButtonsFull() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SECCIÓN 1 ---
        Text("🔘 RECTANGULAR PREMIUM", color = MaverickColors.TextMuted, fontWeight = FontWeight.Black)
        BotonesPremium.Aceptar(onClick = {}, modifier = Modifier.fillMaxWidth())
        BotonesPremium.Cancelar(onClick = {}, modifier = Modifier.fillMaxWidth())
        BotonesPremium.Enviar(onClick = {}, modifier = Modifier.fillMaxWidth())
        BotonesPremium.Simple("SIMPLE BUTTON", onClick = {}, modifier = Modifier.fillMaxWidth())

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- SECCIÓN 2 ---
        Text("🔵 ROUND & BENTO ACTIONS", color = MaverickColors.TextMuted, fontWeight = FontWeight.Black)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            IconosAccion.Presupuesto {}
            IconosAccion.Aceptar {}
            IconosAccion.Cancelar {}
            IconosAccion.Editar {}
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            IconosAccion.Eliminar {}
            IconosAccion.Mensaje {}
            IconosAccion.Generico("🎮", "PLAY") {}
            MaverickRoundButtonSimple("🌑", "SIMPLE", {})
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- SECCIÓN 3 ---
        Text("🌪️ TACTICAL & HUD", color = MaverickColors.TextMuted, fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MaverickTacticalButton(onClick = {}) { Icon(Icons.Default.FilterList, "", tint = Color.White, modifier = Modifier.size(18.dp)) }
            MaverickTacticalButton(onClick = {}, accentColor = MaverickColors.NeonCyan) { Icon(Icons.Default.Settings, "", tint = MaverickColors.NeonCyan, modifier = Modifier.size(18.dp)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BeSmallActionButton("SCAN", {}, icon = Icons.Default.QrCodeScanner, isSelected = true)
            BeSmallActionButton("CHAT", {}, emoji = "🤖")
            BeSmallActionButton("MAP", {}, icon = Icons.Default.Map)
        }
        Text("🛠️ COMPACT HUD BUTTONS", color = MaverickColors.TextMuted, fontSize = 10.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MaverickCompactHudButton("SCAN", {}, emoji = "🔍")
            MaverickCompactHudButton("LOCK", {}, icon = Icons.Default.Lock, accentColor = MaverickColors.DeepRed)
            MaverickCompactHudButton("USER", {}, icon = Icons.Default.Person, isSelected = true, accentColor = MaverickColors.GoldPremium)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- SECCIÓN 4 ---
        Text("💎 BENTO SPECIALS", color = MaverickColors.TextMuted, fontWeight = FontWeight.Black)
        BentoActionButton("CONTINUAR PROCESO", emoji = "🔥", onClick = {})
        BentoPillMenu(items = listOf("🏠" to {}, "🔍" to {}, "👤" to {}))

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- SECCIÓN 5 ---
        Text("📱 M3 ADAPTIVE ICONS", color = MaverickColors.TextMuted, fontWeight = FontWeight.Black)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
            MaverickM3AdaptiveIcon(icon = MaverickIcons.Search, containerColor = MaverickColors.DeepSpace)
            MaverickM3AdaptiveIcon(icon = MaverickIcons.Check, containerColor = MaverickColors.AcidGreen.copy(alpha = 0.2f), iconColor = MaverickColors.AcidGreen)
            MaverickM3AdaptiveIcon(icon = MaverickIcons.Delete, containerColor = MaverickColors.DeepRed.copy(alpha = 0.2f), iconColor = MaverickColors.DeepRed)
            MaverickM3AdaptiveIcon(icon = MaverickIcons.Budget, containerColor = MaverickColors.GoldPremium.copy(alpha = 0.2f), iconColor = MaverickColors.GoldPremium)
        }
/**
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- SECCIÓN 6 ---
        Text("🎛️ VECTORIAL ACTIONS (ROG / STEALTH / EMOJI)", color = MaverickColors.TextMuted, fontWeight = FontWeight.Black)
        val rows = listOf(
            "ACEPTAR" to  { Row { BtnAcceptRog(); Spacer(Modifier.width(20.dp)); BtnAcceptStealth(); Spacer(Modifier.width(20.dp)); BtnAcceptEmoji() } },
            "CANCELAR" to { Row { BtnCancelRog(); Spacer(Modifier.width(20.dp)); BtnCancelStealth(); Spacer(Modifier.width(20.dp)); BtnCancelEmoji() } },
            "LIMPIAR" to { Row { BtnClearRog(); Spacer(Modifier.width(20.dp)); BtnClearStealth(); Spacer(Modifier.width(20.dp)); BtnClearEmoji() } },
            "BORRAR" to { Row { BtnDeleteRog(); Spacer(Modifier.width(20.dp)); BtnDeleteStealth(); Spacer(Modifier.width(20.dp)); BtnDeleteEmoji() } },
            "ENVIAR" to { Row { BtnSendRog(); Spacer(Modifier.width(20.dp)); BtnSendStealth(); Spacer(Modifier.width(20.dp)); BtnSendEmoji() } },
            "PERFIL" to { Row { BtnProfileRog(); Spacer(Modifier.width(20.dp)); BtnProfileStealth(); Spacer(Modifier.width(20.dp)); BtnProfileEmoji() } }
        )
        rows.forEach { (label, content) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 10.sp, color = Color.Gray)
                content()
            }
        }
**/
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- SECCIÓN 7 ---
        Text("📋 DATA & CHAT", color = MaverickColors.TextMuted, fontWeight = FontWeight.Black)
        MaverickDataRow("NOMBRE DE USUARIO", "Maverick_User_01", emoji = "👤", isEditMode = false)
        MaverickDataRow("ESTADO DEL SISTEMA", "Operativo", emoji = "⚡", isEditMode = true)
        ChatBubbleRogElite("Sistemas Maverick actualizados. Todos los botones están listos para el despliegue.")

        Spacer(modifier = Modifier.height(50.dp))
    }
}









