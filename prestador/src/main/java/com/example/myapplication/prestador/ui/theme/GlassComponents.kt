package com.example.myapplication.prestador.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 🔮 COMPONENTES CYBERPUNK
 * Elementos con bordes neón, esquinas cortadas y efectos de brillo
 */

/**
 * ⚡ CYBERPUNK CARD - Tarjeta con bordes neón brillantes
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = null,
    elevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getOrganicColors()
    
    // Animación de pulso para el borde
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val cardModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }
    
    Surface(
        modifier = cardModifier,
        shape = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
        color = colors.glassBackground,
        border = BorderStroke(2.dp, colors.glassBorder.copy(alpha = glowAlpha)),
        shadowElevation = elevation
    ) {
        Column(content = content)
    }
}

/**
 * GLASS STAT CARD
 * Card pequeña con icono, valor y label
 */
@Composable
fun GlassStat(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false
) {
    val colors = getOrganicColors()
    
    // Animación de pulso opcional
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    GlassCard(
        modifier = modifier.scale(if (isPulsing) scale else 1f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono con fondo de color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        accentColor.copy(alpha = if (colors.isDark) 0.2f else 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Valor grande
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Label pequeño
            Text(
                text = label,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}

/**
 * ⚡ GRADIENT BUTTON - Botón neón con borde brillante
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = getOrganicColors()
    
    // Animación de pulso
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .background(
                color = if (enabled) colors.surface else Color.DarkGray,
                shape = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
            )
            .border(
                width = 2.dp,
                color = if (enabled) colors.cyan.copy(alpha = glowAlpha) else Color.Gray,
                shape = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
            )
            .clickable(enabled = enabled) { 
                isPressed = true
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = colors.cyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = colors.cyan,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

/**
 * SECTION TITLE
 * Título de sección con estilo consistente
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val colors = getOrganicColors()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        
        action?.invoke()
    }
}

/**
 * ONLINE BADGE
 * Badge que muestra estado "ONLINE"
 */
@Composable
fun OnlineBadge(
    modifier: Modifier = Modifier
) {
    val colors = getOrganicColors()
    
    Surface(
        modifier = modifier,
        color = if (colors.isDark) colors.green.copy(alpha = 0.1f) else Color(0xFFDCFCE7),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            1.dp,
            if (colors.isDark) colors.green.copy(alpha = 0.2f) else Color(0xFFBBF7D0)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.green, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "ONLINE",
                color = if (colors.isDark) colors.green else Color(0xFF15803D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * BUBBLE ACTION
 * Botón circular con icono para acciones rápidas
 */
@Composable
fun BubbleAction(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit = {}
) {
    val colors = getOrganicColors()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    accentColor.copy(alpha = if (colors.isDark) 0.2f else 0.15f),
                    CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.textSecondary
        )
    }
}
