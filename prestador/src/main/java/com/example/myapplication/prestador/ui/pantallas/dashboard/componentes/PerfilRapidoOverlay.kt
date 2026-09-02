package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel

/**
 * [ELITE]: tarjeta rápida de perfil — solo lectura, se abre al tocar el avatar de la
 * cabecera de Inicio (antes abría el mismo drawer que el ícono de menú). Muestra un
 * resumen de la identidad real (matrícula, contacto, rubros) con un botón directo a
 * "Editar perfil" para quien quiera cambiar algo.
 */
@Composable
fun PerfilRapidoOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEditarPerfil: () -> Unit,
    viewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val identidad = state.ecosistema?.aModelosUi()?.firstOrNull()
    val categoriasPorId = remember(state.todasLasCategorias) { state.todasLasCategorias.associateBy { it.id } }

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        // [ELITE]: entra con un resorte (escala + fade) y sale con un tween corto —
        // como es un "popup" centrado (no un panel lateral) el gesto natural es
        // aparecer/achicarse en el lugar, no deslizar.
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = visible,
            enter = scaleIn(spring(dampingRatio = 0.72f, stiffness = 420f), initialScale = 0.82f) + fadeIn(tween(200)),
            exit = scaleOut(tween(160), targetScale = 0.9f) + fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .widthIn(max = 340.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { }
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFF4511E))))
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(26.dp)
                                .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp).padding(bottom = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-40).dp)
                                .size(84.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF334155))))
                                .border(3.dp, Color(0xFF0F172A), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val foto = identidad?.urlMiniatura ?: identidad?.urlFoto
                            if (foto != null) {
                                AsyncImage(
                                    model = foto,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.offset(y = (-28).dp)) {
                            Text(
                                identidad?.titulo?.ifBlank { null } ?: "Sin nombre",
                                fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF8FAFC)
                            )
                            if (identidad?.estaVerificado == true) {
                                Icon(Icons.Default.Verified, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.offset(y = (-22).dp)) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                            Text("%.2f".format(identidad?.reputacion ?: 0f), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text("· Especialista App", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.offset(y = (-14).dp))

                        FilaDato(Icons.Default.Badge, Color(0xFF388E3C), "Matrícula profesional", identidad?.matricula?.ifBlank { null } ?: "No especificada")
                        FilaDato(Icons.Default.Email, Color(0xFF0288D1), "Correo de contacto", identidad?.correo?.ifBlank { null } ?: "No especificado")
                        FilaDato(Icons.Default.Phone, Color(0xFF7B1FA2), "Teléfono", identidad?.numeroTelefono?.ifBlank { null } ?: "No especificado")

                        val nombresCategorias = identidad?.idCategorias?.mapNotNull { categoriasPorId[it] } ?: emptyList()
                        if (nombresCategorias.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(
                                    "RUBROS Y ESPECIALIDADES",
                                    fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B),
                                    letterSpacing = 0.4.sp,
                                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                                )
                                FlowRowChips(nombresCategorias.map { "${it.icono} ${it.nombre}" })
                            }
                        }

                        Button(
                            onClick = { onDismiss(); onEditarPerfil() },
                            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 18.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("EDITAR PERFIL", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDato(icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(9.dp))
                .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.3.sp)
            Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF1F5F9))
        }
    }
}

@Composable
private fun FlowRowChips(items: List<String>) {
    // [ELITE]: sin dependencia de FlowRow — envuelve en filas simples de a 2, alcanza para pocos rubros.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { texto ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF94A3B8).copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFF94A3B8).copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                    }
                }
            }
        }
    }
}
