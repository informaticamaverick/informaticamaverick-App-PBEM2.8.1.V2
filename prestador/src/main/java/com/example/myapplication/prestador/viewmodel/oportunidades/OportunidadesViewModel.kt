
/**

package com.example.myapplication.prestador.viewmodel.oportunidades

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.dao.AvailabilityScheduleDao
import com.example.myapplication.core.data.local.dao.UserDao
import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.prestador.data.model.OportunidadItem
import com.example.myapplication.prestador.data.repository.OportunidadesRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class OportunidadesViewModel @Inject constructor(
    application: Application,
    private val userDao: UserDao,
    private val availabilityScheduleDao: AvailabilityScheduleDao,
    private val providerDao: ProviderDao,
    private val oportunidadesRepository: OportunidadesRepository
) : AndroidViewModel(application) {

    private val _oportunidades = MutableStateFlow<List<OportunidadItem>>(emptyList())
    val oportunidades: StateFlow<List<OportunidadItem>> = _oportunidades

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mensajeAceptar = MutableStateFlow<String?>(null)
    val mensajeAceptar: StateFlow<String?> = _mensajeAceptar

    private val _clientes = MutableStateFlow<List<User>>(emptyList())
    val clientes: StateFlow<List<User>> = _clientes

    private val colaSolicitudes = ArrayDeque<OportunidadItem>()
    private val _nuevaSolicitud = MutableStateFlow<OportunidadItem?>(null)
    val nuevaSolicitud: StateFlow<OportunidadItem?> = _nuevaSolicitud

    private val _hayTrabajoFastActivo = MutableStateFlow(false)
    val hayTrabajoFastActivo: StateFlow<Boolean> = _hayTrabajoFastActivo

    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)
    private val auth = FirebaseAuth.getInstance()
    private var idsYaVistos = mutableSetOf<String>()

    private val _restriccionHorario = MutableStateFlow<String?>(null)
    private val _restriccionDistacia = MutableStateFlow<String?>(null)
    private val _restriccionSolicitudActiva = MutableStateFlow<String?>(null)
    private val _restriccionCitaEnCurso = MutableStateFlow<String?>(null)

    val restriccionHorario: StateFlow<String?> = _restriccionHorario
    val resticcionSolicitudActiva: StateFlow<String?> = _restriccionSolicitudActiva
    val restriccionCitaEnCurso: StateFlow<String?> = _restriccionCitaEnCurso
    val restriccionDistancia: StateFlow<String?> = _restriccionDistacia

    companion object {
        const val DISTANCIA_MAXIMA_KM = 20.0
    }

    private val prefs = application.getSharedPreferences("fast_prefs", Context.MODE_PRIVATE)
    private fun fastKey() = "conectado_fast_${auth.currentUser?.uid ?: "default"}"
    private val _conectadoFast = MutableStateFlow(prefs.getBoolean("conectado_fast", true))
    val conectadoFast: StateFlow<Boolean> = _conectadoFast

    fun toggleConexionFast() {
        val nuevoEstado = !_conectadoFast.value
        _conectadoFast.value = nuevoEstado
        prefs.edit().putBoolean(fastKey(), nuevoEstado).apply()
        if (nuevoEstado) {
            iniciarListenerTiempoReal()
        } else {
            _nuevaSolicitud.value = null
            colaSolicitudes.clear()
        }
    }

    init {
        if (_conectadoFast.value) iniciarListenerTiempoReal()
        viewModelScope.launch {
            userDao.getAllUsers().collect { entities -> 
                _clientes.value = entities.map { it.toDomain() } 
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun iniciarListenerTiempoReal() {
        viewModelScope.launch {
            oportunidadesRepository.escucharSolicitudesPendientes().collect { solicitudes ->
                val location = try { fusedLocation.lastLocation.await() } catch (e: Exception) { null }

                val lista = solicitudes.map { s ->
                    val item = OportunidadItem(
                        id = s.id, titulo = s.titulo, descripcion = s.descripcion,
                        lat = s.lat, lng = s.lng, creadoEn = s.creadoEn,
                        urgente = s.urgente, clienteId = s.clienteId,
                        clienteNombre = s.clienteNombre, estado = s.estado,
                        categoria = s.categoria
                    )
                    if (location != null) {
                        val dist = calcularDistanciaKm(location.latitude, location.longitude, s.lat, s.lng)
                        item.copy(distanciaKm = dist)
                    } else item
                }.sortedBy { it.distanciaKm }

                // Filtrar por categoria del prestador
                val prestadorId = auth.currentUser?.uid
                val provider = if (prestadorId != null) providerDao.getProviderByIdOnce(prestadorId) else null
                val categoriasPrestador = provider?.categories?.joinToString(",") ?: ""
                val listaFiltrada = if (categoriasPrestador.isNotBlank() && categoriasPrestador != "[]") {
                    lista.filter { it.categoria.isBlank() || categoriasPrestador.contains(it.categoria, ignoreCase = true) }
                } else lista

                // Sin fallback hardcodeado: Firebase es la unica fuente de verdad
                _oportunidades.value = listaFiltrada

                // Expirar solicitudes viejas
                expirarSolicitudesViejas(lista)

                // Detectar solicitudes nuevas para popup
                lista.forEach { item ->
                    if (!idsYaVistos.contains(item.id)) {
                        val antiguedadMinutos = (System.currentTimeMillis() - item.creadoEn) / 1000 / 60
                        if (idsYaVistos.isNotEmpty() && !_hayTrabajoFastActivo.value && antiguedadMinutos <= 5) {
                            if (colaSolicitudes.size >= 5) colaSolicitudes.removeFirst()
                            colaSolicitudes.addLast(item)
                            com.example.myapplication.prestador.utils.NotificationHelper(getApplication())
                                .showSolicitudFastNotification(
                                    titulo = item.titulo,
                                    clienteNombre = item.clienteNombre,
                                    distanciaKm = item.distanciaKm
                                )
                            if (_nuevaSolicitud.value == null) mostrarSiguienteSolicitud()
                        }
                        idsYaVistos.add(item.id)
                    }
                }
            }
        }
    }

    fun descartarNuevaSolicitud() {
        _nuevaSolicitud.value = null
        mostrarSiguienteSolicitud()
    }

    private fun mostrarSiguienteSolicitud() {
        if (_hayTrabajoFastActivo.value) return
        val siguiente = colaSolicitudes.removeFirstOrNull() ?: return
        _nuevaSolicitud.value = siguiente
        verificarConflicto()
        verificarHorario()
        verificarDistancia(siguiente.distanciaKm)
        verificarSolicitudActiva()
        verificarCitaEnCurso()
    }

    private fun verificarConflicto() { /* Sin cambios */ }

    private fun verificarHorario() {
        viewModelScope.launch {
            val prestadorId = auth.currentUser?.uid ?: return@launch
            val ahora = java.util.Calendar.getInstance()
            val diaSemana = when (ahora.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> 1; java.util.Calendar.TUESDAY -> 2
                java.util.Calendar.WEDNESDAY -> 3; java.util.Calendar.THURSDAY -> 4
                java.util.Calendar.FRIDAY -> 5; java.util.Calendar.SATURDAY -> 6
                java.util.Calendar.SUNDAY -> 7; else -> 1
            }
            val minutosAhora = ahora.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                    ahora.get(java.util.Calendar.MINUTE)

            val is24Hours = oportunidadesRepository.getProviderIs24Hours(prestadorId)
            if (is24Hours) { _restriccionHorario.value = null; return@launch }

            val horarios = availabilityScheduleDao.getByProviderIdAndDaySuspend(prestadorId, diaSemana)
            val dentroDeHorario = horarios.any { h ->
                val inicio = h.startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                val fin = h.endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                minutosAhora in inicio..fin
            }
            _restriccionHorario.value = if (!dentroDeHorario && horarios.isNotEmpty())
                "Estas fuera de tu horario de atencion" else null
        }
    }

    private fun verificarDistancia(distanciaKm: Double) {
        _restriccionDistacia.value = if (distanciaKm > DISTANCIA_MAXIMA_KM)
            "La solicitud esta a %.1f km, superas el limite de ${DISTANCIA_MAXIMA_KM.toInt()}km".format(distanciaKm)
        else null
    }

    private fun verificarSolicitudActiva() {
        val prestadorId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val activa = oportunidadesRepository.tieneSolicitudActiva(prestadorId)
            _restriccionSolicitudActiva.value = if (activa) "Ya tenes una solicitud fast en curso" else null
        }
    }

    private fun verificarCitaEnCurso() { _restriccionCitaEnCurso.value = null }

    private fun expirarSolicitudesViejas(lista: List<OportunidadItem>) {
        val ahora = System.currentTimeMillis()
        lista.forEach { item ->
            if ((ahora - item.creadoEn) / 1000 / 60 > 5) {
                viewModelScope.launch { oportunidadesRepository.expirarSolicitud(item.id) }
            }
        }
    }

    fun cargarOportunidades() { iniciarListenerTiempoReal() }

    fun aceptarSolicitud(oportunidad: OportunidadItem) {
        val prestadorId = auth.currentUser?.uid ?: return
        _hayTrabajoFastActivo.value = true
        colaSolicitudes.clear()
        _nuevaSolicitud.value = null
        viewModelScope.launch {
            try {
                oportunidadesRepository.aceptarSolicitud(oportunidad.id, prestadorId)
                _mensajeAceptar.value = "Trabajo Fast aceptado! Revisa tu proximo servicio."
                cargarOportunidades()
            } catch (e: Exception) {
                _hayTrabajoFastActivo.value = false
                _mensajeAceptar.value = "Error al aceptar la solicitud"
            }
        }
    }

    fun completarTrabajoFast(appointmentId: String) {
        viewModelScope.launch {
            try {
                val firestoreId = appointmentId.removePrefix("fast_")
                oportunidadesRepository.completarSolicitud(firestoreId)
            } catch (_: Exception) { }
            _hayTrabajoFastActivo.value = false
            _mensajeAceptar.value = "Trabajo Fast completado!"
        }
    }

    fun limpiarMensaje() { _mensajeAceptar.value = null }

    @SuppressLint("MissingPermission")
    fun crearSolicitudFast(cliente: User, titulo: String, urgente: Boolean) {
        viewModelScope.launch {
            try {
                val location = fusedLocation.lastLocation.await()
                oportunidadesRepository.crearSolicitud(
                    titulo = titulo,
                    clienteNombre = cliente.fullName,
                    clienteId = cliente.uid,
                    lat = location?.latitude ?: -26.82,
                    lng = location?.longitude ?: -65.21,
                    urgente = urgente
                )
                _mensajeAceptar.value = "Solicitud creada para ${cliente.fullName}"
                cargarOportunidades()
            } catch (e: Exception) {
                _mensajeAceptar.value = "Error: ${e.message}"
            }
        }
    }

    private fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
 */