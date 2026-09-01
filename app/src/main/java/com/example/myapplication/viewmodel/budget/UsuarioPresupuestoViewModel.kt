package com.example.myapplication.viewmodel.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers
import com.example.myapplication.core.datos.repositorios.PresupuestoRepositorio
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * --- VIEWMODEL DE GESTIÓN DE PRESUPUESTOS (v2026.ELITE) ---
 * [PROPÓSITO]: Manejar acciones directas sobre presupuestos y el estado de la bandeja comercial (Sueltos/Directos).
 * [LEY #9]: Estándar Mav en Español.
 */
@HiltViewModel
class UsuarioPresupuestoViewModel @Inject constructor(
    private val presupuestoRepositorio: PresupuestoRepositorio,
    private val repositorioChat: ChatMotorSincRepositorio,
    private val repositorioCategoria: CategoriaRepositorio,
    private val autenticacion: FirebaseAuth,
    val coordinador: CoordinadorAcciones
) : ViewModel() {

    private val _presupuestoSeleccionado = MutableStateFlow<PresupuestoConItems?>(null)
    val presupuestoSeleccionado = _presupuestoSeleccionado.asStateFlow()

    private val _idsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val idsSeleccionados = _idsSeleccionados.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val todosLosPresupuestosResumen: StateFlow<List<PresupuestoResumenDominio>> = coordinador.idPerfilSeleccionado
        .map { id -> id ?: autenticacion.currentUser?.uid ?: "" }
        .flatMapLatest { identityId ->
            combine(
                presupuestoRepositorio.todosLosPresupuestos, 
                repositorioCategoria.todasLasCategorias
            ) { presupuestos, categorias ->
                val mapaCategorias = categorias.associateBy { it.id }
                
                presupuestos.filter { (it.idCliente == identityId || it.idPrestador == identityId) }
                    .map { p ->
                        val cat = mapaCategorias[p.idCategoria ?: ""]
                        PresupuestoMappers.aResumenDominio(p, nombreCat = cat?.nombre, iconoCat = cat?.icono)
                    }.sortedByDescending { it.fechaTimestamp }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presupuestosDirectos: StateFlow<List<PresupuestoResumenDominio>> = todosLosPresupuestosResumen
        .map { lista -> lista.filter { it.idConcurso == null } } 
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cargarDetallePresupuesto(id: String) {
        viewModelScope.launch {
            presupuestoRepositorio.obtenerPresupuestoPorId(id).collect {
                _presupuestoSeleccionado.value = it
            }
        }
    }

    fun aceptarPresupuesto(presupuesto: PresupuestoFinalEntity) {
        viewModelScope.launch {
            presupuestoRepositorio.actualizarEstadoPresupuesto(presupuesto.idPresupuesto, EstadoPresupuesto.ACEPTADO)
            
            val chatId = com.example.myapplication.core.utilidades.ChatIdHelper.generateChatId(presupuesto.idCliente, presupuesto.idPrestador)
            repositorioChat.enviarMensajeSistema(
                idChat = chatId,
                receptor = presupuesto.idPrestador,
                texto = "Presupuesto aceptado ✅",
                idReferencia = presupuesto.idPresupuesto
            )
        }
    }

    fun rechazarPresupuesto(presupuesto: PresupuestoFinalEntity) {
        viewModelScope.launch {
            presupuestoRepositorio.actualizarEstadoPresupuesto(presupuesto.idPresupuesto, EstadoPresupuesto.RECHAZADO)
            
            val chatId = com.example.myapplication.core.utilidades.ChatIdHelper.generateChatId(presupuesto.idCliente, presupuesto.idPrestador)
            repositorioChat.enviarMensajeSistema(
                idChat = chatId,
                receptor = presupuesto.idPrestador,
                texto = "Presupuesto rechazado ❌",
                idReferencia = presupuesto.idPresupuesto
            )
        }
    }

    fun actualizarMultiseleccion(activa: Boolean) {
        coordinador.actualizarMultiseleccion(activa)
        if (!activa) _idsSeleccionados.value = emptySet()
    }

    fun alternarSeleccion(id: String) {
        val actual = _idsSeleccionados.value.toMutableSet()
        if (!actual.remove(id)) actual.add(id)
        _idsSeleccionados.value = actual
    }

    fun seleccionarTodo(todosLosIds: List<String>) {
        if (_idsSeleccionados.value.size == todosLosIds.size) _idsSeleccionados.value = emptySet()
        else _idsSeleccionados.value = todosLosIds.toSet()
    }
}
