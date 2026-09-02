package com.example.myapplication.prestador.viewmodel.global

import androidx.lifecycle.ViewModel
import com.example.myapplication.prestador.datos.repositorios.MonitorSesionPrestador
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * --- GUARDIÁN DE SESIÓN (v2026) ---
 * [PROPÓSITO]: Punto único, a nivel de NavGraph raíz, que expone si la cuenta
 * activa fue suspendida en vivo y ejecuta el cierre de sesión forzado.
 */
@HiltViewModel
class SesionGuardianViewModel @Inject constructor(
    private val monitor: MonitorSesionPrestador,
    private val authRepository: PrestadorAutenticacionRepositorio
) : ViewModel() {

    val cuentaSuspendida: StateFlow<String?> = monitor.cuentaSuspendida

    val matriculaVerificada: StateFlow<Boolean> = monitor.matriculaVerificada

    fun cerrarSesionPorBaneo() {
        authRepository.cerrarSesion()
        monitor.limpiarAlertaSuspension()
    }

    fun limpiarAlertaVerificacion() {
        monitor.limpiarAlertaVerificacion()
    }
}
