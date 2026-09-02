package com.example.myapplication.prestador.ui.pantallas.market

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.prestador.ui.theme.PrestadorTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.compose.LazyPagingItems
import androidx.paging.LoadState
import com.example.myapplication.prestador.datos.local.entidades.PresupuestoEntity
import com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.pantallas.market.ConcursoDetalleSheet
import com.example.myapplication.prestador.viewmodel.dashboard.NotificacionesViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.ConcursoDominio

// --- PALETA OSCURA (misma que Inicio/Mensajes, ver InicioComponents.kt/ChatListScreen.kt) ---
private object ThemeColors {
    val DarkBg = Color(0xFF030712)
    val CardBg = Color(0xFF0F172A)
    val CardBorder = Color(0xFF334155).copy(alpha = 0.7f)
    val HeaderBg = Color(0xFF020617).copy(alpha = 0.95f)
    val Divider = Color(0xFF1E293B)
    val BrandOrange = Color(0xFFFF5722)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
}

/**
 * --- MERCADO DE CONCURSOS (V2026.FINAL) ---
 */
@Composable
fun MercadoConcursosScreen(
    onBack: () -> Unit = {},
    onNavigateToPresupuesto: (String) -> Unit,
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToClientePerfil: (String) -> Unit = {},
    viewModel: NotificacionesViewModel = hiltViewModel()
) {
    val concursoSeleccionado by viewModel.concursoSeleccionado.collectAsStateWithLifecycle()
    val presupuestoEnVista by viewModel.presupuestoEnVista.collectAsStateWithLifecycle()
    val concursosPaginados = viewModel.mercadoConcursos.collectAsLazyPagingItems()
    val estaCargandoConcurso by viewModel.estaCargandoConcurso.collectAsStateWithLifecycle()
    
    val identidadViewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
    val stateDeep by identidadViewModel.state.collectAsStateWithLifecycle()
    val ecosistemaMaestro = stateDeep.ecosistema
    val estaSuscrito = ecosistemaMaestro?.cuenta?.estaSuscrito ?: false

    val identidadUi = remember(ecosistemaMaestro) {
        ecosistemaMaestro?.prestador?.perfil
    }

    LaunchedEffect(Unit) { viewModel.refrescarMercado() }

    MercadoConcursosContent(
        onBack = onBack,
        concursos = concursosPaginados,
        estaCargando = estaCargandoConcurso,
        concursoSeleccionado = concursoSeleccionado,
        presupuestoEnVista = presupuestoEnVista,
        identidadPrestador = identidadUi,
        estaSuscrito = estaSuscrito,
        alRefrescar = { viewModel.refrescarMercado() },
        alHacerClickConcurso = { viewModel.alHacerClickConcurso(it) },
        alVerPresupuestoEnviado = { viewModel.alVerPresupuestoEnviado(it) },
        alCerrarHojaConcurso = { viewModel.cerrarHojaConcurso() },
        alCerrarVistaPreviaPresupuesto = { viewModel.cerrarVistaPreviaPresupuesto() },
        alPostularse = onNavigateToPresupuesto,
        onNavigateToPaywall = onNavigateToPaywall,
        alNavegarAPerfilCliente = onNavigateToClientePerfil
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MercadoConcursosContent(
    onBack: () -> Unit = {},
    concursos: LazyPagingItems<ConcursoDominio>,
    estaCargando: Boolean,
    concursoSeleccionado: ConcursoDominio?,
    presupuestoEnVista: com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems?,
    identidadPrestador: PrestadorDominio?,
    estaSuscrito: Boolean,
    alRefrescar: () -> Unit,
    alHacerClickConcurso: (String) -> Unit,
    alVerPresupuestoEnviado: (String) -> Unit,
    alCerrarHojaConcurso: () -> Unit,
    alCerrarVistaPreviaPresupuesto: () -> Unit,
    alPostularse: (String) -> Unit,
    onNavigateToPaywall: () -> Unit = {},
    alNavegarAPerfilCliente: (String) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ThemeColors.DarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ThemeColors.HeaderBg,
                border = BorderStroke(1.dp, ThemeColors.Divider)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ThemeColors.TextPrimary)
                    }

                    Spacer(Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("COTIZACIONES", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ThemeColors.TextPrimary, letterSpacing = 0.5.sp)
                        Text("Concursos Públicos en tu zona", fontSize = 11.sp, color = ThemeColors.TextSecondary)
                    }

                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row (
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                            Spacer(Modifier.width(5.dp))
                            Text("${concursos.itemCount} ACTIVOS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                    }

                    IconButton(
                        onClick = alRefrescar,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ThemeColors.CardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ThemeColors.CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        if (estaCargando) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ThemeColors.BrandOrange, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, null, tint = ThemeColors.TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { relleno ->
        val cargandoRefresh = concursos.loadState.refresh is LoadState.Loading || estaCargando

        Column(modifier = Modifier.fillMaxSize().padding(relleno).background(ThemeColors.DarkBg)) {
            if (concursos.itemCount == 0 && !cargandoRefresh) {
                EstadoVacioMercado(onActualizar = alRefrescar)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(
                        count = concursos.itemCount,
                        key = concursos.itemKey { it.idConcurso }
                    ) { indice ->
                        concursos[indice]?.let { concurso ->
                            TarjetaConcursoPublico(
                                concurso = concurso, 
                                alHacerClick = { alHacerClickConcurso(concurso.idConcurso) },
                                alResponder = { alPostularse(concurso.idConcurso) },
                                alVerDetalles = { alHacerClickConcurso(concurso.idConcurso) }
                            )
                        }
                    }
                }
            }
        }

        concursoSeleccionado?.let { concurso ->
            ConcursoDetalleSheet(
                concurso = com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity(
                    idConcurso = concurso.idConcurso,
                    idCliente = concurso.idCliente,
                    titulo = concurso.titulo,
                    descripcion = concurso.descripcion,
                    idCategoria = concurso.idCategoria,
                    nombreCliente = concurso.nombreCliente,
                    miniaturaCliente = concurso.urlMiniaturaCliente,
                    estado = concurso.estado,
                    exigeVisita = concurso.exigeVisita,
                    exigeGarantia = concurso.exigeGarantia,
                    exigeMetodoPago = concurso.exigePago,
                    exigeDocPrestador = concurso.exigeDocumentacion,
                    urlImagenes = concurso.urlImagenes,
                    marcaTiempo = concurso.marcaTiempo,
                    nombreEmpresa = concurso.nombreEmpresa,
                    nombreSucursal = concurso.nombreSucursal,
                    direccionCalle = concurso.direccionCalle,
                    direccionNumero = concurso.direccionNumero,
                    direccionLocalidad = concurso.direccionLocalidad,
                    direccionCodigoPostal = concurso.direccionCodigoPostal,
                    fechaInicio = concurso.fechaInicio,
                    fechaFin = concurso.fechaFin,
                    tieneMiPresupuesto = concurso.tieneMiPresupuesto
                ), 
                estaSuscrito = estaSuscrito,
                onDismiss = alCerrarHojaConcurso, 
                onPostularse = { id -> alCerrarHojaConcurso(); alPostularse(id) }, 
                onViewBudget = { alVerPresupuestoEnviado(concurso.idConcurso) }, 
                onNavigateToUserProfile = alNavegarAPerfilCliente,
                onNavigateToPaywall = onNavigateToPaywall
            )
        }

        if (presupuestoEnVista != null && identidadPrestador != null) {
            com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog(
                prestador = identidadPrestador, 
                relacion = presupuestoEnVista, 
                onDismiss = alCerrarVistaPreviaPresupuesto, 
                showSendButton = false
            )
        }
    }
}


@Composable
private fun EstadoVacioMercado(onActualizar: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(ThemeColors.BrandOrange.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .border(1.dp, ThemeColors.BrandOrange.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(40.dp), tint = ThemeColors.BrandOrange)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Todavía no hay concursos en tu zona",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ThemeColors.TextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Los clientes publican pedidos de presupuesto público acá. Te avisamos apenas aparezca uno que coincida con tus rubros.",
                    fontSize = 13.sp,
                    color = ThemeColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 19.sp
                )
            }

            Surface(
                onClick = onActualizar,
                color = ThemeColors.CardBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ThemeColors.CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Refresh, null, tint = ThemeColors.BrandOrange, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ACTUALIZAR AHORA", fontSize = 12.sp, fontWeight = FontWeight.Black, color = ThemeColors.TextPrimary)
                }
            }
        }
    }
}

















































