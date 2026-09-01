package com.example.myapplication.prestador.ui.pantallas.components

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

// Modelo
data class PrestadorAction(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val tint: Color = Color.White,
    val onClick: () -> Unit = {}
)

// Barra flotante — botones circulares con morphing desde la derecha
@Composable
fun BeActionsBar(
    visible: Boolean,
    actions: List<PrestadorAction>,
    modifier: Modifier = Modifier
) {
    val realActions = actions.filter { !it.id.startsWith("divider") }
    val actionsKey = realActions.joinToString { it.id }

    Box(
        modifier = modifier.zIndex(10f),
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedContent(
            targetState = actionsKey,
            transitionSpec = {
                (scaleIn(
                    initialScale = 0.75f,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(350))) togetherWith
                (scaleOut(
                    targetScale = 0.75f,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(250)))
            },
            label = "actionsBarTransition"
        ) { _ ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                realActions.forEach { action ->
                    BeActionButton(action = action)
                }
            }
        }
    }
}

// Botón circular con solo ícono y efecto press
@Composable
private fun BeActionButton(
    action: PrestadorAction,
    modifier: Modifier = Modifier
) {
    val currentOnClick by rememberUpdatedState(action.onClick)
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.80f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .size(46.dp)
                .shadow(
                    elevation = if (pressed) 2.dp else 12.dp,
                    shape = CircleShape,
                    spotColor = action.tint.copy(alpha = 0.6f)
                )
                .clip(CircleShape)
                .background(action.tint)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        },
                        onTap = { currentOnClick() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        
        // 🔥 [ELITE v2026] Etiqueta de texto para mayor claridad UX
        androidx.compose.material3.Text(
            text = action.label.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = action.tint,
            letterSpacing = 0.5.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.width(60.dp)
        )
    }
}













































