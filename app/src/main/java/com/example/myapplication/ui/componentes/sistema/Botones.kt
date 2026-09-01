package com.example.myapplication.ui.componentes.sistema
import com.example.myapplication.uishared.estilos.AppIcons
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.estilos.SharedCyberColors
import com.example.myapplication.uishared.estilos.CyberTypography
import com.example.myapplication.viewmodel.home.*
import com.example.myapplication.viewmodel.auth.*
import com.example.myapplication.ui.pantallas.auth.*
import com.example.myapplication.viewmodel.home.*
import com.example.myapplication.ui.pantallas.home.*
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
import androidx.compose.ui.draw.blur
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
import com.example.myapplication.ui.componentes.be.modelos.BeDictionary
import com.example.myapplication.ui.componentes.be.modelos.AppButtonStyle

// ==========================================================================================
// 🎯 SECCIÓN 0: COMPONENTE INTELIGENTE (SEMANTIC DISPATCHER)
// ==========================================================================================

/**
 * Botón "Mágico" que se configura solo desde el diccionario.
 * Busca la acción por key y aplica el molde App correspondiente.
 */
@Composable
fun AppActionButton(
    actionKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    overrideTint: Color? = null
) {
    val action = BeDictionary.Actions[actionKey] ?: return

    when (action.style) {
        AppButtonStyle.ACTION_CIRCLE -> {
            AppActionCircle(
                icon = action.icon,
                emoji = action.emoji,
                onClick = onClick,
                modifier = modifier,
                accentColor = overrideTint ?: action.tint
            )
        }
        AppButtonStyle.RECTANGULAR_PREMIUM -> {
            AppButton(
                text = action.label,
                onClick = onClick,
                modifier = modifier,
                emoji = action.emoji,
                accentColor = overrideTint ?: action.tint
            )
        }
        AppButtonStyle.ROUND_BENTO -> {
            AppRoundButton(
                emoji = action.emoji ?: "❓",
                label = action.label,
                onClick = onClick,
                modifier = modifier,
                accentColor = overrideTint ?: action.tint
            )
        }
        AppButtonStyle.TACTICAL_SQUARE -> {
            AppTacticalButton(
                onClick = onClick,
                modifier = modifier,
                accentColor = overrideTint ?: action.tint
            ) {
                if (action.emoji != null) Text(action.emoji, fontSize = 18.sp)
                else Icon(action.icon, null, tint = overrideTint ?: action.tint, modifier = Modifier.size(18.dp))
            }
        }
        AppButtonStyle.COMPACT_HUD -> {
            AppCompactHudButton(
                label = action.label,
                onClick = onClick,
                modifier = modifier,
                emoji = action.emoji,
                icon = action.icon,
                accentColor = overrideTint ?: action.tint
            )
        }
    }
}

/**
 * Molde: ACTION_CIRCLE (Boton de Cabecera Glass)
 * Conserva la forma y estilo de la Tarjeta de Licitaciones.
 */
@Composable
fun AppActionCircle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = Color.White,
    size: Dp = 28.dp
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        color = Color.White.copy(0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size((size.value * 0.55).dp)
                )
            } else if (emoji != null) {
                Text(text = emoji, fontSize = (size.value * 0.5).sp)
            }
        }
    }
}

// ==========================================================================================
// 🔘 SECCIÓN 1: BOTONES RECTANGULARES (MAVERICK BASE)
// ==========================================================================================

/**
 * Botón base con estilo App: Cristal, bordes brillantes y animación 3D.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    backgroundColor: Color = SharedPalette.BentoDarkGlassBackground,
    accentColor: Color = SharedPalette.NeonCyan,
    textColor: Color = Color.White,
    height: Dp = 56.dp,
    cornerRadius: Dp = 16.dp,
    fontSize: TextUnit = 16.sp,
    paddingHorizontal: Dp = 24.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "ButtonScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 12.dp,
        label = "ButtonElevation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(height)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                )
            )
            .border(
                1.dp, 
                Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.6f), Color.Transparent)
                ), 
                RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
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
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Versión del botón rectangular SIN NEÓN (sin sombra brillante).
 */
