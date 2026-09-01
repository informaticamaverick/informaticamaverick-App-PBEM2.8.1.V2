package com.example.myapplication.ui.pantallas.chat

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.ui.pantallas.home.Screen
import com.example.myapplication.ui.componentes.be.modelos.ConfiguracionContextoBe
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.ui.componentes.sistema.ListaShimmerChat
import com.example.myapplication.ui.componentes.sistema.ShimmerCabeceraChat
import com.example.myapplication.ui.componentes.sistema.cabecera.BotonBackCabeceraV3
import com.example.myapplication.ui.componentes.sistema.efectoShimmer
import com.example.myapplication.ui.pantallas.chat.turnos.SelectorTurnoSheet
import com.example.myapplication.uishared.ui.components.chat.*
import com.example.myapplication.viewmodel.chat.ChatViewModel
import com.example.myapplication.viewmodel.home.UbicacionGpsObrero
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File

/**
 * --- PANTALLA DE CONVERSACIÓN (CLIENTE v2026.ELITE) ---
 * [LEY #1]: Pantalla Tonta - Solo delega eventos al ViewModel.
 */
@Composable
fun ChatConversacionPantalla(
    identidadRemota: PrestadorDominio,
    viewModel: ChatViewModel = hiltViewModel(),
    beBrainViewModel: BeCerebroViewModel = hiltViewModel(),
    onBack: () -> Unit,
    navController: androidx.navigation.NavHostController? = null,
    onBudgetClick: (String) -> Unit = {},
    onAddressClick: (String) -> Unit = {},
    onShowSearch: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itemsPaginados = uiState.pagingMessages.collectAsLazyPagingItems()
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    val beConfig = remember {
        ContextoHUD.CHAT_CONVERSACION.crearConfiguracionBase().copy(
            mostrarBe = false,           // 🔥 [v2026.ELITE]: Ocultar Asistente en charla
            mostrarBarraNavegacion = false, // 🔥 [v2026.ELITE]: Ocultar NavBar
            mostrarHerramientas = false
        )
    }
    
    // 🔥 [LEY #12]: Soberanía de contexto HUD mediante Mapa de Registros
    DisposableEffect(Unit) {
        beBrainViewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose { 
            beBrainViewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    // 🔥 [v2026.ELITE]: Colector de acciones soberanas para la conversación
    LaunchedEffect(beBrainViewModel) {
        beBrainViewModel.actionEvent.collect { actionId ->
            if (actionId == "archivo_chat") {
                val rId = identidadRemota.id
                val lId = beBrainViewModel.coordinador.idPerfilSeleccionado.value ?: "personal"
                android.util.Log.d("ChatConversacion", "📁 [BE_ACTION] Abriendo Archivero Multimedia para: $rId")
                navController?.navigate(Screen.ArchiveroChatMultimedia.createRoute(rId, lId))
            }
        }
    }

    val estaGrabando by viewModel.estaGrabando.collectAsStateWithLifecycle()
    val tiempoGrabacion by viewModel.tiempoGrabacion.collectAsStateWithLifecycle()
    val idAudioReproduciendo by viewModel.idAudioReproduciendo.collectAsStateWithLifecycle()
    val progresoAudio by viewModel.progresoAudio.collectAsStateWithLifecycle()

    val userViewModel: ArmadorUsuarioViewModel = hiltViewModel()
    val ubicacionObrero: UbicacionGpsObrero = hiltViewModel()
    val ecosistemaMaestro by userViewModel.ecosistemaMaestro.collectAsStateWithLifecycle()
    val activeGpsAddress by ubicacionObrero.direccionActiva.collectAsStateWithLifecycle()

    val personalAddresses = remember(ecosistemaMaestro) {
        ecosistemaMaestro?.usuario?.direcciones ?: emptyList()
    }

    val busquedaArchivero by viewModel.busqueda.collectAsStateWithLifecycle(initialValue = "")
    val presupuestosHistoricos by viewModel.presupuestosArchivero.collectAsStateWithLifecycle(initialValue = null)
    val productosHistoricos by viewModel.productosArchivero.collectAsStateWithLifecycle(initialValue = null)
    val turnosHistoricos by viewModel.turnosArchivero.collectAsStateWithLifecycle(initialValue = null)
    val visitasHistoricas by viewModel.visitasArchivero.collectAsStateWithLifecycle(initialValue = null)
    val ubicacionesHistoricas by viewModel.direccionesArchivero.collectAsStateWithLifecycle(initialValue = null)
    val imagenesHistoricas by viewModel.imagenesArchivero.collectAsStateWithLifecycle(initialValue = null)

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) } 
    var showArchivero by remember { mutableStateOf(false) } 
    var showOptionsMenu by remember { mutableStateOf(false) } 
    var showDeleteConfirm by remember { mutableStateOf(false) } // 🔥 [NEW]

    var showArchiveroImagenes by remember { mutableStateOf(false) }
    var showArchiveroPresupuestos by remember { mutableStateOf(false) }
    var showArchiveroProductos by remember { mutableStateOf(false) }
    var showArchiveroTurnos by remember { mutableStateOf(false) }
    var showArchiveroVisitas by remember { mutableStateOf(false) }
    var showArchiveroUbicaciones by remember { mutableStateOf(false) }

    val uriFotoTemporal = remember {
        try {
            val archivo = File(context.cacheDir, "camara_temp_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        } catch (_: Exception) { Uri.EMPTY }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito && (uriFotoTemporal != Uri.EMPTY)) {
            viewModel.enviarImagen(uriFotoTemporal)
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.enviarImagen(it) }
    }

    val permisoAudio = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) viewModel.iniciarGrabacionAudio()
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzadorCamara.launch(uriFotoTemporal)
    }

    val identidadesSoberanas by userViewModel.identidadesSoberanas.collectAsStateWithLifecycle()
    val perfilActivo = remember(identidadesSoberanas) {
        identidadesSoberanas.find { it.id == (beBrainViewModel.coordinador.idPerfilSeleccionado.value ?: "personal") }
            ?: identidadesSoberanas.firstOrNull()
    }

    var showTurnoSelector by remember { mutableStateOf(false) }
    var selectedMessageForTurno by remember { mutableStateOf<MensajeEntity?>(null) }

    ChatConversationMaster(
        identidadRemota = identidadRemota,
        itemsPaginados = itemsPaginados,
        idUsuarioActual = currentUserId,
        estaGrabando = estaGrabando,
        tiempoGrabacion = tiempoGrabacion,
        colorAcento = Color(0xFF22D3EE),
        alVolver = {
            android.util.Log.d("MAV_NAV", "🔙 [CHAT_CONV] Volviendo...")
            onBack()
        },
        alEnviarTexto = viewModel::enviarTexto,
        alHacerClickMic = { 
            if (estaGrabando) {
                viewModel.detenerGrabacionYEnviar()
            } else {
                val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (tienePermiso) viewModel.iniciarGrabacionAudio()
                else permisoAudio.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        alCancelarGrabacion = viewModel::cancelarGrabacionAudio,
        alHacerClickAdjunto = { showAttachmentMenu = true },
        alHacerClickCamara = { 
            val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (tienePermiso) lanzadorCamara.launch(uriFotoTemporal)
            else permisoCamara.launch(Manifest.permission.CAMERA)
        },
        alHacerClickImagen = { lanzadorGaleria.launch("image/*") },
        alHacerClickAudio = { id, url, path -> viewModel.reproducirAudio(id, url, path) },
        alHacerClickMapa = { uri ->
            if (uri.isNotBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "No se pudo abrir el mapa", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        },
        alSolicitarUbicacion = { showLocationSheet = true },
        alHacerClickPresupuesto = onBudgetClick,
        alHacerClickProducto = { },
        alGenerarVisitaDesdeMapa = { _ -> },
        alAceptarCita = { msgId -> 
            val msg = itemsPaginados.itemSnapshotList.items
                .filterIsInstance<ItemPaginacionChat.Mensaje>()
                .find { it.entidad.id == msgId }?.entidad
            
            if (msg?.subtipoOperativo == "AGENDA_ABIERTA") {
                selectedMessageForTurno = msg
                showTurnoSelector = true
            } else {
                viewModel.responderACita(msgId, true) 
            }
        },
        alRechazarCita = { msgId -> viewModel.responderACita(msgId, false) },
        alVerCalendarioCita = { _ -> },
        alHacerClickCuerpoCita = { },
        alResponderMensaje = { viewModel.setReplyMessage(it?.let { m -> ItemPaginacionChat.Mensaje(m) }) },
        alEliminarMensaje = { viewModel.eliminarMensaje(it) }, // 🔥 [FIX]
        alHacerClickOpciones = { showOptionsMenu = true }, // 🔥 [FIX]
        alHacerClickBuscar = onShowSearch,
        alHacerClickInfo = { 
            val route = Screen.PerfilPrestador.createRoute(
                providerId = identidadRemota.id,
                companyId = identidadRemota.idEmpresa,
                branchId = if (identidadRemota.tipo == com.example.myapplication.core.dominio.modelos.TipoPrestador.SUCURSAL) identidadRemota.id else null
            )
            navController?.navigate(route)
        },
        estaCargando = uiState.isCargando,
        estaOnline = uiState.isProviderOnline,
        idAudioReproduciendo = idAudioReproduciendo,
        progresoAudio = progresoAudio,
        alHacerClickArchivero = { showArchivero = true }, 
        nombreCliente = perfilActivo?.nombre ?: "Yo",
        nombrePrestador = identidadRemota.titulo,
        fotoLocal = perfilActivo?.photoUrl,
        mostrarAccionesComerciales = true,
        menuAdjuntosAbierto = showAttachmentMenu,
        esModoPrestador = false,
        alSolicitarCompraProducto = { viewModel.solicitarCompraProducto(it) },
        slotContenidoCarga = { 
            com.example.myapplication.ui.componentes.sistema.ListaShimmerChat() 
        }
    )

    if (showOptionsMenu) {
        com.example.myapplication.ui.componentes.sistema.menu.v3.MoldeMenuArmadorV3(
            expanded = showOptionsMenu,
            onDismissRequest = { showOptionsMenu = false },
            alignment = androidx.compose.ui.Alignment.TopEnd,
            arrowOffset = 20.dp,
            verticalOffset = 40.dp
        ) {
            com.example.myapplication.ui.componentes.sistema.menu.v3.MenuGrupoV3 {
                com.example.myapplication.ui.componentes.sistema.menu.v3.MenuSectionHeaderV3("CONVERSACIÓN")
                com.example.myapplication.ui.componentes.sistema.menu.v3.MenuItemEliteV3(
                    label = "Ver Perfil",
                    emoji = "👤",
                    onClick = { 
                        showOptionsMenu = false
                        val route = Screen.PerfilPrestador.createRoute(
                            providerId = identidadRemota.id,
                            companyId = identidadRemota.idEmpresa,
                            branchId = if (identidadRemota.tipo == com.example.myapplication.core.dominio.modelos.TipoPrestador.SUCURSAL) identidadRemota.id else null
                        )
                        navController?.navigate(route)
                    }
                )
                com.example.myapplication.ui.componentes.sistema.menu.v3.MenuItemEliteV3(
                    label = "Agregar a Favoritos",
                    emoji = "⭐",
                    onClick = { showOptionsMenu = false /* TODO */ }
                )
            }

            com.example.myapplication.ui.componentes.sistema.menu.v3.MenuGrupoV3 {
                com.example.myapplication.ui.componentes.sistema.menu.v3.MenuItemEliteV3(
                    label = "Eliminar Chat",
                    emoji = "🗑️",
                    onClick = { 
                        showOptionsMenu = false
                        showDeleteConfirm = true // 🔥 [FIX]
                    }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar conversación?") },
            text = { Text("Se borrarán todos los mensajes y documentos de este chat localmente.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.eliminarConversacion()
                    showDeleteConfirm = false 
                    onBack()
                }) { 
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    if (showArchivero) {
        BackHandler(enabled = showArchivero) { showArchivero = false }
        ArchiveroChatSheet(
            alCerrar = { showArchivero = false },
            listaPresupuestos = presupuestosHistoricos,
            listaImagenes = imagenesHistoricas,
            listaDirecciones = ubicacionesHistoricas,
            listaProductos = productosHistoricos,
            listaTurnos = turnosHistoricos,
            listaVisitas = visitasHistoricas,
            alSeleccionarPestana = { viewModel.cargarSeccionArchivero(it) },
            alHacerClickPresupuesto = { id ->
                onBudgetClick(id)
                showArchivero = false
            },
            alHacerClickMultimedia = { _ -> showArchivero = false },
            alHacerClickUbicacion = { uri ->
                onAddressClick(uri)
                showArchivero = false
            },
            colorAcento = Color(0xFF22D3EE)
        )
    }

    if (showArchiveroImagenes) {
        BackHandler(enabled = showArchiveroImagenes) { showArchiveroImagenes = false }
        ArchiveroImagenesSheet(
            imagenes = imagenesHistoricas,
            alCerrar = { showArchiveroImagenes = false },
            alHacerClick = { _ -> showArchiveroImagenes = false }
        )
    }

    if (showArchiveroPresupuestos) {
        BackHandler(enabled = showArchiveroPresupuestos) { showArchiveroPresupuestos = false }
        ArchiveroPresupuestosSheet(
            presupuestos = presupuestosHistoricos,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = viewModel::buscar,
            alCerrar = { showArchiveroPresupuestos = false },
            alSeleccionar = { p ->
                onBudgetClick(p.idPresupuesto)
                showArchiveroPresupuestos = false
            }
        )
    }

    if (showArchiveroProductos) {
        BackHandler(enabled = showArchiveroProductos) { showArchiveroProductos = false }
        ArchiveroProductosSheet(
            productos = productosHistoricos,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = viewModel::buscar,
            alCerrar = { showArchiveroProductos = false },
            alSeleccionar = { _ -> showArchiveroProductos = false }
        )
    }

    if (showArchiveroTurnos) {
        BackHandler(enabled = showArchiveroTurnos) { showArchiveroTurnos = false }
        ArchiveroEventosSheet(
            eventos = turnosHistoricos ?: emptyList(),
            busqueda = busquedaArchivero,
            alCambiarBusqueda = viewModel::buscar,
            alCerrar = { showArchiveroTurnos = false },
            alSeleccionar = { _ -> showArchiveroTurnos = false },
            tituloOverride = "Historial de Turnos",
            subtituloOverride = "Atención en establecimiento"
        )
    }

    if (showArchiveroVisitas) {
        BackHandler(enabled = showArchiveroVisitas) { showArchiveroVisitas = false }
        ArchiveroEventosSheet(
            eventos = visitasHistoricas ?: emptyList(),
            busqueda = busquedaArchivero,
            alCambiarBusqueda = viewModel::buscar,
            alCerrar = { showArchiveroVisitas = false },
            alSeleccionar = { _ -> showArchiveroVisitas = false },
            tituloOverride = "Historial de Visitas",
            subtituloOverride = "Servicios a domicilio"
        )
    }

    if (showArchiveroUbicaciones) {
        BackHandler(enabled = showArchiveroUbicaciones) { showArchiveroUbicaciones = false }
        ArchiveroUbicacionesSheet(
            ubicaciones = ubicacionesHistoricas,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = viewModel::buscar,
            alCerrar = { showArchiveroUbicaciones = false },
            alSeleccionar = { m ->
                viewModel.enviarUbicacion(m.latitud ?: 0.0, m.longitud ?: 0.0, m.direccionTexto ?: "")
                showArchiveroUbicaciones = false
            }
        )
    }

    if (showAttachmentMenu) {
        BackHandler(enabled = showAttachmentMenu) { showAttachmentMenu = false }
        MenuAdjuntos(
            alCerrar = { showAttachmentMenu = false },
            alHacerClickGaleria = { lanzadorGaleria.launch("image/*"); showAttachmentMenu = false },
            alHacerClickCamara = {
                val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (tienePermiso) lanzadorCamara.launch(uriFotoTemporal)
                else permisoCamara.launch(Manifest.permission.CAMERA)
                showAttachmentMenu = false
            },
            alHacerClickPdf = { /* TODO */ showAttachmentMenu = false },
            alHacerClickUbicacion = { showLocationSheet = true; showAttachmentMenu = false },
            alHacerClickArchiveroImagenes = {
                viewModel.cargarSeccionArchivero(1)
                showArchiveroImagenes = true
                showAttachmentMenu = false
            },
            alHacerClickArchiveroPresupuestos = {
                viewModel.cargarSeccionArchivero(0)
                showArchiveroPresupuestos = true
                showAttachmentMenu = false
            },
            alHacerClickArchiveroProductos = {
                viewModel.cargarSeccionArchivero(3)
                showArchiveroProductos = true
                showAttachmentMenu = false
            },
            alHacerClickArchiveroTurnos = {
                viewModel.cargarSeccionArchivero(4)
                showArchiveroTurnos = true
                showAttachmentMenu = false
            },
            alHacerClickArchiveroVisitas = {
                viewModel.cargarSeccionArchivero(5)
                showArchiveroVisitas = true
                showAttachmentMenu = false
            },
            alHacerClickArchiveroUbicaciones = {
                viewModel.cargarSeccionArchivero(2)
                showArchiveroUbicaciones = true
                showAttachmentMenu = false
            },
            colorAcento = Color(0xFF22D3EE),
            mostrarSeccionArchivero = true 
        )
    }

    if (showLocationSheet) {
        BackHandler(enabled = showLocationSheet) { showLocationSheet = false }
        EnviarUbicacionSheet(
            direccionesGuardadas = personalAddresses,
            ubicacionGps = activeGpsAddress,
            alCerrar = { showLocationSheet = false },
            alAlternarGps = { ubicacionObrero.toggleGps(context) },
            alSeleccionar = { lat, lng, addr ->
                viewModel.enviarUbicacion(lat, lng, addr)
                showLocationSheet = false
            }
        )
    }

    if (showTurnoSelector && selectedMessageForTurno != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        BackHandler(enabled = showTurnoSelector) { showTurnoSelector = false }
        SelectorTurnoSheet(
            mensaje = selectedMessageForTurno!!,
            alCerrar = { showTurnoSelector = false },
            alConfirmar = { fecha, bloque, idRec ->
                viewModel.responderACitaAbierta(
                    mensajeId = selectedMessageForTurno!!.id,
                    fechaElegida = fecha.toString(),
                    horaElegida = bloque.horaTexto,
                    idRecursoElegido = idRec
                )
                showTurnoSelector = false
            }
        )
    }
}
