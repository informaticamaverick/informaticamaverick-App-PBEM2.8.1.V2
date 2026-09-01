package com.example.myapplication.coordinadores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.CuentaDao
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.repositorios.PresupuestoRepositorio
import com.example.myapplication.obreros.GestorSincronizacionUsuario
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- GESTOR DE ARRANQUE (EL INICIADOR) ---
 * [LEY #9]: Estándar en Español.
 * Único responsable de verificar la sesión y decidir la ruta inicial.
 */
@HiltViewModel
class UsuarioArranqueViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val cuentaDao: CuentaDao,
    private val presupuestoRepositorio: PresupuestoRepositorio,
    private val sincRepo: com.example.myapplication.datos.repositorios.SincUsuarioRepositorio,
    private val gestorSincronizacion: GestorSincronizacionUsuario,
    private val chatRepository: ChatMotorSincRepositorio
) : ViewModel() {
    private val _rutaInicial = MutableStateFlow<String>("verificando")
    val rutaInicial: StateFlow<String> = _rutaInicial.asStateFlow()

    fun realizarVerificacionInicial() {
        android.util.Log.d("Elite", "[INICIO_VERIFICACION_ARRANQUE]")
        viewModelScope.launch {
            val usuarioActual = auth.currentUser
            if (usuarioActual == null) {
                _rutaInicial.value = "login"
            } else {
                // [ELITE]: Restauración Inteligente (Ley #5)
                val cuentaLocal = cuentaDao.obtenerPorId(usuarioActual.uid).first()
                if (cuentaLocal == null) {
                    android.util.Log.d("UsuarioArranque", "🔄 [RESTORATION] Sesión activa pero Room vacío. Restaurando...")
                    sincRepo.finalizarAcceso(usuarioActual)
                } else {
                    gestorSincronizacion.encolarSincronizacionPush(usuarioActual.uid)
                }
                
                presupuestoRepositorio.observarPresupuestosEntrantes(usuarioActual.uid)
                chatRepository.inicializarEcosistemaChat(usuarioActual.uid)
                _rutaInicial.value = "home"
            }
        }
    }
}
