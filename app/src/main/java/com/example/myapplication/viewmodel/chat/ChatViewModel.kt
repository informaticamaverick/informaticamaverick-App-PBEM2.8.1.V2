package com.example.myapplication.viewmodel.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.repositorios.PromocionRepositorio
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.utilidades.AudioManager
import com.example.myapplication.uishared.ui.components.chat.ItemPaginacionChat
import com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio
import com.example.myapplication.datos.repositorios.ArmadorPerfilPrestadorRepositorio
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import com.google.firebase.auth.FirebaseAuth
import android.net.Uri as AndroidUri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE CHAT (V2026.FINAL) ---
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatMotorSincRepositorio,
    private val consultasUserRepo: ConsultasUsuarioRepositorio,
    private val consultasPrestadorRepo: ArmadorPerfilPrestadorRepositorio,
    private val motorLocal: MotorSincLocal,
    private val motorSincRemoto: com.example.myapplication.core.dominio.motores.MotorSincRemoto,
    private val promotionRepository: PromocionRepositorio,
    private val budgetDao: com.example.myapplication.core.datos.local.dao.PresupuestoFinalDao,
    private val eventoRepositorio: com.example.myapplication.core.datos.repositorios.EventoRepositorio,
    private val audioManager: AudioManager,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatUiEvent>()

    val estaGrabando: StateFlow<Boolean> = audioManager.estaGrabando
    val tiempoGrabacion: StateFlow<Int> = audioManager.tiempoTranscurrido
    val idAudioReproduciendo: StateFlow<String?> = audioManager.idAudioActual
    val progresoAudio: StateFlow<Float> = audioManager.progresoReproduccion

    private val _imagenesArchivero = MutableStateFlow<List<MensajeEntity>?>(null)
    val imagenesArchivero = _imagenesArchivero.asStateFlow()

    private val _direccionesArchivero = MutableStateFlow<List<MensajeEntity>?>(null)
    val direccionesArchivero = _direccionesArchivero.asStateFlow()

    private val _turnosArchivero = MutableStateFlow<List<EventoDominio>?>(null)
    val turnosArchivero = _turnosArchivero.asStateFlow()

    private val _visitasArchivero = MutableStateFlow<List<EventoDominio>?>(null)
    val visitasArchivero = _visitasArchivero.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()

    fun buscar(query: String) { _busqueda.value = query }

    private val _presupuestosArchivero = MutableStateFlow<List<PresupuestoResumenDominio>?>(null)
    val presupuestosArchivero = _presupuestosArchivero.asStateFlow()

    private val _productosArchivero = MutableStateFlow<List<MensajeEntity>?>(null)
    val productosArchivero = _productosArchivero.asStateFlow()

    fun cargarSeccionArchivero(tipo: Int) {
        val chatId = _uiState.value.idChatActivo
        if (chatId.isEmpty()) return
        viewModelScope.launch {
            when (tipo) {
                0 -> budgetDao.obtenerPresupuestosEntre(_uiState.value.idIdentidadLocal, _uiState.value.idIdentidadRemota).collect { lista -> _presupuestosArchivero.value = lista.map { com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers.aResumenDominio(it) } }
                1 -> chatRepository.obtenerSoloImagenes(chatId).collect { _imagenesArchivero.value = it }
                2 -> chatRepository.obtenerSoloUbicaciones(chatId).collect { _direccionesArchivero.value = it }
                3 -> chatRepository.obtenerSoloProductos(chatId).collect { _productosArchivero.value = it }
                4 -> eventoRepositorio.obtenerPorChat(chatId).map { list -> list.filter { it.tipo == TipoEvento.TURNO_CITA }.map { com.example.myapplication.core.dominio.mapeadores.EventoMappers.aUiModel(it) } }.collect { _turnosArchivero.value = it }
                5 -> eventoRepositorio.obtenerPorChat(chatId).map { list -> list.filter { it.tipo == TipoEvento.VISITA_TECNICA }.map { com.example.myapplication.core.dominio.mapeadores.EventoMappers.aUiModel(it) } }.collect { _visitasArchivero.value = it }
            }
        }
    }

    private var estaInicializado = false

    fun inicializar(chatId: String, idLocal: String? = null, idRemota: String? = null, initialProvider: Any? = null, initialPromoId: String? = null) {
        if (estaInicializado && _uiState.value.idChatActivo == chatId) return
        estaInicializado = true
        val miUid = auth.currentUser?.uid ?: ""

        // [CORRECCIÓN]: Limpieza de placeholders de navegación (v2026)
        val cleanLocal = if (idLocal == "{idLocal}" || idLocal.isNullOrBlank()) "personal" else idLocal
        val cleanRemota = if (idRemota == "{idRemota}" || idRemota.isNullOrBlank()) "" else idRemota

        val resolvedLocal = if (cleanLocal == "personal") miUid else cleanLocal
        val resolvedRemota = cleanRemota

        val messagesFlow = chatRepository.obtenerFlujoMensajesPaginados(chatId)
            .map { pagingData ->
                pagingData.map { mensaje ->
                    val presupuesto = if (mensaje.tipo == TipoMensaje.PRESUPUESTO) {
                        com.example.myapplication.core.dominio.mapeadores.MensajeMappers.parsearPresupuestoJson(
                            mensaje.contenido,
                            mensaje.idReferencia ?: "",
                            mensaje.idEmisor,
                            mensaje.idReceptor
                        )?.let {
                            com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers.aResumenDominio(it.cabecera)
                        }
                    } else null
                    ItemPaginacionChat.Mensaje(mensaje, presupuesto) as ItemPaginacionChat
                }.insertSeparators { m1, m2 ->
                    if (m1 is ItemPaginacionChat.Mensaje && m2 is ItemPaginacionChat.Mensaje) {
                        val f1 = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.getDefault()).format(java.util.Date(m1.entidad.marcaTiempo))
                        val f2 = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.getDefault()).format(java.util.Date(m2.entidad.marcaTiempo))
                        if (f1 != f2) ItemPaginacionChat.SeparadorFecha(f1) else null
                    } else if (m1 is ItemPaginacionChat.Mensaje && m2 == null) {
                        ItemPaginacionChat.SeparadorFecha(java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.getDefault()).format(java.util.Date(m1.entidad.marcaTiempo)))
                    } else null
                }
            }.cachedIn(viewModelScope)

        _uiState.update { it.copy(
            idChatActivo = chatId, 
            idIdentidadLocal = resolvedLocal, 
            idIdentidadRemota = resolvedRemota, 
            pagingMessages = messagesFlow, 
            activeProvider = if (initialProvider is PrestadorDominio) initialProvider else null,
            isCargando = initialProvider == null
        ) }

        if (resolvedRemota.isNotEmpty() && _uiState.value.activeProvider == null) {
            viewModelScope.launch {
                motorLocal.impactarPrestadorShallow(resolvedRemota)
                combine(
                    consultasUserRepo.obtenerUsuarioCompletoFlujo(resolvedRemota),
                    consultasPrestadorRepo.obtenerPerfilPolimorficoFlujo(resolvedRemota),
                    motorSincRemoto.observarPresencia(com.example.myapplication.core.dominio.motores.MotorSincRemoto.COL_PRESTADOR, resolvedRemota)
                ) { u, p, estaOnline ->
                    // [CORRECCIÓN]: Mappers actualizados a PrestadorDominio (SSOT)
                    val prestador = if (u != null) com.example.myapplication.core.dominio.mapeadores.UsuarioMappers.deDominioAPrestadorUi(u.perfil)
                    else p
                    prestador to estaOnline
                }.collect { (ui, estaOnline) ->
                    ui?.let { prestador ->
                        _uiState.update { s -> s.copy(activeProvider = prestador, isProviderOnline = estaOnline, isCargando = false) }
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isCargando = false) }
        }
        chatRepository.observarChat(chatId)
        if (!initialPromoId.isNullOrBlank()) {
            viewModelScope.launch {
                promotionRepository.obtenerPromocionPorId(initialPromoId).collect { promo ->
                    _uiState.update { it.copy(activePromo = promo) }
                }
            }
        }
    }

    fun enviarTexto(texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            val emisor = _uiState.value.idIdentidadLocal
            val receptor = _uiState.value.idIdentidadRemota
            chatRepository.enviarMensajeTexto(_uiState.value.idChatActivo, emisor, receptor, auth.currentUser?.uid ?: emisor, receptor, texto, _uiState.value.replyingToMessage?.id, _uiState.value.replyingToMessage?.contenido)
            _uiState.update { it.copy(replyingToMessage = null) }
            _events.emit(ChatUiEvent.MessageSent)
        }
    }

    fun enviarImagen(uri: AndroidUri) {
        viewModelScope.launch { chatRepository.enviarMensajeImagen(_uiState.value.idChatActivo, _uiState.value.idIdentidadLocal, _uiState.value.idIdentidadRemota, uri.toString()) }
    }

    fun enviarUbicacion(lat: Double, lng: Double, direccion: String) {
        viewModelScope.launch { chatRepository.enviarMensajeUbicacion(_uiState.value.idChatActivo, _uiState.value.idIdentidadLocal, _uiState.value.idIdentidadRemota, lat, lng, direccion) }
    }

    fun iniciarGrabacionAudio() = audioManager.iniciarGrabacion(context)
    fun cancelarGrabacionAudio() = audioManager.cancelarGrabacion()
    fun reproducirAudio(id: String, url: String?, path: String?) = audioManager.reproducirAudio(id, url, path)
    
    fun detenerGrabacionYEnviar() {
        viewModelScope.launch {
            val file = audioManager.detenerGrabacion()
            file?.let { chatRepository.enviarMensajeAudio(_uiState.value.idChatActivo, _uiState.value.idIdentidadLocal, _uiState.value.idIdentidadRemota, it.absolutePath, audioManager.tiempoTranscurrido.value * 1000L) }
        }
    }

    fun responderACita(mensajeId: String, aceptada: Boolean) {
        val chatId = _uiState.value.idChatActivo
        if (chatId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.responderACita(chatId, mensajeId, aceptada)
            marcarComoLeido()
        }
    }

    /**
     * 🔥 [SUPREME]: El cliente confirma un horario específico tras una propuesta abierta.
     */
    fun responderACitaAbierta(
        mensajeId: String,
        fechaElegida: String,
        horaElegida: String,
        idRecursoElegido: String
    ) {
        val chatId = _uiState.value.idChatActivo
        if (chatId.isEmpty()) return
        
        viewModelScope.launch {
            val miNombre = auth.currentUser?.displayName ?: "Un cliente"
            
            chatRepository.responderACitaAbierta(
                idChat = chatId,
                mensajeId = mensajeId,
                fecha = fechaElegida,
                hora = horaElegida,
                idRecurso = idRecursoElegido,
                nombreConfirmador = miNombre
            )
            
            marcarComoLeido()
        }
    }

    fun setReplyMessage(item: ItemPaginacionChat.Mensaje?) {
        _uiState.update { it.copy(replyingToMessage = item?.entidad) }
    }

    fun solicitarCompraProducto(producto: ProductoMensajeDominio) {
        val chatId = _uiState.value.idChatActivo
        val originalMsgId = producto.idMensajeOriginal
        if (chatId.isEmpty() || originalMsgId == null) return
        viewModelScope.launch {
            val miNombre = auth.currentUser?.displayName ?: "Un cliente"
            chatRepository.solicitarPedido(chatId, originalMsgId, _uiState.value.activeProvider?.titulo ?: "el prestador", miNombre)
        }
    }

    fun marcarComoLeido() {
        viewModelScope.launch { if (_uiState.value.idChatActivo.isNotEmpty()) chatRepository.marcarComoLeido(_uiState.value.idChatActivo) }
    }

    /**
     * 🔥 [ELITE]: Elimina un mensaje localmente y deja rastro en el sistema (Tombstone).
     */
    fun eliminarMensaje(idMensaje: String) {
        val chatId = _uiState.value.idChatActivo
        val idRemoto = _uiState.value.activeProvider?.id
        if (chatId.isEmpty() || idRemoto == null) return

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            chatRepository.eliminarMensaje(idMensaje)
            chatRepository.enviarMensajeSistema(
                idChat = chatId,
                receptor = idRemoto,
                texto = "⚠️ Mensaje eliminado por el usuario"
            )
        }
    }

    /**
     * 🔥 [ELITE]: Elimina la conversación actual del dispositivo.
     */
    fun eliminarConversacion() {
        val chatId = _uiState.value.idChatActivo
        if (chatId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.eliminarConversacion(chatId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.idChatActivo.isNotEmpty()) chatRepository.detenerObservacionChat(_uiState.value.idChatActivo)
    }
}
