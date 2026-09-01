package com.example.myapplication.prestador.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import com.example.myapplication.prestador.datos.repositorios.ConsultasPrestadorRepositorio
import com.example.myapplication.prestador.datos.repositorios.PrestadorPresupuestoRepositorio
import com.example.myapplication.prestador.datos.repositorios.PrestadorCalendarioRepositorio
import com.example.myapplication.core.utilidades.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ESTADO DEL PANEL DE CONTROL (UI) ---
 */
data class EstadoDashboardUi(
    val estaCargando: Boolean = true,
    val nombreVisible: String = "",
    val photoUrl: Any? = null,
    val esVerificado: Boolean = false,
    val profileCompletion: Float = 0.8f,
    val totalPresupuestos: Int = 0,
    val totalTurnos: Int = 0,
    val gananciaRealMensual: Double = 0.0, // 🔥 [SUPREME] Be-Profit
    val totalAceptadoMensual: Double = 0.0,
    val serviceType: String = "PRESTADOR",
    val saludo: String = "¡Hola!",
    val eventosProximos: List<EventoEntity> = emptyList(),
    val concursosActivos: List<ConcursoPublicoEntity> = emptyList()
) {
    val isLoading: Boolean get() = estaCargando
    val nombrePrestador: String get() = nombreVisible
}

/**
 * --- VIEWMODEL DASHBOARD PRESTADOR (ELITE v2026.FINAL) ---
 * 
 * [PROPÓSITO]: Orquestar la Fuente Única de Verdad (SSOT) para la pantalla de inicio 
 * del profesional, gestionando estadísticas, eventos y soberanía de identidad.
 * [LEY #9]: Estándar Mav en Español.
 */
@HiltViewModel
class PrestadorDashboardViewModel @Inject constructor(
    private val authRepository: PrestadorAutenticacionRepositorio,
    private val consultasRepo: ConsultasPrestadorRepositorio,
    private val presupuestoRepositorio: PrestadorPresupuestoRepositorio,
    private val calendarioRepository: PrestadorCalendarioRepositorio
) : ViewModel() {

    // --- SECTOR: ESTADO REACTIVO (SSOT) ---

    @OptIn(ExperimentalCoroutinesApi::class)
    val estadoUi: StateFlow<EstadoDashboardUi> = authRepository.observarUsuarioActual()
        .flatMapLatest { usuarioFirebase ->
            if (usuarioFirebase == null) flowOf(EstadoDashboardUi(estaCargando = false))
            else {
                combine(
                    consultasRepo.obtenerPerfilPrestadorDeepFlujo(usuarioFirebase.uid),
                    presupuestoRepositorio.todasLasLicitaciones,
                    presupuestoRepositorio.todosLosPresupuestos,
                    presupuestoRepositorio.todosLosPresupuestosFinales // 🔥 [SUPREME]
                ) { maestro, concursos, presupuestosCocina, presupuestosFinales ->
                    maestro?.let {
                        val aceptados = presupuestosFinales.filter { it.estado == com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto.ACEPTADO }
                        
                        // 🔥 [BE-PROFIT]: Rentabilidad Real (Snapshot local privado)
                        val ingresos = aceptados.sumOf { it.totalGeneral }
                        val costos = aceptados.sumOf { it.totalCostoGeral }

                        val nombre = if (it.cuenta.priorizarEmpresa && it.cuenta.idPerfilActivo != null) {
                            val idActivo = it.cuenta.idPerfilActivo
                            val empresa = it.empresas.find { e -> e.empresa.id == idActivo }?.empresa
                            if (empresa != null) empresa.nombre
                            else {
                                it.empresas.flatMap { e -> e.sucursales }
                                    .find { s -> s.sucursal.id == idActivo }?.sucursal?.nombre ?: "Sin nombre"
                            }
                        } else it.prestador.perfil.titulo

                        val fotoRaw = if (it.cuenta.priorizarEmpresa && it.cuenta.idPerfilActivo != null) {
                            val idActivo = it.cuenta.idPerfilActivo
                            val emp = it.empresas.find { e -> e.empresa.id == idActivo }?.empresa
                            if (emp != null) {
                                emp.urlMiniatura ?: emp.urlFoto
                            } else {
                                val empDuenia = it.empresas.find { e -> e.sucursales.any { s -> s.sucursal.id == idActivo } }?.empresa
                                empDuenia?.urlMiniatura ?: empDuenia?.urlFoto
                            }
                        } else {
                            it.prestador.perfil.urlMiniatura ?: it.prestador.perfil.urlFoto
                        }

                        EstadoDashboardUi(
                            estaCargando = false,
                            nombreVisible = nombre,
                            photoUrl = ImageUtils.processImageSource(fotoRaw),
                            esVerificado = it.prestador.perfil.estaVerificado,
                            totalPresupuestos = presupuestosCocina.size,
                            totalAceptadoMensual = ingresos,
                            gananciaRealMensual = ingresos - costos,
                            saludo = obtenerSaludo(),
                            concursosActivos = concursos.take(5)
                        )
                    } ?: EstadoDashboardUi(estaCargando = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EstadoDashboardUi())

    val uiState = estadoUi

    // --- SECTOR: ACCIONES ---

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.cerrarSesion()
        }
    }

    // --- UTILIDADES ---

    private fun obtenerSaludo(): String {
        val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hora) {
            in 6..12 -> "¡Buen día!"
            in 13..19 -> "¡Buenas tardes!"
            else -> "¡Buenas noches!"
        }
    }
}
















































