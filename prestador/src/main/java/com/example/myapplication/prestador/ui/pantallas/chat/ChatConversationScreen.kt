package com.example.myapplication.prestador.ui.pantallas.chat

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.ui.Alignment
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.datos.local.entidades.TipoMensaje
import com.example.myapplication.core.dominio.mapeadores.EquipoTrabajoMappers
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.utilidades.ChatIdHelper
import com.example.myapplication.prestador.ui.pantallas.chat.wizard.WizardTurnoSheet
import com.example.myapplication.prestador.ui.pantallas.chat.wizard.WizardVisitaSheet
import com.example.myapplication.prestador.viewmodel.chat.PrestadorChatViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import com.example.myapplication.core.viewmodel.chat.ArchiveroViewModel
import com.example.myapplication.prestador.ui.pantallas.chat.componentes.*
import com.example.myapplication.uishared.ui.components.chat.*
import com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog
import androidx.lifecycle.viewModelScope
import java.io.File
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun ChatConversationScreen(
    userId: String,
    userName: String,
    userPhotoUrl: String? = null,
    providerId: String,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCreateBudget: (String) -> Unit = {},
    chatViewModel: PrestadorChatViewModel = hiltViewModel(),
    archiveroViewModel: ArchiveroViewModel = hiltViewModel(), 
    identidadViewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
) {
    val estadoUi by chatViewModel.uiState.collectAsStateWithLifecycle()
    val stateDeep by identidadViewModel.state.collectAsStateWithLifecycle()
    val maestro = stateDeep.ecosistema
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val busquedaArchivero by archiveroViewModel.busqueda.collectAsStateWithLifecycle()
    val presupuestosHistoricos by archiveroViewModel.obtenerPresupuestos().collectAsState(null)
    val productosHistoricos by archiveroViewModel.obtenerProductos().collectAsState(null)
    val turnosHistoricos by archiveroViewModel.obtenerTurnos().collectAsState(null)
    val visitasHistoricas by archiveroViewModel.obtenerVisitas().collectAsState(null)
    val ubicacionesHistoricas by archiveroViewModel.obtenerUbicaciones().collectAsState(null)
    val imagenesHistoricas by archiveroViewModel.obtenerImagenes().collectAsState(null)

    LaunchedEffect(maestro?.cuenta?.idPerfilActivo) {
        val idLocal = maestro?.cuenta?.idPerfilActivo ?: providerId
        val idChat = ChatIdHelper.generateChatId(idLocal, userId)
        archiveroViewModel.inicializar(idChat, idLocal, userId)
    }
    
    val estaGrabando by chatViewModel.estaGrabando.collectAsStateWithLifecycle()
    val tiempoGrabacion by chatViewModel.tiempoGrabacion.collectAsStateWithLifecycle()
    val idAudioReproduciendo by chatViewModel.idAudioReproduciendo.collectAsStateWithLifecycle()
    val progresoAudio by chatViewModel.progresoAudio.collectAsStateWithLifecycle()

    val direccionesChat by chatViewModel.direccionesChatFiltradas.collectAsStateWithLifecycle()
    val presupuestosChat by chatViewModel.presupuestosChatOrdenados.collectAsStateWithLifecycle()

    var budgetForPreview by remember { mutableStateOf<PresupuestoConItems?>(null) }

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) } 
    var showArchivero by remember { mutableStateOf(false) }
    var showVisitaSheet by remember { mutableStateOf(false) } 
    var showTurnoSheet by remember { mutableStateOf(false) } 
    var showBudgetSheet by remember { mutableStateOf(false) } 
    var showProductSheet by remember { mutableStateOf(false) } 
    
    var showArchiveroImagenes by remember { mutableStateOf(false) }
    var showArchiveroPresupuestos by remember { mutableStateOf(false) }
    var showArchiveroProductos by remember { mutableStateOf(false) }
    var showArchiveroTurnos by remember { mutableStateOf(false) }
    var showArchiveroVisitas by remember { mutableStateOf(false) }
    var showArchiveroUbicaciones by remember { mutableStateOf(false) }

    var gpsAddress by remember { mutableStateOf<DireccionDominio?>(null) }
    var capturedMessageForVisita by remember { mutableStateOf<MensajeEntity?>(null) }
    var showConfirmMapActionDialog by remember { mutableStateOf(false) }

    val personalAddresses = remember(maestro) {
        val list = mutableListOf<DireccionDominio>()
        maestro?.prestador?.direcciones?.forEach { list.add(it) }
        maestro?.empresas?.forEach { emp ->
            emp.sucursales.forEach { suc ->
                suc.direccion?.let { list.add(it) }
            }
        }
        list
    }

    val uriFotoTemporal = remember {
        try {
            val archivo = File(context.cacheDir, "camara_temp_prestador_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        } catch (_: Exception) { Uri.EMPTY }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito && (uriFotoTemporal != Uri.EMPTY)) {
            val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
            chatViewModel.enviarImagen(uriFotoTemporal, idEmisor, userId)
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
            chatViewModel.enviarImagen(it, idEmisor, userId) 
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzadorCamara.launch(uriFotoTemporal)
    }

    val permisoAudio = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) chatViewModel.iniciarGrabacionAudio()
    }


    val itemsPaginados = estadoUi.pagingMessages.collectAsLazyPagingItems()

    val alEnviarTexto: (String) -> Unit = { texto ->
        val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
        chatViewModel.enviarTexto(texto, idEmisor, userId, userId)
    }

    ChatConversationMaster(
        identidadRemota = estadoUi.identidadRemota ?: PrestadorDominio(
            id = userId,
            titulo = userName,
            urlMiniatura = userPhotoUrl
        ),
        itemsPaginados = itemsPaginados,
        idUsuarioActual = maestro?.cuenta?.idPerfilActivo ?: providerId,
        estaGrabando = estaGrabando,
        tiempoGrabacion = tiempoGrabacion,
        colorAcento = Color(0xFFF97316),
        alVolver = onBack,
        alEnviarTexto = alEnviarTexto,
        alHacerClickMic = { 
            if (tiempoGrabacion > 0) {
                chatViewModel.detenerGrabacionYEnviar(maestro?.cuenta?.idPerfilActivo ?: providerId, userId)
            } else {
                val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (tienePermiso) chatViewModel.iniciarGrabacionAudio()
                else permisoAudio.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        alCancelarGrabacion = { chatViewModel.cancelarGrabacionAudio() },
        alHacerClickAdjunto = { showAttachmentMenu = true },
        alHacerClickCamara = {
            val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (tienePermiso) lanzadorCamara.launch(uriFotoTemporal)
            else permisoCamara.launch(Manifest.permission.CAMERA)
        },
        alHacerClickAudio = { id, url, path -> chatViewModel.reproducirAudio(id, url, path) },
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
        alHacerClickArchivero = { showArchivero = true },
        alGenerarVisitaDesdeMapa = { msg ->
            capturedMessageForVisita = msg
            showConfirmMapActionDialog = true
        },
        alHacerClickImagen = { lanzadorGaleria.launch("image/*") },
        alHacerClickProducto = { showProductSheet = true },
        alHacerClickPresupuesto = { budgetId ->
            scope.launch {
                chatViewModel.obtenerPresupuesto(budgetId).collect { budget ->
                    budgetForPreview = budget
                }
            }
        },
        estaOnline = false,
        menuAdjuntosAbierto = showAttachmentMenu,
        nombreCliente = estadoUi.identidadRemota?.titulo ?: userName,
        nombrePrestador = maestro?.prestador?.perfil?.titulo ?: "Yo",
        fotoLocal = maestro?.prestador?.perfil?.urlMiniatura ?: maestro?.prestador?.perfil?.urlFoto,
        mostrarAccionesComerciales = false,
        alSolicitarCompraProducto = {}, 
        mensajeRespuesta = estadoUi.replyingToMessage,
        alResponderMensaje = { chatViewModel.setReplyMessage(it) },
        idAudioReproduciendo = idAudioReproduciendo,
        progresoAudio = progresoAudio,
        esModoPrestador = true,
        paddingExtraBotonAbajo = 56.dp, 
        slotMenuFab = { p ->
            MenuFab(
                alHacerClickAccion = { accion ->
                    when (accion) {
                        AccionFab.NUEVO_PRESUPUESTO -> showBudgetSheet = true
                        AccionFab.VISITA_TECNICA -> showVisitaSheet = true
                        AccionFab.NUEVO_TURNO -> showTurnoSheet = true
                        AccionFab.ENVIAR_PRODUCTO -> showProductSheet = true
                        AccionFab.UBICACION -> showLocationSheet = true
                        AccionFab.FINALIZAR_TRABAJO -> {
                            val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
                            chatViewModel.enviarFinalizacionServicio(idEmisor, userId)
                        }
                    }
                },
                colorAcento = Color(0xFFF97316),
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(bottom = p.calculateBottomPadding() + 8.dp, end = 8.dp)
            )
        }
    )

    if (showAttachmentMenu) {
        MenuAdjuntos(
            alCerrar = { showAttachmentMenu = false },
            alHacerClickGaleria = { lanzadorGaleria.launch("image/*"); showAttachmentMenu = false },
            alHacerClickCamara = { 
                val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (tienePermiso) lanzadorCamara.launch(uriFotoTemporal)
                else permisoCamara.launch(Manifest.permission.CAMERA)
                showAttachmentMenu = false
            },
            alHacerClickPdf = { showAttachmentMenu = false },
            alHacerClickArchiveroImagenes = { 
                chatViewModel.cargarSeccionArchivero(1)
                showArchiveroImagenes = true
                showAttachmentMenu = false 
            },
            alHacerClickArchiveroPresupuestos = { 
                chatViewModel.cargarSeccionArchivero(0)
                showArchiveroPresupuestos = true
                showAttachmentMenu = false 
            },
            alHacerClickArchiveroProductos = { 
                chatViewModel.cargarSeccionArchivero(3)
                showArchiveroProductos = true
                showAttachmentMenu = false 
            },
            alHacerClickArchiveroTurnos = { 
                chatViewModel.cargarSeccionArchivero(4)
                showArchiveroTurnos = true
                showAttachmentMenu = false 
            },
            alHacerClickArchiveroVisitas = { 
                chatViewModel.cargarSeccionArchivero(5)
                showArchiveroVisitas = true
                showAttachmentMenu = false 
            },
            alHacerClickArchiveroUbicaciones = { 
                chatViewModel.cargarSeccionArchivero(2)
                showArchiveroUbicaciones = true
                showAttachmentMenu = false 
            },
            colorAcento = Color(0xFFF97316)
        )
    }

    if (showArchiveroImagenes) {
        ArchiveroImagenesSheet(
            imagenes = imagenesHistoricas,
            alCerrar = { showArchiveroImagenes = false },
            alHacerClick = { _ ->
                showArchiveroImagenes = false
            }
        )
    }

    if (showArchiveroPresupuestos) {
        ArchiveroPresupuestosSheet(
            presupuestos = presupuestosHistoricos,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = archiveroViewModel::buscar,
            alCerrar = { showArchiveroPresupuestos = false },
            alSeleccionar = { _ ->
                showArchiveroPresupuestos = false
            }
        )
    }

    if (showArchiveroProductos) {
        ArchiveroProductosSheet(
            productos = productosHistoricos,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = archiveroViewModel::buscar,
            alCerrar = { showArchiveroProductos = false },
            alSeleccionar = { _ ->
                showArchiveroProductos = false
            }
        )
    }

    if (showArchiveroTurnos) {
        ArchiveroEventosSheet(
            eventos = turnosHistoricos,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = archiveroViewModel::buscar,
            alCerrar = { showArchiveroTurnos = false },
            alSeleccionar = { e ->
                showArchiveroTurnos = false
            },
            tituloOverride = "Historial de Turnos",
            subtituloOverride = "Atención en establecimiento"
        )
    }

    if (showArchiveroVisitas) {
        ArchiveroEventosSheet(
            eventos = visitasHistoricas,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = archiveroViewModel::buscar,
            alCerrar = { showArchiveroVisitas = false },
            alSeleccionar = { e ->
                showArchiveroVisitas = false
            },
            tituloOverride = "Historial de Visitas",
            subtituloOverride = "Servicios a domicilio"
        )
    }

    if (showArchiveroUbicaciones) {
        ArchiveroUbicacionesSheet(
            ubicaciones = ubicacionesHistoricas,
            busqueda = busquedaArchivero,
            alCambiarBusqueda = archiveroViewModel::buscar,
            alCerrar = { showArchiveroUbicaciones = false },
            alSeleccionar = { m ->
                chatViewModel.enviarUbicacion(m.latitud ?: 0.0, m.longitud ?: 0.0, m.direccionTexto ?: "", maestro?.cuenta?.idPerfilActivo ?: providerId, userId)
                showArchiveroUbicaciones = false
            }
        )
    }

    if (showTurnoSheet) {
        val categoriaPrincipal = maestro?.prestador?.perfil?.idCategorias?.firstOrNull()
        val todasLasCategorias = stateDeep.todasLasCategorias
        val catMeta = todasLasCategorias.find { it.id == categoriaPrincipal }
        val iconoCat = catMeta?.icono ?: "🔧"
        val nombreCat = catMeta?.nombre ?: (categoriaPrincipal ?: "Servicio")

        WizardTurnoSheet(
            idSucursal = maestro?.cuenta?.idPerfilActivo ?: providerId,
            nombreCliente = estadoUi.identidadRemota?.titulo ?: userName,
            urlFotoCliente = userPhotoUrl,
            categoriaActual = nombreCat,
            iconoActual = iconoCat,
            alCerrar = { showTurnoSheet = false },
            alConfirmarCerrado = { fecha, hora, direccion, idRec, nomRec ->
                val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
                chatViewModel.enviarPropuestaCita(
                    idEmisor = idEmisor,
                    idReceptor = userId,
                    tipo = com.example.myapplication.core.datos.local.entidades.TipoMensaje.TURNO,
                    fecha = fecha,
                    hora = hora,
                    direccion = direccion,
                    categoria = categoriaPrincipal, 
                    nombreRecurso = nomRec,
                    idRecurso = idRec,
                    idPresupuesto = null 
                )
                showTurnoSheet = false
            },
            alConfirmarAbierto = { direccion, idRec, nomRec ->
                val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
                chatViewModel.enviarPropuestaAgendaAbierta(
                    idEmisor = idEmisor,
                    idReceptor = userId,
                    tipo = com.example.myapplication.core.datos.local.entidades.TipoMensaje.TURNO,
                    direccion = direccion,
                    categoria = categoriaPrincipal,
                    recursosIds = listOfNotNull(idRec),
                    nombreRecursoReferencia = nomRec
                )
                showTurnoSheet = false
            }
        )
    }

    if (showVisitaSheet) {
        val idSucursal = maestro?.cuenta?.idPerfilActivo ?: providerId
        val sucursalActual = maestro?.empresas?.flatMap { it.sucursales }?.find { it.id == idSucursal }
        val equipo = sucursalActual?.equipoTrabajo ?: emptyList()
        val categoriaPrincipal = maestro?.prestador?.perfil?.idCategorias?.firstOrNull()
        val todasLasCategorias = stateDeep.todasLasCategorias
        val catMeta = todasLasCategorias.find { it.id == categoriaPrincipal }
        val iconoCat = catMeta?.icono ?: "🛠️"
        val nombreCat = catMeta?.nombre ?: (categoriaPrincipal ?: "Servicio")

        WizardVisitaSheet(
            idSucursal = idSucursal,
            nombreCliente = estadoUi.identidadRemota?.titulo ?: userName,
            urlFotoCliente = userPhotoUrl,
            categoriaActual = nombreCat,
            iconoActual = iconoCat,
            ubicacionesChat = direccionesChat,
            presupuestosChat = presupuestosChat,
            direccionInicial = capturedMessageForVisita,
            alCerrar = { 
                showVisitaSheet = false
                capturedMessageForVisita = null
            },
            alConfirmarCerrado = { fecha, hora, direccion, equipoIds, idPre ->
                val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
                val primerIntegrante = equipo.find { equipoIds.contains(it.id) }
                val nombresEquipo = equipo.filter { equipoIds.contains(it.id) }
                    .joinToString(", ") { "${it.nombre} ${it.apellido}" }
                    .ifBlank { maestro?.prestador?.perfil?.titulo ?: "Yo" }

                val fotoRecurso = (primerIntegrante?.avatarEmoji ?: (maestro?.prestador?.perfil?.urlMiniatura ?: maestro?.prestador?.perfil?.urlFoto))?.toString()
                val cargoRecurso = primerIntegrante?.cargo ?: "Responsable"

                chatViewModel.enviarPropuestaCita(
                    idEmisor = idEmisor,
                    idReceptor = userId,
                    tipo = com.example.myapplication.core.datos.local.entidades.TipoMensaje.VISITA,
                    fecha = fecha,
                    hora = hora,
                    direccion = direccion,
                    categoria = categoriaPrincipal,
                    nombreRecurso = nombresEquipo,
                    idRecurso = null,
                    fotoRecurso = fotoRecurso,
                    cargoRecurso = cargoRecurso,
                    idPresupuesto = idPre
                )
                showVisitaSheet = false
                capturedMessageForVisita = null
            },
            alConfirmarAbierto = { direccion, equipoIds, idPre ->
                val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
                chatViewModel.enviarPropuestaAgendaAbierta(
                    idEmisor = idEmisor,
                    idReceptor = userId,
                    tipo = com.example.myapplication.core.datos.local.entidades.TipoMensaje.VISITA,
                    direccion = direccion,
                    categoria = categoriaPrincipal,
                    recursosIds = equipoIds,
                    nombreRecursoReferencia = "Equipo Técnico"
                )
                showVisitaSheet = false
                capturedMessageForVisita = null
            }
        )
    }

    if (showProductSheet) {
        EnviarProductoSheet(
            idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId,
            idReceptor = userId,
            onClose = { showProductSheet = false }
        )
    }

    if (showBudgetSheet) {
        onNavigateToCreateBudget(userId)
        showBudgetSheet = false
    }

    if (showArchivero) {
        ArchiveroChatSheet(
            alCerrar = { showArchivero = false },
            listaPresupuestos = presupuestosHistoricos,
            listaImagenes = imagenesHistoricas,
            listaDirecciones = ubicacionesHistoricas,
            listaProductos = productosHistoricos,
            listaTurnos = turnosHistoricos,
            listaVisitas = visitasHistoricas,
            alSeleccionarPestana = { chatViewModel.cargarSeccionArchivero(it) },
            alHacerClickPresupuesto = { showArchivero = false },
            alHacerClickMultimedia = { _ -> showArchivero = false },
            alHacerClickUbicacion = { uri -> 
                if (uri.isNotBlank()) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                    } catch (_: Exception) {}
                }
                showArchivero = false 
            },
            colorAcento = Color(0xFFF97316)
        )
    }

    if (showLocationSheet) {
        EnviarUbicacionSheet(
            direccionesGuardadas = personalAddresses,
            ubicacionGps = gpsAddress,
            alCerrar = { showLocationSheet = false },
            alAlternarGps = { 
                identidadViewModel.detectarUbicacionActual { dir -> gpsAddress = dir }
            },
            alSeleccionar = { lat, lng, addr -> 
                val idEmisor = maestro?.cuenta?.idPerfilActivo ?: providerId
                chatViewModel.enviarUbicacion(lat, lng, addr, idEmisor, userId)
                showLocationSheet = false 
            }
        )
    }

    if (budgetForPreview != null) {
        val prestadorUi = maestro?.let { com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deCompletoAModeloUi(it.prestador) } 
            ?: PrestadorDominio(titulo = "Yo")
            
        com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog(
            prestador = prestadorUi,
            relacion = budgetForPreview!!,
            onDismiss = { budgetForPreview = null },
            showSendButton = false
        )
    }
}