@Composable
fun AppButtonSimple(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    backgroundColor: Color = SharedPalette.BentoDarkGlassBackground,
    accentColor: Color = SharedPalette.NeonCyan,
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
            .background(SharedPalette.BentoGlassBrush)
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

// ==========================================================================================
// 🔵 SECCIÓN 2: BOTONES CIRCULARES (ROUND & BENTO ACTIONS)
// ==========================================================================================

/**
 * Botón redondo base con estilo App: Efecto Flotante 3D y etiqueta.
 */
@Composable
fun AppRoundButton(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = SharedPalette.NeonCyan,
    buttonSize: Dp = 64.dp,
    emojiSize: TextUnit = 28.sp,
    labelColor: Color = Color.Black.copy(alpha = 0.6f),
    labelFontSize: TextUnit = 12.sp,
    showLabel: Boolean = true,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "RoundButtonScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 16.dp,
        label = "RoundButtonElevation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // --- CAPA 0: SOMBRA MANUAL (CÍRCULO NEGRO DESFASADO) ---
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(buttonSize)
                    .offset(y = 3.dp, x = 3.dp)
                    .background(Color.Black.copy(alpha = 0.9f), CircleShape)
                    //.background(Brush.verticalGradient(listOf(Color.Black, Color.Transparent))))
            )


            // --- CAPA 1: CUERPO DEL BOTÓN ---
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(SharedPalette.AsPanelBorder)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (content != null) {
                    content()
                } else {
                    Text(text = emoji, fontSize = emojiSize)
                }
            }
        }

        if (showLabel && label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = labelColor,
                fontSize = labelFontSize,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ==========================================================================================
// 🌪️ SECCIÓN 3: BOTONES TÁCTICOS Y HUD (ASISTENTE BE)
// ==========================================================================================

@Composable
fun AppTacticalButton(
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
                color = if (active) SharedPalette.NeonCyan else Color.Gray.copy(alpha = 0.6f),
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
    AppTacticalButton(
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
    AppTacticalButton(
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
    val accentColor = if (isSelected) SharedPalette.NeonCyan else tint

    // ==========================================================================================
    // 🛡️ SECCIÓN: BOTÓN SIN CONTENEDOR (SOLO EMOJI + TEXTO)
    // ==========================================================================================
    // Se elimina AppRoundButtonSimple para dejar solo el icono y el texto solicitado.
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
fun AppCompactHudButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    accentColor: Color = SharedPalette.NeonCyan,
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
            .background(SharedPalette.ROG_Dark_Bg)
            .background(SharedPalette.BentoGlassBrush)
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
                .background(SharedPalette.ROG_Dark_Bg) // Fondo sólido para ocultar el icono detrás
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
fun PreviewAppCompactHudButton() {
    Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCompactHudButton("SCAN", {}, emoji = "🔍")
        AppCompactHudButton("LOCK", {}, icon = Icons.Default.Lock, accentColor = SharedPalette.DeepRed)
        AppCompactHudButton("USER", {}, icon = Icons.Default.Person, isSelected = true, accentColor = SharedPalette.GoldPremium)
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
    containerColor: Color = SharedPalette.ROG_Dark_Bg,
    contentColor: Color = Color.White,
    accentColor: Color = SharedPalette.NeonCyan
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
    color: Color = SharedPalette.GeminiAccent,
    fontSize: TextUnit = 16.sp,
    emojiSize: TextUnit = 20.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(SharedPalette.BentoDarkGlassBackground)
            .background(SharedPalette.BentoGlassBrush)
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
            .shadow(elevation = 35.dp, shape = RoundedCornerShape(50), ambientColor = Color.Black)
            .clip(RoundedCornerShape(50))
            .background(SharedPalette.BentoDarkGlassBackground)
            .background(SharedPalette.BentoGlassBrush)
            .border(1.5.dp, SharedPalette.BentoBorderBrush, RoundedCornerShape(50))
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
fun AppM3AdaptiveIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = SharedPalette.GlassWhite,
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
// 📋 SECCIÓN 7: OTROS COMPONENTES (DATA ROWS & CHAT)
// ==========================================================================================

@Composable
fun AppDataRow(
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
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SharedPalette.TextMuted, fontWeight = FontWeight.Bold)
            if (isEditMode && !readOnly) {
                BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), cursorBrush = SolidColor(SharedPalette.GeminiAccent), decorationBox = { innerTextField ->
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
        Box(modifier = Modifier.widthIn(max = 280.dp).clip(CutCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)).background(Brush.linearGradient(listOf(SharedCyberColors.TechPurple, SharedCyberColors.NeonMagenta)))) {
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, heightDp = 1500)
@Composable
fun PreviewAppButtonsFull() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ==========================================================================================
        // 📚 SECCIÓN: CATÁLOGO DEL DICCIONARIO (ÚNICA FUENTE DE VERDAD)
        // ==========================================================================================
        Text("📖 BE_DICTIONARY CATALOG (SMART DISPATCHER)", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BeDictionary.Actions.keys.chunked(3).forEach { keys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    keys.forEach { key ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppActionButton(actionKey = key, onClick = {})
                            Spacer(Modifier.height(4.dp))
                            Text(key.uppercase(), fontSize = 8.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- SECCIÓN: COMPONENTES ESPECIALES (AÚN NO TOKENIZADOS) ---
        Text("💎 BENTO SPECIALS", color = SharedPalette.TextMuted, fontWeight = FontWeight.Black)
        BentoActionButton("CONTINUAR PROCESO", emoji = "🔥", onClick = {})
        BentoPillMenu(items = listOf("🏠" to {}, "🔍" to {}, "👤" to {}))

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        Text("📱 M3 ADAPTIVE ICONS", color = SharedPalette.TextMuted, fontWeight = FontWeight.Black)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
            AppM3AdaptiveIcon(icon = AppIcons.Search, containerColor = SharedPalette.DeepSpace)
            AppM3AdaptiveIcon(icon = AppIcons.Check, containerColor = SharedPalette.AcidGreen.copy(alpha = 0.2f), iconColor = SharedPalette.AcidGreen)
            AppM3AdaptiveIcon(icon = AppIcons.Delete, containerColor = SharedPalette.DeepRed.copy(alpha = 0.2f), iconColor = SharedPalette.DeepRed)
            AppM3AdaptiveIcon(icon = AppIcons.Budget, containerColor = SharedPalette.GoldPremium.copy(alpha = 0.2f), iconColor = SharedPalette.GoldPremium)
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        Text("📋 DATA & CHAT", color = SharedPalette.TextMuted, fontWeight = FontWeight.Black)
        AppDataRow("NOMBRE DE USUARIO", "App_User_01", emoji = "👤", isEditMode = false)
        ChatBubbleRogElite("Sistemas App actualizados. Los botones redundantes han sido eliminados.")

        Spacer(modifier = Modifier.height(50.dp))
    }
}
