package com.example.myapplication.prestador.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

// ── MODELO ───────────────────────────────────────────────────────────────────
data class PrestadorAction(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val tint: Color = Color.White,
    val onClick: () -> Unit = {}
)

// ── BARRA FLOTANTE DERECHA ────────────────────────────────────────────────────
@Composable
fun BeActionsBar(
    visible: Boolean,
    actions: List<PrestadorAction>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.zIndex(10f),
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedVisibility(
            visible = visible && actions.isNotEmpty(),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .height(72.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CutCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                    .clip(CutCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                actions.forEachIndexed { index, action ->
                    if (action.id.startsWith("divider")) {
                        BeVerticalDivider()
                    } else {
                        BeActionButton(action = action)
                        if (index < actions.lastIndex) Spacer(Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

// ── BOTÓN INDIVIDUAL ──────────────────────────────────────────────────────────
@Composable
private fun BeActionButton(action: PrestadorAction) {
    Column(
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight()
            .clickable { action.onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            tint = action.tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = action.label.uppercase(),
            color = action.tint.copy(alpha = 0.9f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── DIVISOR VERTICAL ─────────────────────────────────────────────────────────
@Composable
private fun BeVerticalDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(36.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent)
                )
            )
    )
}
