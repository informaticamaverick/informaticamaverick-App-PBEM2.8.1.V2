package com.example.myapplication.prestador.ui.pantallas.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.dashboard.NotificacionesViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.ConcursoDominio

/**
 * --- MERCADO DE CONCURSOS (V2026.FINAL) ---
 */
@Composable
fun MercadoConcursosScreen(
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
    val colores = getPrestadorColors()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colores.backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colores.primaryOrange,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colores.primaryOrange, Color(0xFFEA580C))
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("COTIZACIONES", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White, letterSpacing = 1.sp)
                        Text("Concursos Públicos en tu zona", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    
                    IconButton(onClick = alRefrescar) {
                        if (estaCargando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { relleno ->
        val cargandoRefresh = concursos.loadState.refresh is LoadState.Loading || estaCargando
        
        Column(modifier = Modifier.fillMaxSize().padding(relleno).background(colores.backgroundColor)) {
            if (concursos.itemCount == 0 && !cargandoRefresh) {
                EstadoVacioMercado()
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
private fun EstadoVacioMercado() {
    val colores = getPrestadorColors()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = colores.divider)
            Text("Explorando el Mercado...", fontWeight = FontWeight.Bold, color = colores.textSecondary)
        }
    }
}

















































