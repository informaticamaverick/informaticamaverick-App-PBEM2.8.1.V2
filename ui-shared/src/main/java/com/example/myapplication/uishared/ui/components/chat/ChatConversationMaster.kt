package com.example.myapplication.uishared.ui.components.chat

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.ProductoMensajeDominio
import com.example.myapplication.core.R as CoreR
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PANTALLA MAESTRA DE CONVERSACIÓN (V2026.FINAL) ---
 * ELITE SSOT: Pantalla universal y stateless para el ecosistema.
 */
@Composable
fun ChatConversationMaster(
    identidadRemota: PrestadorDominio,
    itemsPaginados: LazyPagingItems<ItemPaginacionChat>,
    idUsuarioActual: String,
    estaGrabando: Boolean,
    tiempoGrabacion: Int,
    colorAcento: Color,
    alVolver: () -> Unit,
    alEnviarTexto: (String) -> Unit,
    alHacerClickMic: () -> Unit,
    alHacerClickCamara: () -> Unit = {},
    alCancelarGrabacion: () -> Unit,
    alHacerClickAdjunto: () -> Unit,
    alHacerClickImagen: (String) -> Unit = {},
    alHacerClickAudio: (String, String?, String?) -> Unit = { _, _, _ -> },
    alHacerClickMapa: (String) -> Unit = {},
    alSolicitarUbicacion: () -> Unit = {},
    alHacerClickPresupuesto: (String) -> Unit = {},
    alAceptarCita: (String) -> Unit = {},
    alRechazarCita: (String) -> Unit = {},
    alVerCalendarioCita: (String) -> Unit = {},
    alHacerClickCuerpoCita: (String) -> Unit = {},
    alCrearPresupuestoDesdeSolicitud: (String) -> Unit = {},
    alGenerarVisitaDesdeMapa: (MensajeEntity) -> Unit = {},
    alCambiarEstadoEscritura: (Boolean) -> Unit = {},
    mensajeRespuesta: MensajeEntity? = null,
    alResponderMensaje: (MensajeEntity?) -> Unit = {},
    alEliminarMensaje: (String) -> Unit = {},
    alHacerClickOpciones: () -> Unit = {},
    alHacerClickBuscar: () -> Unit = {},
    alHacerClickInfo: () -> Unit = {},
    estaCargando: Boolean = false,
    estaOnline: Boolean = false,
    idAudioReproduciendo: String? = null,
    progresoAudio: Float = 0f,
    nombreCliente: String = "Cliente",
    nombrePrestador: String = "Prestador",
    fotoLocal: Any? = null,
    mostrarAccionesComerciales: Boolean = false,
    alHacerClickProducto: () -> Unit = {},
    alSolicitarCompraProducto: (ProductoMensajeDominio) -> Unit = {},
    alHacerClickArchivero: () -> Unit = {},
    alHacerClickCitaTactiva: (() -> Unit)? = null,
    esModoPrestador: Boolean = false,
    menuAdjuntosAbierto: Boolean = false,
    alSeleccionarEmoji: (String) -> Unit = {},
    slotContenidoCarga: @Composable (() -> Unit)? = null,
    slotMenuFab: @Composable (BoxScope.(PaddingValues) -> Unit)? = null,
    paddingExtraBotonAbajo: androidx.compose.ui.unit.Dp = 0.dp
) {
    var textoEntrada by remember { mutableStateOf("") }
    var mostrarMenuEmojis by remember { mutableStateOf(false) }
    var mensajeSeleccionadoContextual by remember { mutableStateOf<MensajeEntity?>(null) }
    var idMensajeDestacado by remember { mutableStateOf<String?>(null) }
    
    val estadoLista = rememberLazyListState()
    val alcanceCorrutina = rememberCoroutineScope()

    LaunchedEffect(idMensajeDestacado) {
        if (idMensajeDestacado != null) {
            kotlinx.coroutines.delay(2000)
            idMensajeDestacado = null
        }
    }

    val fechaFlotante by remember {
        derivedStateOf {
            val visibleItem = estadoLista.layoutInfo.visibleItemsInfo.firstOrNull()
            if (visibleItem != null && visibleItem.index < itemsPaginados.itemCount) {
                val item = itemsPaginados[visibleItem.index]
                when (item) {
                    is ItemPaginacionChat.Mensaje -> {
                        SimpleDateFormat("d 'de' MMMM", Locale.getDefault()).format(Date(item.entidad.marcaTiempo))
                    }
                    is ItemPaginacionChat.SeparadorFecha -> item.fecha
                    else -> null
                }
            } else null
        }
    }

    val primerItemVisible by remember { derivedStateOf { estadoLista.firstVisibleItemIndex } }
    val mostrarBotonAbajo by remember { derivedStateOf { primerItemVisible > 5 } }
    val fraccionColapso by remember {
        derivedStateOf {
            if (primerItemVisible > 0) 1f
            else (estadoLista.firstVisibleItemScrollOffset / 200f).coerceIn(0f, 1f)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0), 
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {
                if (mensajeRespuesta != null) {
                    PrevisualizacionRespuesta(
                        mensaje = mensajeRespuesta,
                        alCancelar = { alResponderMensaje(null) },
                        colorAcento = colorAcento,
                        colorFondo = Color(0xFF0F172A),
                        esMio = mensajeRespuesta.idEmisor == idUsuarioActual
                    )
                }
                
                BarraEntradaMensaje(
                    valor = textoEntrada,
                    alCambiarValor = { 
                        textoEntrada = it
                        alCambiarEstadoEscritura(it.isNotEmpty())
                    },
                    alEnviar = {
                        if (it.isNotBlank()) {
                            alEnviarTexto(it.trim())
                            textoEntrada = ""
                        }
                    },
                    colorAcento = colorAcento,
                    alHacerClickAdjunto = alHacerClickAdjunto,
                    alHacerClickMic = alHacerClickMic,
                    alHacerClickCamara = alHacerClickCamara,
                    alHacerClickEmoji = { mostrarMenuEmojis = !mostrarMenuEmojis },
                    estaGrabando = estaGrabando,
                    tiempoGrabacion = tiempoGrabacion,
                    alCancelarAudio = alCancelarGrabacion,
                    menuAdjuntosAbierto = menuAdjuntosAbierto,
                    modifier = Modifier.navigationBarsPadding() 
                )

                if (mostrarMenuEmojis) {
                    SelectorEmojis(alSeleccionarEmoji = { 
                        textoEntrada += it
                        alSeleccionarEmoji(it) 
                    })
                }

            }
        },
        containerColor = Color(0xFF030712)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = estadoLista,
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                // [FIX] "bottom" suma paddingExtraBotonAbajo (56dp en el prestador, 0dp en el
                // cliente) para que la última burbuja nunca quede tapada por el FAB de acciones
                // rápidas flotando en la esquina inferior derecha.
                contentPadding = PaddingValues(top = 100.dp, bottom = 12.dp + paddingExtraBotonAbajo, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) 
            ) {
                items(
                    count = itemsPaginados.itemCount,
                    key = itemsPaginados.itemKey { 
                        when (it) {
                            is ItemPaginacionChat.Mensaje -> "msg_${it.entidad.id}"
                            is ItemPaginacionChat.SeparadorFecha -> "sep_${it.fecha}"
                        }
                    }
                ) { indice ->
                    when (val item = itemsPaginados[indice]) {
                        is ItemPaginacionChat.Mensaje -> {
                            ContenedorRespuesta(
                                alResponder = { alResponderMensaje(item.entidad) },
                                alHacerLongClick = { mensajeSeleccionadoContextual = item.entidad },
                                colorAcento = colorAcento,
                                esDestacado = idMensajeDestacado == item.entidad.id
                            ) {
                                OrquestadorBurbujas(
                                    mensaje = item.entidad,
                                    idUsuarioActual = idUsuarioActual,
                                    presupuesto = item.presupuesto,
                                    // [FIX] Se oscurece colorAcento un 28% para el fondo de la burbuja: cada
                                    // app ya trae su propio acento (naranja en prestador, celeste en cliente),
                                    // pero el celeste del cliente es demasiado claro para texto blanco encima
                                    // si se usa tal cual. Oscurecer mantiene la identidad de cada app sin
                                    // perder contraste, sin necesidad de un color fijo compartido.
                                    colorFondoMio = Color(
                                        red = colorAcento.red * 0.72f,
                                        green = colorAcento.green * 0.72f,
                                        blue = colorAcento.blue * 0.72f,
                                        alpha = colorAcento.alpha
                                    ),
                                    colorFondoOtro = Color(0xFF1E293B),
                                    idAudioReproduciendo = idAudioReproduciendo,
                                    progresoAudio = progresoAudio,
                                    alHacerClickImagen = alHacerClickImagen,
                                    alHacerClickAudio = alHacerClickAudio,
                                    alHacerClickMapa = alHacerClickMapa,
                                    alHacerClickPresupuesto = alHacerClickPresupuesto,
                                    alAceptarCita = alAceptarCita,
                                    alRechazarCita = alRechazarCita,
                                    alVerCalendarioCita = alVerCalendarioCita,
                                    alHacerClickCuerpoCita = alHacerClickCuerpoCita,
                                    alCrearPresupuestoDesdeSolicitud = alCrearPresupuestoDesdeSolicitud,
                                    nombreCliente = nombreCliente,
                                    nombrePrestador = nombrePrestador,
                                    fotoRemota = identidadRemota.urlMiniatura,
                                    fotoLocal = fotoLocal,
                                    mostrarAccionesComerciales = mostrarAccionesComerciales,
                                    alGenerarVisitaDesdeMapa = alGenerarVisitaDesdeMapa,
                                    alSolicitarCompraProducto = alSolicitarCompraProducto,
                                    alHacerClickSistema = { targetId ->
                                        if (targetId != null) {
                                            idMensajeDestacado = targetId
                                            alcanceCorrutina.launch {
                                                for (i in 0 until itemsPaginados.itemCount) {
                                                    val item = itemsPaginados[i]
                                                    if (item is ItemPaginacionChat.Mensaje && item.entidad.id == targetId) {
                                                        estadoLista.animateScrollToItem(i)
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        is ItemPaginacionChat.SeparadorFecha -> SeparadorFechaChat(fechaTexto = item.fecha)
                        null -> {}
                    }
                }
            }

            BarraCabeceraChat(
                titulo = identidadRemota.titulo,
                urlFoto = identidadRemota.urlMiniatura,
                estaOnline = estaOnline,
                estaVerificado = identidadRemota.estaVerificado,
                alVolver = alVolver,
                colorAcento = colorAcento,
                fraccionColapso = fraccionColapso,
                alHacerClickBuscar = alHacerClickBuscar,
                alHacerClickOpciones = alHacerClickOpciones,
                alHacerClickInfo = alHacerClickInfo
            )

            slotMenuFab?.invoke(this, paddingValues)

            AnimatedVisibility(
                visible = mostrarBotonAbajo,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.7f)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 8.dp + paddingExtraBotonAbajo,
                        end = 8.dp
                    )
            ) {
                Surface(
                    onClick = { alcanceCorrutina.launch { estadoLista.animateScrollToItem(0) } },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.KeyboardDoubleArrowDown, null, tint = colorAcento, modifier = Modifier.size(24.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = estadoLista.isScrollInProgress && fechaFlotante != null,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut(animationSpec = tween(800)) + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 110.dp).zIndex(150f)
            ) {
                Surface(color = Color(0xFF1E293B).copy(alpha = 0.9f), shape = RoundedCornerShape(20.dp), border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)), modifier = Modifier.padding(8.dp)) {
                    Text(text = fechaFlotante ?: "", modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp), color = Color.White)
                }
            }

            if (estaCargando) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).zIndex(200f), contentAlignment = Alignment.Center) {
                    if (slotContenidoCarga != null) slotContenidoCarga()
                    else CircularProgressIndicator(color = colorAcento)
                }
            }

            if (mensajeSeleccionadoContextual != null) {
                MenuContextualMensaje(
                    alResponder = { alResponderMensaje(mensajeSeleccionadoContextual!!) },
                    alCopiar = { },
                    alReenviar = { },
                    alEliminar = { },
                    alCerrar = { mensajeSeleccionadoContextual = null }
                )
            }
        }
    }
}

@Composable
fun SeparadorFechaChat(fechaTexto: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
        Surface(color = Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp), border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))) {
            Text(text = fechaTexto.uppercase(), modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, letterSpacing = 1.2.sp), color = Color.White.copy(alpha = 0.6f))
        }
    }
}
