package com.example.myapplication.prestador.ui.pantallas.promocion

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.promocion.PrePromocionViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val colores = getPrestadorColors()
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
                color = colores.primaryOrange,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colores.primaryOrange, Color(0xFFEA580C))
                            )
                        )
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text("MIS PUBLICACIONES", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White, letterSpacing = 1.sp)
                            Text("Gestiona tus ofertas", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                    TabRow(
                        selectedTabIndex = pestanaSeleccionada,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pestanaSeleccionada]),
                                color = Color.White
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
                                        color = if (pestanaSeleccionada == indice) Color.White else Color.White.copy(alpha = 0.7f)
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
                containerColor = colores.primaryOrange,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, "Crear Publicación")
            }
        },
        containerColor = colores.backgroundColor
    ) { paddingValues ->
        val estadoPull = rememberPullToRefreshState()

        PullToRefreshBox(
            state = estadoPull,
            isRefreshing = estaCargando,
            onRefresh = { viewModel.refreshMyPromotions() },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (promociones.isEmpty()) {
                    EstadoVacioPromocionesMav(colores)
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
                                colores = colores,
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
    colores: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    alEliminar: () -> Unit,
    alRepublicar: () -> Unit,
    alHacerClick: () -> Unit
) {
    val expirada = promo.fechaExpiracion <= ahora
    val colorEstado = when {
        expirada -> Color.Gray
        promo.estado == EstadoPromocion.ACTIVA -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }

    val tiempoRestante = if (expirada) "Finalizada" else {
        val diff = promo.fechaExpiracion - ahora
        val horas = diff / (1000 * 60 * 60)
        if (horas > 24) "Faltan ${horas / 24}d" else "Faltan ${horas}h"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { alHacerClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colores.surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.2f))) {
                    if (promo.urlImagenes.isNotEmpty()) {
                        AsyncImage(model = promo.urlImagenes.first(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Surface(
                        color = if (promo.tipo == TipoPromocion.HISTORIA) Color(0xFFE91E63) else Color(0xFF00E5FF),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(text = if (promo.tipo == TipoPromocion.HISTORIA) "STORY" else "OFERTA", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Surface(shape = RoundedCornerShape(50), color = colorEstado.copy(alpha = 0.1f), border = BorderStroke(0.5.dp, colorEstado.copy(alpha = 0.3f))) {
                        Text(text = if (expirada) "EXPIRADA" else promo.estado.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colorEstado, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(text = promo.titulo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = promo.descripcion, fontSize = 13.sp, color = colores.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    MetricaAdminItem(label = "Vistas", valor = promo.conteoVistas.toString(), icono = Icons.Default.Visibility, color = Color.Gray)
                    MetricaAdminItem(label = "Likes", valor = promo.conteoLikes.toString(), icono = Icons.Default.Favorite, color = Color(0xFFE91E63))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (expirada) {
                        IconButton(onClick = alRepublicar) { Icon(Icons.Default.Refresh, null, tint = colores.primaryOrange) }
                    }
                    IconButton(onClick = alEliminar) { Icon(Icons.Default.DeleteOutline, null, tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun MetricaAdminItem(label: String, valor: String, icono: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = valor, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = label.uppercase(), fontSize = 8.sp, color = Color.Gray)
        }
    }
}

@Composable
fun EstadoVacioPromocionesMav(colores: com.example.myapplication.prestador.ui.theme.PrestadorColors) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Campaign, null, tint = colores.primaryOrange, modifier = Modifier.size(60.dp))
        Spacer(Modifier.height(24.dp))
        Text("No tienes publicaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colores.textPrimary)
    }
}
