package com.example.myapplication.prestador.viewmodel.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.myapplication.core.datos.local.dao.PresupuestoFinalDao
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.datos.local.entidades.TipoMensaje
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.repositorios.EventoRepositorio
import com.example.myapplication.core.utilidades.ChatIdHelper
import com.example.myapplication.core.utilidades.AudioManager
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import com.example.myapplication.core.dominio.modelos.ProductoMensajeDominio
import com.example.myapplication.uishared.ui.components.chat.ItemPaginacionChat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InboxType {
    PERSONAL, EMPRESA
}

data class EstadoChatUi(
    val mensajesActuales: List<MensajeEntity> = emptyList(),
    val pagingMessages: Flow<PagingData<ItemPaginacionChat>> = emptyFlow(),
    val identidadRemota: PrestadorDominio? = null,
    val replyingToMessage: MensajeEntity? = null,
    val idChatActivo: String? = null,
    val estaCargando: Boolean = false
)

/**
 * --- VIEWMODEL DE CHAT PRESTADOR (ELITE v2026.FINAL) ---
 */
@HiltViewModel
class PrestadorChatViewModel @Inject constructor(
    private val chatRepository: ChatMotorSincRepositorio,
    private val authRepository: PrestadorAutenticacionRepositorio,
    private val motorLocal: MotorSincLocal,
    private val motorSincRemoto: com.example.myapplication.core.dominio.motores.MotorSincRemoto,
    private val budgetFinalDao: PresupuestoFinalDao,
    private val eventoRepositorio: EventoRepositorio,
    private val audioMavManager: AudioManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(EstadoChatUi())
    val uiState: StateFlow<EstadoChatUi> = _estadoUi.asStateFlow()

    private var jobIdentidadRemota: Job? = null

    val estaGrabando: StateFlow<Boolean> = audioMavManager.estaGrabando
    val tiempoGrabacion: StateFlow<Int> = audioMavManager.tiempoTranscurrido
    val estaReproduciendoAudio: StateFlow<Boolean> = audioMavManager.estaReproduciendo
    val idAudioReproduciendo: StateFlow<String?> = audioMavManager.idAudioActual
    val progresoAudio: StateFlow<Float> = audioMavManager.progresoReproduccion

    private val _imagenesArchivero = MutableStateFlow<List<MensajeEntity>?>(null)
    val imagenesArchivero = _imagenesArchivero.asStateFlow()

    private val _direccionesArchivero = MutableStateFlow<List<MensajeEntity>?>(null)
    val direccionesArchivero = _direccionesArchivero.asStateFlow()

    private val _presupuestosArchivero = MutableStateFlow<List<PresupuestoResumenDominio>?>(null)
    val presupuestosArchivero = _presupuestosArchivero.asStateFlow()

    private val _turnosArchivero = MutableStateFlow<List<com.example.myapplication.core.datos.local.entidades.EventoEntity>?>(null)
    val turnosArchivero = _turnosArchivero.asStateFlow()

    private val _visitasArchivero = MutableStateFlow<List<com.example.myapplication.core.datos.local.entidades.EventoEntity>?>(null)
    val visitasArchivero = _visitasArchivero.asStateFlow()

    private val _productosArchivero = MutableStateFlow<List<MensajeEntity>?>(null)
    val productosArchivero = _productosArchivero.asStateFlow()

    val direccionesChatFiltradas: StateFlow<List<MensajeEntity>> = _direccionesArchivero
        .map { list -> list?.filter { !it.direccionTexto.isNullOrBlank() }?.sortedByDescending { it.marcaTiempo } ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presupuestosChatOrdenados: StateFlow<List<PresupuestoResumenDominio>> = _presupuestosArchivero
        .map { list -> list?.sortedByDescending { it.fechaTimestamp } ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cargarSeccionArchivero(tipo: Int) {
        val chatId = _estadoUi.value.idChatActivo ?: return

        viewModelScope.launch {
            when (tipo) {
                0 -> {
                    if (_presupuestosArchivero.value == null) {
                        val miUid = authRepository.obtenerUsuarioActual()?.uid ?: ""
                        val receptor = _estadoUi.value.identidadRemota?.id ?: ""
                        budgetFinalDao.obtenerPresupuestosEntre(miUid, receptor)
                            .collect { lista -> 
                                _presupuestosArchivero.value = lista.map { PresupuestoMappers.aResumenDominio(it) } 
                            }
                    }
                }
                1 -> {
                    if (_imagenesArchivero.value == null) {
                        chatRepository.obtenerSoloImagenes(chatId)
                            .collect { _imagenesArchivero.value = it }
                    }
                }
                2 -> {
                    if (_direccionesArchivero.value == null) {
                        chatRepository.obtenerSoloUbicaciones(chatId)
                            .collect { _direccionesArchivero.value = it }
                    }
                }
                3 -> {
                    if (_productosArchivero.value == null) {
                        chatRepository.obtenerSoloProductos(chatId)
                            .collect { _productosArchivero.value = it }
                    }
                }
                4 -> {
                    if (_turnosArchivero.value == null) {
                        eventoRepositorio.obtenerPorChat(chatId)
                            .map { list -> list.filter { it.tipo == com.example.myapplication.core.datos.local.entidades.TipoEvento.TURNO_CITA } }
                            .collect { _turnosArchivero.value = it }
                    }
                }
                5 -> {
                    if (_visitasArchivero.value == null) {
                        eventoRepositorio.obtenerPorChat(chatId)
                            .map { list -> list.filter { it.tipo == com.example.myapplication.core.datos.local.entidades.TipoEvento.VISITA_TECNICA } }
                            .collect { _visitasArchivero.value = it }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalNoLeidos: StateFlow<Int> = authRepository.observarUsuarioActual()
        .flatMapLatest { usuario ->
            if (usuario == null) flowOf(0)
            else chatRepository.obtenerConteoNoLeidosGlobal(usuario.uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun cargarHilo(idChat: String?, idRemoto: String? = null) {
        val miUid = authRepository.obtenerUsuarioActual()?.uid ?: ""
        val idRealChat = if (idChat == null || idChat == "auto") {
            if (idRemoto != null) ChatIdHelper.generateChatId(miUid, idRemoto) else null
        } else idChat

        if (idRealChat == null) return
        
        if (_estadoUi.value.idChatActivo == idRealChat && _estadoUi.value.identidadRemota?.id == idRemoto) {
            return
        }

        _estadoUi.update { it.copy(idChatActivo = idRealChat, mensajesActuales = emptyList(), identidadRemota = null) }

        // [FIX]: este observador de identidad remota nunca se cancelaba al cambiar de chat —
        // quedaba corriendo para siempre en viewModelScope, y si el chat anterior recibía
        // cualquier actualización (ej. un mensaje nuevo) mientras el usuario ya estaba viendo
        // OTRO chat, pisaba identidadRemota con el nombre/foto del chat viejo. Los mensajes
        // mostrados sí eran los correctos porque esos se resetean en cada llamada.
        jobIdentidadRemota?.cancel()

        viewModelScope.launch {
            chatRepository.observarChat(idRealChat)

            idRemoto?.let { remoteId ->
                jobIdentidadRemota = launch {
                    motorLocal.impactarUsuarioShallow(remoteId)
                    combine(
                        chatRepository.obtenerConversaciones(miUid).map { list -> list.find { it.idChat == idRealChat } },
                        motorSincRemoto.observarPresencia(com.example.myapplication.core.dominio.motores.MotorSincRemoto.COL_CLIENTE, remoteId)
                    ) { conv, estaOnline -> conv to estaOnline }
                    .collectLatest { (conv, estaOnline) ->
                        conv?.let {
                            val uiModel = PrestadorDominio(
                                id = it.idIdentidadRemota,
                                titulo = if (it.nombreRemoto == "Usuario Maverick") "Cargando..." else it.nombreRemoto,
                                urlMiniatura = it.fotoRemotaUrl ?: it.miniaturaRemotaBase64,
                                estaOnline = estaOnline
                            )
                            _estadoUi.update { state -> state.copy(identidadRemota = uiModel) }

                            if (it.nombreRemoto == "Usuario Maverick") {
                                motorLocal.impactarUsuarioShallow(it.idIdentidadRemota)
                                motorLocal.impactarPrestadorShallow(it.idIdentidadRemota)
                            }
                        }
                    }
                }
            }

            val messagesFlow = chatRepository.obtenerFlujoMensajesPaginados(idRealChat)
                .map { pagingData ->
                    pagingData.map { mensaje ->
                        val presupuesto = if (mensaje.tipo == TipoMensaje.PRESUPUESTO) {
                            com.example.myapplication.core.utilidades.CompresorPresupuesto.descomprimir(mensaje.contenido)?.let {
                                com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers.aResumenDominio(it.cabecera)
                            }
                        } else null
                        ItemPaginacionChat.Mensaje(mensaje, presupuesto) as ItemPaginacionChat
                    }
                        .insertSeparators { m1, m2 ->
                            if (m1 is ItemPaginacionChat.Mensaje && m2 is ItemPaginacionChat.Mensaje) {
                                val fecha1 = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.getDefault()).format(java.util.Date(m1.entidad.marcaTiempo))
                                val fecha2 = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.getDefault()).format(java.util.Date(m2.entidad.marcaTiempo))
                                if (fecha1 != fecha2) ItemPaginacionChat.SeparadorFecha(fecha1) else null
                            } else if (m1 is ItemPaginacionChat.Mensaje && m2 == null) {
                                 val fecha = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.getDefault()).format(java.util.Date(m1.entidad.marcaTiempo))
                                 ItemPaginacionChat.SeparadorFecha(fecha)
                            } else null
                        }
                }.cachedIn(viewModelScope)

            _estadoUi.update { it.copy(pagingMessages = messagesFlow) }
        }
    }

    fun enviarTexto(texto: String, idEmisor: String, idReceptor: String, propietarioReceptor: String) {
        val uid = authRepository.obtenerUsuarioActual()?.uid ?: return
        val idChat = _estadoUi.value.idChatActivo ?: ChatIdHelper.generateChatId(idEmisor, idReceptor)
        val replyMessage = _estadoUi.value.replyingToMessage
        
        viewModelScope.launch {
            chatRepository.enviarMensajeTexto(
                idChat = idChat,
                emisor = idEmisor,
                receptor = idReceptor,
                propietarioEmisor = uid,
                propietarioReceptor = propietarioReceptor,
                texto = texto,
                respondidoAId = replyMessage?.id,
                respondidoAContenido = replyMessage?.contenido
            )
            _estadoUi.update { it.copy(replyingToMessage = null) }
        }
    }

    fun enviarImagen(uri: android.net.Uri, idEmisor: String, idReceptor: String) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        viewModelScope.launch {
            chatRepository.enviarMensajeImagen(idChat, idEmisor, idReceptor, uri.toString())
        }
    }

    fun enviarUbicacion(lat: Double, lng: Double, direccion: String, idEmisor: String, idReceptor: String) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        viewModelScope.launch {
            chatRepository.enviarMensajeUbicacion(idChat, idEmisor, idReceptor, lat, lng, direccion)
        }
    }

    fun enviarFinalizacionServicio(idEmisor: String, idReceptor: String, urlEvidencia: String? = null) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        viewModelScope.launch {
            chatRepository.enviarMensajeFinalizacionServicio(idChat, idEmisor, idReceptor, urlEvidencia)
        }
    }

    fun iniciarGrabacionAudio() {
        audioMavManager.iniciarGrabacion(context)
    }

    fun cancelarGrabacionAudio() {
        audioMavManager.cancelarGrabacion()
    }

    fun detenerGrabacionYEnviar(idEmisor: String, idReceptor: String) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        viewModelScope.launch {
            val archivo = audioMavManager.detenerGrabacion()
            if (archivo != null) {
                chatRepository.enviarMensajeAudio(
                    idChat = idChat,
                    emisor = idEmisor,
                    receptor = idReceptor,
                    pathLocal = archivo.absolutePath,
                    duracionMs = audioMavManager.tiempoTranscurrido.value * 1000L
                )
            }
        }
    }

    fun reproducirAudio(id: String, url: String?, pathLocal: String?) {
        audioMavManager.reproducirAudio(id, url, pathLocal)
    }

    fun enviarProductoElite(
        uiModel: ProductoMensajeDominio,
        idEmisor: String,
        idReceptor: String
    ) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        viewModelScope.launch {
            val json = org.json.JSONObject()
            json.put("id", uiModel.idProducto)
            json.put("nom", uiModel.titulo)
            json.put("pv", uiModel.precioActual)
            json.put("pa", uiModel.precioAnterior ?: 0.0)
            json.put("des", uiModel.porcentajeDescuento)
            json.put("ct", uiModel.cuotasTexto)
            json.put("eg", uiModel.envioGratis)
            json.put("img", uiModel.urlImagen)
            json.put("min", uiModel.miniaturaBase64 ?: "")
            json.put("idCat", uiModel.idCategoria)
            json.put("tip", if (uiModel.esServicio) "SERVICIO" else "PRODUCTO")
            json.put("mar", uiModel.marca)

            chatRepository.enviarMensajeProductoElite(
                idChat = idChat,
                emisor = idEmisor,
                receptor = idReceptor,
                jsonContenido = json.toString(),
                idReferencia = uiModel.idProducto,
                precio = uiModel.precioActual,
                categoria = uiModel.idCategoria,
                subtipo = if (uiModel.esServicio) "SERVICIO" else "PRODUCTO",
                miniatura = uiModel.miniaturaBase64,
                urlImg = uiModel.urlImagen
            )
        }
    }

    fun enviarPropuestaCita(
        idEmisor: String,
        idReceptor: String,
        tipo: TipoMensaje,
        fecha: String,
        hora: String,
        direccion: String,
        categoria: String? = null,
        nombreRecurso: String? = null,
        idRecurso: String? = null,
        fotoRecurso: String? = null,
        cargoRecurso: String? = null,
        idPresupuesto: String? = null
    ) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        val prefijo = if (tipo == TipoMensaje.TURNO) "TN-" else "VT-"
        val codigo = prefijo + (100000..999999).random().toString()

        viewModelScope.launch {
            chatRepository.enviarMensajeOperativo(
                idChat = idChat,
                emisor = idEmisor,
                receptor = idReceptor,
                tipo = tipo,
                fecha = fecha,
                hora = hora,
                direccion = direccion,
                categoria = categoria,
                nombreRecurso = nombreRecurso,
                idReferencia = idRecurso,
                urlFotoRecurso = fotoRecurso,
                cargoRecurso = cargoRecurso,
                idPresupuestoAsociado = idPresupuesto,
                codigoVerificacion = codigo
            )
        }
    }

    /**
     * 🔥 [SUPREME]: Envía una propuesta de agenda abierta (el cliente elige).
     */
    fun enviarPropuestaAgendaAbierta(
        idEmisor: String,
        idReceptor: String,
        tipo: TipoMensaje,
        direccion: String,
        categoria: String? = null,
        recursosIds: List<String> = emptyList(),
        nombreRecursoReferencia: String? = null
    ) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        
        viewModelScope.launch {
            val json = org.json.JSONObject()
            json.put("modo", "ABIERTO")
            json.put("dias_sugeridos", "Lunes a Viernes")
            json.put("recursos", org.json.JSONArray(recursosIds))

            chatRepository.enviarMensajeOperativo(
                idChat = idChat,
                emisor = idEmisor,
                receptor = idReceptor,
                tipo = tipo,
                fecha = "PRÓXIMOS 7 DÍAS",
                hora = "A ELECCIÓN",
                direccion = direccion,
                categoria = categoria,
                nombreRecurso = nombreRecursoReferencia ?: "Múltiples opciones",
                subtipoOperativo = "AGENDA_ABIERTA",
                contenidoOverride = json.toString()
            )
        }
    }

    fun responderACita(mensajeId: String, aceptada: Boolean) {
        val idChat = _estadoUi.value.idChatActivo ?: return
        viewModelScope.launch {
            chatRepository.responderACita(idChat, mensajeId, aceptada)
        }
    }

    fun setReplyMessage(message: MensajeEntity?) {
        _estadoUi.update { it.copy(replyingToMessage = message) }
    }

    fun marcarLeido(idChat: String) {
        viewModelScope.launch {
            chatRepository.marcarComoLeido(idChat)
        }
    }

    suspend fun obtenerPresupuesto(id: String): Flow<com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems?> {
        return budgetFinalDao.obtenerPorId(id)
    }

    override fun onCleared() {
        super.onCleared()
        _estadoUi.value.idChatActivo?.let {
            chatRepository.detenerObservacionChat(it)
        }
    }
}
