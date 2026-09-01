package com.example.myapplication.core.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.ChatDao
import com.example.myapplication.core.datos.local.dao.EventoDao
import com.example.myapplication.core.datos.local.dao.PresupuestoFinalDao
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.EventoDominio
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.dominio.mapeadores.EventoMappers
import com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * --- VIEWMODEL DEL ARCHIVERO (v2026.ELITE) ---
 * PROPÓSITO: Gestionar la búsqueda y filtrado de archivos históricos del chat.
 * LEY #9: Nomenclatura en español.
 */
@HiltViewModel
class ArchiveroViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val presupuestoDao: PresupuestoFinalDao,
    private val eventoDao: EventoDao
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private var idChatActivo: String = ""
    private var idLocal: String = ""
    private var idRemoto: String = ""

    /**
     * Inicializa el archivero para un contexto de conversación específico.
     */
    fun inicializar(idChat: String, idLocal: String, idRemoto: String) {
        this.idChatActivo = idChat
        this.idLocal = idLocal
        this.idRemoto = idRemoto
    }

    /**
     * Actualiza el término de búsqueda.
     */
    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    /**
     * Obtiene presupuestos históricos intercambiados entre ambos participantes.
     */
    fun obtenerPresupuestos(): Flow<List<PresupuestoResumenDominio>> {
        return presupuestoDao.obtenerPresupuestosEntre(idLocal, idRemoto)
            .combine(_busqueda) { lista: List<PresupuestoFinalEntity>, query: String ->
                val mapeados = lista.map { PresupuestoMappers.aResumenDominio(it) }
                if (query.isBlank()) mapeados
                else mapeados.filter { 
                    it.tituloTrabajo?.contains(query, ignoreCase = true) == true ||
                    it.idPresupuesto.contains(query, ignoreCase = true)
                }
            }
    }

    /**
     * Obtiene imágenes enviadas en este chat específico.
     */
    fun obtenerImagenes(): Flow<List<MensajeEntity>> {
        return chatDao.obtenerMensajesPorTipo(idChatActivo, TipoMensaje.IMAGEN)
    }

    /**
     * Obtiene productos enviados en el chat.
     */
    fun obtenerProductos(): Flow<List<MensajeEntity>> {
        return chatDao.obtenerMensajesPorTipo(idChatActivo, TipoMensaje.PRODUCTO)
            .combine(_busqueda) { lista, query ->
                if (query.isBlank()) lista
                else lista.filter { it.contenido.contains(query, ignoreCase = true) }
            }
    }

    /**
     * Obtiene ubicaciones enviadas en el chat.
     */
    fun obtenerUbicaciones(): Flow<List<MensajeEntity>> {
        return chatDao.obtenerMensajesPorTipo(idChatActivo, TipoMensaje.UBICACION)
            .combine(_busqueda) { lista, query ->
                if (query.isBlank()) lista
                else lista.filter { it.direccionTexto?.contains(query, ignoreCase = true) == true }
            }
    }

    /**
     * Obtiene eventos (Turnos y Visitas) vinculados al chat.
     */
    fun obtenerEventos(): Flow<List<EventoDominio>> {
        return eventoDao.obtenerPorChat(idChatActivo)
            .combine(_busqueda) { lista: List<EventoEntity>, query: String ->
                val mapeados = lista.map { EventoMappers.aUiModel(it) }
                if (query.isBlank()) mapeados
                else mapeados.filter { 
                    it.titulo.contains(query, ignoreCase = true) ||
                    it.descripcion.contains(query, ignoreCase = true)
                }
            }
    }

    /**
     * Obtiene específicamente Turnos en Local vinculados al chat.
     */
    fun obtenerTurnos(): Flow<List<EventoDominio>> {
        return obtenerEventos().map { lista ->
            lista.filter { it.tipo == TipoEvento.TURNO_CITA }
        }
    }

    /**
     * Obtiene específicamente Visitas Técnicas vinculadas al chat.
     */
    fun obtenerVisitas(): Flow<List<EventoDominio>> {
        return obtenerEventos().map { lista ->
            lista.filter { it.tipo == TipoEvento.VISITA_TECNICA }
        }
    }
}




