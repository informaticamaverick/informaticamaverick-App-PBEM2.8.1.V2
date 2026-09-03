package com.example.myapplication.prestador.coordinadores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.CuentaDao
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.prestador.datos.repositorios.PerfilPrestadorDeepRepositorio
import com.example.myapplication.prestador.obreros.GestorSincronizacionPrestador
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- GESTOR DE ARRANQUE PRESTADOR (EL INICIADOR PRE) ---
 * [LEY #9]: Estándar Mav en Español.
 * Único responsable de verificar la sesión y decidir la ruta inicial del profesional.
 */
@HiltViewModel
class PrestadorArranqueViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val cuentaDao: CuentaDao,
    private val repoDeep: PerfilPrestadorDeepRepositorio,
    private val gestorSincronizacion: GestorSincronizacionPrestador,
    private val chatRepository: ChatMotorSincRepositorio
) : ViewModel() {
    private val _rutaInicial = MutableStateFlow<String>("verificando")
    val rutaInicial: StateFlow<String> = _rutaInicial.asStateFlow()

    fun realizarVerificacionInicial() {
        android.util.Log.d("AppElite", "[INICIO_VERIFICACION_ARRANQUE_PRESTADOR]")
        viewModelScope.launch {
            val usuarioActual = auth.currentUser
            if (usuarioActual == null) {
                _rutaInicial.value = "login"
            } else {
                // [ELITE]: Restauración Inteligente (Ley #5)
                val cuentaLocal = cuentaDao.obtenerPorId(usuarioActual.uid)
                if (cuentaLocal == null) {
                    android.util.Log.d("PrestadorArranque", "🔄 [RESTORATION] Sesión detectada pero Room vacío. Preparando ecosistema...")
                    repoDeep.finalizarAcceso(usuarioActual)
                } else {
                    // Si ya existe la cuenta, solo encolamos sincronización de cambios
                    gestorSincronizacion.encolarSincronizacionPush(usuarioActual.uid)
                }
                
                chatRepository.inicializarEcosistemaChat(usuarioActual.uid)
                // [FIX]: inicializarEcosistemaChat solo escucha señales para el uid personal.
                // Los mensajes dirigidos a una sucursal (idIdentidad = sucursal.id) nunca
                // disparaban nada acá porque nadie se suscribía a su buzón — un cliente podía
                // escribirle a la empresa y el prestador nunca se enteraba. Se registra cada
                // sucursal existente al arrancar, igual que ya hacía (sin usarse) ArmadorPrestadorViewModel.
                repoDeep.obtenerPerfilDeepFlujo(usuarioActual.uid).firstOrNull()?.empresas?.forEach { emp ->
                    emp.sucursales.forEach { suc ->
                        chatRepository.agregarIdentidadASincronizacion(suc.sucursal.id)
                    }
                }
                _rutaInicial.value = "home"
            }
        }
    }
}
