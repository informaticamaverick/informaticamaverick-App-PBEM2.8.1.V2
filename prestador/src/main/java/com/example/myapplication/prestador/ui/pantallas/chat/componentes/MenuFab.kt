package com.example.myapplication.prestador.ui.pantallas.chat.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- ACCIONES DEL MENÚ FAB (v2026.ELITE) ---
 */
enum class AccionFab(val titulo: String, val icono: ImageVector) {
    NUEVO_PRESUPUESTO("Nuevo Presupuesto", Icons.Default.Description),
    VISITA_TECNICA("Nueva Visita Técnica", Icons.Default.HomeWork),
    NUEVO_TURNO("Nuevo Turno", Icons.Default.Event),
    ENVIAR_PRODUCTO("Enviar Producto", Icons.Default.Storefront),
    FINALIZAR_TRABAJO("Finalizar Trabajo", Icons.Default.TaskAlt)
}

/**
 * --- MENÚ FAB EXPANDIBLE PRESTADOR (v2026.ELITE) ---
 */
@Composable
fun MenuFab(
    alHacerClickAccion: (AccionFab) -> Unit,
    colorAcento: Color = SharedPalette.BlueEnd,
    modifier: Modifier = Modifier,
    inicialmenteExpandido: Boolean = false
) {
    var expandido by remember { mutableStateOf(inicialmenteExpandido) }
    val transicion = updateTransition(targetState = expandido, label = "expansionFab")

    val rotacionFab by transicion.animateFloat(label = "rotacion") { 
        if (it) 45f else 0f 
    }

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AccionFab.entries.reversed().forEach { accion ->
            ItemMenuFab(
                accion = accion,
                visible = expandido,
                onClick = {
                    expandido = false
                    alHacerClickAccion(accion)
                },
                colorAcento = colorAcento
            )
        }

        FloatingActionButton(
            onClick = { expandido = !expandido },
            containerColor = if (expandido) SharedPalette.Slate800 else colorAcento,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp) 
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Menu de acciones",
                modifier = Modifier.size(24.dp).rotate(rotacionFab)
            )
        }
    }
}

@Composable
private fun ItemMenuFab(
    accion: AccionFab,
    visible: Boolean,
    onClick: () -> Unit,
    colorAcento: Color
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(initialScale = 0.5f),
        exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(targetScale = 0.5f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                color = SharedPalette.Slate800.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = accion.titulo,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                SmallFloatingActionButton(
                    onClick = onClick,
                    containerColor = SharedPalette.Slate800,
                    contentColor = colorAcento,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(accion.icono, null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
