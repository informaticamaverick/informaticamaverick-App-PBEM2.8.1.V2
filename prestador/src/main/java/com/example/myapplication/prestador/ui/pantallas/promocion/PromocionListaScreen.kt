package com.example.myapplication.prestador.ui.pantallas.promocion

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.EstadoPromocion
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.core.dominio.modelos.TipoCategoriaPromo
import com.example.myapplication.core.dominio.modelos.TipoPromocion
import com.example.myapplication.prestador.viewmodel.promocion.PrePromocionViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- PALETA OSCURA (misma que Inicio/Mensajes/Concursos) ---
private object ThemeColors {
    val DarkBg = Color(0xFF030712)
    val CardBg = Color(0xFF0F172A)
    val CardBorder = Color(0xFF334155).copy(alpha = 0.7f)
    val HeaderBg = Color(0xFF020617).copy(alpha = 0.95f)
    val Divider = Color(0xFF1E293B)
    val BrandOrange = Color(0xFFFF5722)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}

/**
 * --- PANTALLA DE ADMINISTRACIÓN DE PROMOCIONES (ELITE v2026.FINAL) ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocionListaScreen(
    onBack: () -> Unit,
    onNavigateToCreatePromotion: () -> Unit = {},
    viewModel: PrePromocionViewModel = hiltViewModel()
) {
    val promociones by viewModel.misPublicaciones.collectAsStateWithLifecycle()
    val estaCargando by viewModel.estaCargando.collectAsStateWithLifecycle()
    
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    val titulosPestanas = listOf("Activas", "Inactivas")

    var promocionParaEliminar by remember { mutableStateOf<Promocion?>(null) }
    var promocionSeleccionadaDetalle by remember { mutableStateOf<Promocion?>(null) }

    val ahora = System.currentTimeMillis()
    val activas = promociones.filter { it.fechaExpiracion > ahora && it.estado == EstadoPromocion.ACTIVA }
    val inactivas = promociones.filter { it.fechaExpiracion <= ahora || it.estado != EstadoPromocion.ACTIVA }

    if (promocionParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { promocionParaEliminar = null },
            title = { Text("¿Eliminar publicación?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es permanente. ¿Continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePromotion(promocionParaEliminar!!.id)
                        promocionParaEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("ELIMINAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { promocionParaEliminar = null }) { Text("CANCELAR") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ThemeColors.HeaderBg,
                border = BorderStroke(1.dp, ThemeColors.Divider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .background(ThemeColors.CardBg, RoundedCornerShape(8.dp))
                                .border(1.dp, ThemeColors.CardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = ThemeColors.TextPrimary, modifier = Modifier.size(16.dp))
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            Text("MIS PUBLICACIONES", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ThemeColors.TextPrimary, letterSpacing = 0.5.sp)
                            Text("Gestiona tus ofertas", fontSize = 11.sp, color = ThemeColors.TextSecondary)
                        }
                        Surface(
                            color = ThemeColors.BrandOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, ThemeColors.BrandOrange.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${activas.size} ACTIVAS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = ThemeColors.BrandOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    TabRow(
                        selectedTabIndex = pestanaSeleccionada,
                        containerColor = Color.Transparent,
                        contentColor = ThemeColors.TextPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pestanaSeleccionada]),
                                color = ThemeColors.BrandOrange
                            )
                        },
                        divider = {}
                    ) {
                        titulosPestanas.forEachIndexed { indice, titulo ->
                            Tab(
                                selected = pestanaSeleccionada == indice,
                                onClick = { pestanaSeleccionada = indice },
                                text = {
                                    Text(
                                        text = "$titulo (${if (indice == 0) activas.size else inactivas.size})",
                                        fontWeight = if (pestanaSeleccionada == indice) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (pestanaSeleccionada == indice) ThemeColors.TextPrimary else ThemeColors.TextMuted
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePromotion,
                containerColor = ThemeColors.BrandOrange,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, "Crear Publicación")
            }
        },
        containerColor = ThemeColors.DarkBg
    ) { paddingValues ->
        val estadoPull = rememberPullToRefreshState()

        PullToRefreshBox(
            state = estadoPull,
            isRefreshing = estaCargando,
            onRefresh = { viewModel.refreshMyPromotions() },
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (promociones.isEmpty()) {
                    EstadoVacioPromocionesMav()
                } else {
                    val listaActual = if (pestanaSeleccionada == 0) activas else inactivas
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(listaActual, key = { it.id }) { promo ->
                            TarjetaGestionPromoMav(
                                promo = promo,
                                ahora = ahora,
                                alEliminar = { promocionParaEliminar = promo },
                                alRepublicar = { viewModel.republishPromotion(promo) },
                                alHacerClick = { promocionSeleccionadaDetalle = promo }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaGestionPromoMav(
    promo: Promocion,
    ahora: Long,
    alEliminar: () -> Unit,
    alRepublicar: () -> Unit,
    alHacerClick: () -> Unit
) {
    val expirada = promo.fechaExpiracion <= ahora
    val colorEstado = when {
        expirada -> ThemeColors.TextMuted
        promo.estado == EstadoPromocion.ACTIVA -> Color(0xFF10B981)
        else -> Color(0xFFF59E0B)
    }

    val tiempoRestante = if (expirada) "Finalizada" else {
        val diff = promo.fechaExpiracion - ahora
        val horas = diff / (1000 * 60 * 60)
        if (horas > 24) "Faltan ${horas / 24}d" else "Faltan ${horas}h"
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { alHacerClick() },
        shape = RoundedCornerShape(16.dp),
        color = ThemeColors.CardBg,
        border = BorderStroke(1.dp, ThemeColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(84.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.2f))) {
                    if (promo.urlImagenes.isNotEmpty()) {
                        AsyncImage(model = promo.urlImagenes.first(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Surface(
                        color = if (promo.tipo == TipoPromocion.HISTORIA) Color(0xFFF472B6) else Color(0xFF22D3EE),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(text = if (promo.tipo == TipoPromocion.HISTORIA) "STORY" else "OFERTA", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ThemeColors.CardBg, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colorEstado.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, colorEstado.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(colorEstado))
                            Spacer(Modifier.width(5.dp))
                            Text(text = if (expirada) "EXPIRADA" else promo.estado.name, fontSize = 9.sp, fontWeight = FontWeight.Black, color = colorEstado, letterSpacing = 0.5.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(text = promo.titulo, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ThemeColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = promo.descripcion, fontSize = 12.sp, color = ThemeColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = ThemeColors.Divider)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    MetricaAdminItem(label = "Vistas", valor = promo.conteoVistas.toString(), icono = Icons.Default.Visibility, color = ThemeColors.TextMuted)
                    MetricaAdminItem(label = "Likes", valor = promo.conteoLikes.toString(), icono = Icons.Default.Favorite, color = Color(0xFFF43F5E))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (expirada) {
                        IconButton(
                            onClick = alRepublicar,
                            modifier = Modifier
                                .size(30.dp)
                                .background(ThemeColors.CardBg, RoundedCornerShape(8.dp))
                                .border(1.dp, ThemeColors.BrandOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        ) { Icon(Icons.Default.Refresh, null, tint = ThemeColors.BrandOrange, modifier = Modifier.size(14.dp)) }
                    }
                    IconButton(
                        onClick = alEliminar,
                        modifier = Modifier
                            .size(30.dp)
                            .background(ThemeColors.CardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ThemeColors.CardBorder, RoundedCornerShape(8.dp))
                    ) { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFF43F5E), modifier = Modifier.size(14.dp)) }
                }
            }
        }
    }
}

@Composable
fun MetricaAdminItem(label: String, valor: String, icono: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(7.dp))
        Column {
            Text(text = valor, fontSize = 13.sp, fontWeight = FontWeight.Black, color = ThemeColors.TextPrimary)
            Text(text = label.uppercase(), fontSize = 8.sp, color = ThemeColors.TextMuted, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun EstadoVacioPromocionesMav() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Campaign, null, tint = ThemeColors.BrandOrange, modifier = Modifier.size(60.dp))
        Spacer(Modifier.height(24.dp))
        Text("No tienes publicaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ThemeColors.TextPrimary)
    }
}
