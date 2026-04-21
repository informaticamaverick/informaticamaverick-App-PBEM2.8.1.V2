package com.example.myapplication.prestador.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.myapplication.prestador.data.local.dao.AppointmentDao
import com.example.myapplication.prestador.data.local.dao.ClienteDao
import com.example.myapplication.prestador.data.local.dao.AvailabilityScheduleDao
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.model.OportunidadItem
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.*
import com.google.firebase.firestore.ListenerRegistration
import android.content.Context
import androidx.compose.foundation.layout.ContextualFlowRow

@HiltViewModel
class OportunidadesViewModel @Inject constructor(
    application: Application,
    private val clienteDao: ClienteDao,
    private val appointmentDao: AppointmentDao,
    private val availabilityScheduleDao: AvailabilityScheduleDao,
    private val providerDao: ProviderDao
) : AndroidViewModel(application) {
    private val _oportunidades = MutableStateFlow<List<OportunidadItem>>(emptyList())
    val oportunidades: StateFlow<List<OportunidadItem>> = _oportunidades

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mensajeAceptar = MutableStateFlow<String?>(null)
    val mensajeAceptar: StateFlow<String?> = _mensajeAceptar

    private val _clientes = MutableStateFlow<List<ClienteEntity>>(emptyList())
    val clientes: StateFlow<List<ClienteEntity>> = _clientes


    //Cola de solicitudes fast pendientes de mostrar
    private val colaSolicitudes = ArrayDeque<OportunidadItem>()
    private val _nuevaSolicitud = MutableStateFlow<OportunidadItem?>(null)
    val nuevaSolicitud: StateFlow<OportunidadItem?> = _nuevaSolicitud

    // Bloquea nuevas solicitudes mientras haya un trabajo Fast activo
    private val _hayTrabajoFastActivo = MutableStateFlow(false)
    val hayTrabajoFastActivo: StateFlow<Boolean> = _hayTrabajoFastActivo

    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var idsYaVistos = mutableSetOf<String>()
    private val _proximaCita = MutableStateFlow<com.example.myapplication.prestador.data.local.entity.AppointmentEntity?>(null)
    private val _restriccionHorario = MutableStateFlow<String?>(null)
    private val _restriccionDistacia = MutableStateFlow<String?>(null)
    private val _restriccionSolicitudActiva = MutableStateFlow<String?>(null)
    private val _restriccionCitaEnCurso = MutableStateFlow<String?>(null)

    val restriccionHorario: StateFlow<String?> = _restriccionHorario

    val resticcionSolicitudActiva: StateFlow<String?> = _restriccionSolicitudActiva

    val restriccionCitaEnCurso: StateFlow<String?> = _restriccionCitaEnCurso
    val restriccionDistancia: StateFlow<String?> = _restriccionDistacia
    val proximaCita: StateFlow<com.example.myapplication.prestador.data.local.entity.AppointmentEntity?> = _proximaCita

    companion object{
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
            listenerRegistration?.remove()
            listenerRegistration = null
            _nuevaSolicitud.value = null
            colaSolicitudes.clear()
        }
    }

    init {
        if (_conectadoFast.value)
            iniciarListenerTiempoReal()
        viewModelScope.launch {
            clienteDao.getAllClientes().collect {
                _clientes.value = it
            }
        }
        // Verificar si ya hay un trabajo FAST activo al iniciar (ej: app reiniciada)
        viewModelScope.launch {
            val fastActivo = appointmentDao.getAllAppointmentsFlow().let { flow ->
                var lista = emptyList<com.example.myapplication.prestador.data.local.entity.AppointmentEntity>()
                val job = viewModelScope.launch { flow.collect { lista = it } }
                kotlinx.coroutines.delay(300)
                job.cancel()
                lista
            }
            _hayTrabajoFastActivo.value = fastActivo.any {
                it.serviceType == "FAST" && (it.status == "confirmed" || it.status == "in_progress")
            }
        }
    }



    @SuppressLint("MissingPermission")
    private fun iniciarListenerTiempoReal() {
        listenerRegistration?.remove()
        listenerRegistration = firestore.collection("solicitudes_fast")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    if (_oportunidades.value.isEmpty()) _oportunidades.value = datosDeEjemplo()
                    return@addSnapshotListener
                }
                viewModelScope.launch {
                    val location = try { fusedLocation.lastLocation.await() } catch (e: Exception) { null }
                    val lista = snapshot.documents.mapNotNull { doc ->
                        try {
                            val item = OportunidadItem(
                                id = doc.id,
                                titulo = doc.getString("titulo") ?: "",
                                descripcion = doc.getString("descripcion") ?: "",
                                lat = doc.getDouble("lat") ?: 0.0,
                                lng = doc.getDouble("lng") ?: 0.0,
                                creadoEn = doc.getLong("creadoEn") ?: 0L,
                                urgente = doc.getBoolean("urgente") ?: false,
                                clienteId = doc.getString("clienteId") ?: "",
                                clienteNombre = doc.getString("clienteNombre") ?: "Cliente",
                                estado = "pendiente",
                                categoria = doc.getString("categoria") ?: ""
                            )
                            if (location != null) {
                                val dist = calcularDistanciaKm(location.latitude, location.longitude, item.lat, item.lng)
                                item.copy(distanciaKm = dist)
                            } else item
                        } catch (e: Exception) { null }
                    }.sortedBy { it.distanciaKm }

                    //Filtrar por categoria del prestador
                    val prestadorId = auth.currentUser?.uid
                    val provider = if (prestadorId != null) providerDao.getProviderByIdOnce(prestadorId) else null
                    val categoriasPrestador = provider?.categories?.joinToString(",") ?: ""
                    val listaFiltrada = if (categoriasPrestador.isNotBlank() && categoriasPrestador != "[]") {
                        lista.filter { item ->
                            item.categoria.isBlank() || categoriasPrestador.contains(item.categoria, ignoreCase = true)
                        }
                    } else lista

                    _oportunidades.value = if (listaFiltrada.isEmpty()) datosDeEjemplo() else listaFiltrada

                    // Expirar solicitudes viejas en Firestore (mas de 5 min)
                    expirarSolicitudesViejas(lista)

                    // Detectar solicitudes nuevas para mostrar popup
                    lista.forEach { item ->
                        if (!idsYaVistos.contains(item.id)) {
                            val ahora = System.currentTimeMillis()
                            val antiguedadMinutos = (ahora - item.creadoEn) / 1000 /60
                            if(idsYaVistos.isNotEmpty() && !_hayTrabajoFastActivo.value && antiguedadMinutos <= 5) {
                                if (colaSolicitudes.size >= 5) {
                                    colaSolicitudes.removeFirst()
                                }
                                colaSolicitudes.addLast(item)
                                // Notificar al prestador aunque la app esté en background
                                com.example.myapplication.prestador.utils.NotificationHelper(getApplication())
                                    .showSolicitudFastNotification(
                                        titulo = item.titulo,
                                        clienteNombre = item.clienteNombre,
                                        distanciaKm = item.distanciaKm
                                    )
                                if (_nuevaSolicitud.value == null) {
                                    mostrarSiguienteSolicitud()
                                }
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
        if (_hayTrabajoFastActivo.value) return // bloqueado mientras haya trabajo activo
        val siguiente = colaSolicitudes.removeFirstOrNull()
        if (siguiente != null) {
            _nuevaSolicitud.value = siguiente
            verificarConflicto()
            verificarHorario()
            verificarDistancia(siguiente.distanciaKm)
            verificarSolicitudActiva()
            verificarCitaEnCurso()
        }
    }

    private fun verificarConflicto() {
        viewModelScope.launch {
            val ahora = java.util.Calendar.getInstance()
            val hoy = "%04d-%02d-%02d".format(
                ahora.get(java.util.Calendar.YEAR),
                ahora.get(java.util.Calendar.MONTH) + 1,
                ahora.get(java.util.Calendar.DAY_OF_MONTH)
            )

            val minutosAhora =
                ahora.get(java.util.Calendar.HOUR_OF_DAY) * 60 + ahora.get(java.util.Calendar.MINUTE)
            val citas = appointmentDao.getAllAppointmentsFlow().let { flow ->
                var lista = emptyList<com.example.myapplication.prestador.data.local.entity.AppointmentEntity>()
                val job = viewModelScope.launch { flow.collect { lista = it } }
                kotlinx.coroutines.delay(200)
                job.cancel()
                lista.filter { it.date == hoy }
            }
            _proximaCita.value = citas.firstOrNull { cita ->
                if (cita.status != "pending" && cita.status != "confirmed")
                    return@firstOrNull false
                val partes = cita.time.split(":")
                val minutosCita = partes[0].toInt() * 60 + partes[1].toInt()
                val diff = minutosCita - minutosAhora
                diff in 0..120
            }
        }
    }

    private fun verificarHorario() {
        viewModelScope.launch {
            val prestadorId = auth.currentUser?.uid ?: return@launch
            val ahora = java.util.Calendar.getInstance()
            val diaSemana = when (ahora.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> 1
                java.util.Calendar.TUESDAY -> 2
                java.util.Calendar.WEDNESDAY -> 3
                java.util.Calendar.THURSDAY -> 4
                java.util.Calendar.FRIDAY -> 5
                java.util.Calendar.SATURDAY -> 6
                java.util.Calendar.SUNDAY -> 7
                else -> 1
            }
            val minutosAhora = ahora.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                ahora.get(java.util.Calendar.MINUTE)

            val doc = try {
                firestore.collection("providers").document(prestadorId).get().await()
            } catch (e: Exception) { null }
            val is24Hours = doc?.getBoolean("is24Hours") ?: false

            if (is24Hours) {
                _restriccionHorario.value = null
                return@launch
            }

            val horarios = availabilityScheduleDao.getByProviderIdAndDaySuspend(prestadorId, diaSemana)
            val dentroDeHorario = horarios.any { h ->
                val partesInicio = h.startTime.split(":")
                val partesFin = h.endTime.split(":")
                val inicio = partesInicio[0].toInt() * 60 + partesInicio[1].toInt()
                val fin = partesFin[0].toInt() * 60 + partesFin[1].toInt()
                minutosAhora in inicio..fin
            }
            _restriccionHorario.value = if (!dentroDeHorario && horarios.isNotEmpty())
                "Estás fuera de tu horario de atención"
            else null
        }
    }

    private fun
            verificarDistancia(distanciaKm: Double) {
                _restriccionDistacia.value = if (distanciaKm > DISTANCIA_MAXIMA_KM)
                    "La solicitud esta a %.1f km, superás el limite de ${DISTANCIA_MAXIMA_KM.toInt()}km".format(distanciaKm)

                else null
            }


    private fun verificarSolicitudActiva() {
        val prestadorId = auth.currentUser?.uid ?:
        return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("solicitudes_fast")
                    .whereEqualTo("estado", "aceptada")
                    .whereEqualTo("prestadorId", prestadorId)
                    .get().await()
                _restriccionSolicitudActiva.value = if (!snapshot.isEmpty)
                    "Ya tenés una solicitud fast en curso"
                else null

            } catch (e: Exception) {
                _restriccionSolicitudActiva.value = null
            }
        }
    }

    private fun verificarCitaEnCurso() {
        viewModelScope.launch {
            val ahora = java.util.Calendar.getInstance()
            val hoy = "%04d-%02d-%02d".format(ahora.get(java.util.Calendar.YEAR),
                ahora.get(java.util.Calendar.MONTH) + 1,
                ahora.get(java.util.Calendar.DAY_OF_MONTH)
            )
            val minutosAhora = ahora.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                    ahora.get(java.util.Calendar.MINUTE)
            val citas = appointmentDao.getAllAppointmentsFlow().let {
                flow ->
                var lista = emptyList<com.example.myapplication.prestador.data.local.entity.AppointmentEntity>()
                val job = viewModelScope.launch { flow.collect { lista = it } }
                kotlinx.coroutines.delay(200)
                job.cancel()
                lista.filter { it.date == hoy }
            }
            val citaEnCurso = citas.firstOrNull { cita ->
                if (cita.status != "pending" && cita.status != "confirmed") return@firstOrNull false
                val partes = cita.time.split(":")
                val inicio = partes[0].toInt() * 60 + partes[1].toInt()
                val fin = inicio + cita.duration
                minutosAhora in inicio..fin
            }
            _restriccionCitaEnCurso.value = if (citaEnCurso != null)
                "Estás en una cita ahora (${citaEnCurso.service})"
            else null
        }
    }

    private fun expirarSolicitudesViejas(lista: List<OportunidadItem>) {
        val ahora = System.currentTimeMillis()
        lista.forEach { item ->
            val antiguedadMinutos = (ahora - item.creadoEn) / 1000 / 60
            if (antiguedadMinutos > 5) {
                viewModelScope.launch {
                    try {
                        firestore.collection("solicitudes_fast")
                            .document(item.id)
                            .update("estado", "expirada")
                            .await()
                    } catch (_: Exception) { }
                }
            }
        }
    }

    fun cargarOportunidades() {
        iniciarListenerTiempoReal()
    }

    fun aceptarSolicitud(oportunidad: OportunidadItem) {
        val prestadorId = auth.currentUser?.uid ?: return
        // Bloquear INMEDIATAMENTE, antes de cualquier operación async
        // Así evitamos que aparezca otra solicitud mientras Firestore responde
        _hayTrabajoFastActivo.value = true
        colaSolicitudes.clear()
        _nuevaSolicitud.value = null

        viewModelScope.launch {
            try {
                firestore.collection("solicitudes_fast")
                    .document(oportunidad.id)
                    .update(mapOf("estado" to "aceptada", "prestadorId" to prestadorId))
                    .await()

                //Crear appointment en Room para que aparezca en "Tu proximo servicio"
                val ahora = java.util.Calendar.getInstance()
                val hoy = "%04d-%02d-%02d".format(
                    ahora.get(java.util.Calendar.YEAR),
                    ahora.get(java.util.Calendar.MONTH) + 1,
                    ahora.get(java.util.Calendar.DAY_OF_MONTH)
                )
                val hora = "%02d:%02d".format(
                    ahora.get(java.util.Calendar.HOUR_OF_DAY),
                    ahora.get(java.util.Calendar.MINUTE)
                )
                val appointment = com.example.myapplication.prestador.data.local.entity.AppointmentEntity(
                    id = "fast_${oportunidad.id}",
                    clientId = oportunidad.clienteId,
                    clientName = oportunidad.clienteNombre,
                    providerId = prestadorId,
                    service = oportunidad.titulo,
                    date = hoy,
                    time = hora,
                    duration = 60,
                    status = "confirmed",
                    notes = oportunidad.descripcion,
                    serviceType = "FAST"
                )
                appointmentDao.insertAppointment(appointment)
                _mensajeAceptar.value = "⚡ ¡Trabajo Fast aceptado! Revisá tu próximo servicio."
                cargarOportunidades()
            } catch (e: Exception) {
                // Si falla, revertir el bloqueo para no quedar bloqueado permanentemente
                _hayTrabajoFastActivo.value = false
                _mensajeAceptar.value = "Error al aceptar la solicitud"
            }
        }
    }

    fun completarTrabajoFast(appointmentId: String) {
        viewModelScope.launch {
            try {
                // Actualizar estado en Room
                val appointments = appointmentDao.getAllAppointmentsFlow().let { flow ->
                    var lista = emptyList<com.example.myapplication.prestador.data.local.entity.AppointmentEntity>()
                    val job = viewModelScope.launch { flow.collect { lista = it } }
                    kotlinx.coroutines.delay(200)
                    job.cancel()
                    lista
                }
                val appointment = appointments.firstOrNull { it.id == appointmentId }
                if (appointment != null) {
                    appointmentDao.insertAppointment(appointment.copy(status = "completed"))
                }
                // Actualizar estado en Firestore
                val firestoreId = appointmentId.removePrefix("fast_")
                firestore.collection("solicitudes_fast")
                    .document(firestoreId)
                    .update("estado", "completada")
                    .await()
            } catch (_: Exception) { }

            // Desbloquear: volver a recibir solicitudes fast
            _hayTrabajoFastActivo.value = false
            _mensajeAceptar.value = "✅ ¡Trabajo Fast completado!"
        }
    }

    fun limpiarMensaje() {
        _mensajeAceptar.value = null
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    @SuppressLint("MissingPermission")
    fun crearSolicitudFast(cliente: ClienteEntity, titulo: String, urgente: Boolean) {
        viewModelScope.launch {
            try {
                val location = fusedLocation.lastLocation.await()
                firestore.collection("solicitudes_fast").add(
                    mapOf(
                        "titulo" to titulo,
                        "descripcion" to titulo,
                        "clienteNombre" to cliente.nombre,
                        "clienteId" to cliente.id,
                        "lat" to (location?.latitude ?: -26.82),
                        "lng" to (location?.longitude ?: -65.21),
                        "urgente" to urgente,
                        "estado" to "pendiente",
                        "creadoEn" to System.currentTimeMillis()
                    )
                ).await()
                _mensajeAceptar.value = "✅ Solicitud creada para ${cliente.nombre}"
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

    private fun datosDeEjemplo(): List<OportunidadItem> = listOf(
        OportunidadItem(
            id = "demo1",
            titulo = "Reparación eléctrica",
            descripcion = "Falla en tablero eléctrico",
            clienteNombre = "Carlos Méndez",
            urgente = true,
            distanciaKm = 0.8
        ),
        OportunidadItem(
            id = "demo2",
            titulo = "Plomería urgente",
            descripcion = "Pérdida de agua en cocina",
            clienteNombre = "Ana García",
            urgente = false,
            distanciaKm = 1.5
        ),
        OportunidadItem(
            id = "demo3",
            titulo = "Instalación de AC",
            descripcion = "El equipo no enfría",
            clienteNombre = "Roberto Sosa",
            urgente = false,
            distanciaKm = 2.2
        )
    )
}
