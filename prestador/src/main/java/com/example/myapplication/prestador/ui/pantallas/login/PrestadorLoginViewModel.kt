package com.example.myapplication.prestador.ui.pantallas.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.repositorios.SincronizadorRemotoPrestador
import com.example.myapplication.prestador.datos.repositorios.PerfilPrestadorDeepRepositorio
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * --- ESTADO DEL PROCESO DE LOGIN (UI) ---
 */
sealed class EstadoLogin {
    object Inactivo : EstadoLogin()
    object Cargando : EstadoLogin()
    object Exito : EstadoLogin()
    data class Error(val mensaje: String) : EstadoLogin()
    data class Suspendido(val motivo: String?) : EstadoLogin()
}

/**
 * --- VIEWMODEL DE LOGIN PRESTADOR (PRE) ---
 * 
 * [PROPÓSITO]: Gestionar el flujo de autenticación y asegurar que los datos 
 * del profesional estén sincronizados localmente antes de entrar al Dashboard.
 */
@HiltViewModel
class PrestadorLoginViewModel @Inject constructor(
    private val authRepository: PrestadorAutenticacionRepositorio,
    private val repoDeep: PerfilPrestadorDeepRepositorio,
    private val repoRemoto: SincronizadorRemotoPrestador,
    private val prestadorDao: com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao,
    private val chatRepository: ChatMotorSincRepositorio
) : ViewModel() {

    private val _estadoLogin = MutableStateFlow<EstadoLogin>(EstadoLogin.Inactivo)
    val estadoLogin: StateFlow<EstadoLogin> = _estadoLogin.asStateFlow()

    private val _tienePerfil = MutableStateFlow(false)
    val tienePerfil: StateFlow<Boolean> = _tienePerfil.asStateFlow()

    private val _correoRecuperacionEnviado = MutableStateFlow(false)
    val correoRecuperacionEnviado: StateFlow<Boolean> = _correoRecuperacionEnviado.asStateFlow()

    // --- SECTOR: ACCIONES ---

    fun iniciarSesion(correo: String, clave: String) {
        viewModelScope.launch {
            _estadoLogin.value = EstadoLogin.Cargando
            authRepository.iniciarSesionConEmailClave(correo, clave)
                .onSuccess { usuario -> 
                    finalizarAccesoElite(usuario) 
                }
                .onFailure { _estadoLogin.value = EstadoLogin.Error(it.message ?: "Error de acceso") }
        }
    }

    fun handleGoogleSignInResult(tokenIdentidad: String) {
        viewModelScope.launch {
            android.util.Log.d("PrestadorLogin", "🔑 [GOOGLE_TOKEN_RECEIVED] Procesando token...")
            _estadoLogin.value = EstadoLogin.Cargando
            authRepository.iniciarSesionConGoogle(tokenIdentidad)
                .onSuccess { usuario -> 
                    android.util.Log.d("PrestadorLogin", "✅ [AUTH_SUCCESS] UID: ${usuario.uid}")
                    finalizarAccesoElite(usuario) 
                }
                .onFailure { 
                    android.util.Log.e("PrestadorLogin", "❌ [AUTH_ERROR] ${it.message}")
                    _estadoLogin.value = EstadoLogin.Error(it.message ?: "Error Google") 
                }
        }
    }

    /**
     * 🔥 [ELITE]: Finaliza el acceso de forma OPTIMISTA.
     * [LEY #5]: Navegamos al Dashboard inmediatamente; la restauración ocurre en el repositorio.
     */
    private suspend fun finalizarAccesoElite(usuario: com.google.firebase.auth.FirebaseUser) {
        val uid = usuario.uid
        try {
            android.util.Log.d("PrestadorLogin", "🌱 [SEED_SYNC] Delegando preparación de Room para $uid...")

            // 1. Verificación de estado de cuenta (existe + baneo) ANTES de tocar Room/Dashboard.
            val estadoCuenta = withTimeoutOrNull(3000) {
                repoRemoto.verificarEstadoCuenta(uid)
            } ?: com.example.myapplication.core.datos.repositorios.EstadoCuentaPrestador(
                existe = true, baneado = false, motivoBaneo = null
            )

            if (estadoCuenta.baneado) {
                android.util.Log.w("PrestadorLogin", "🚫 [CUENTA_SUSPENDIDA] $uid: ${estadoCuenta.motivoBaneo}")
                // No cerramos sesión acá: la dejamos activa para que el diálogo de
                // cuenta suspendida pueda leer/crear el ticket de apelación en Firestore
                // (las reglas exigen sesión autenticada). Se cierra recién cuando el
                // usuario cierra el diálogo, vía cerrarSesionSuspendida().
                withContext(Dispatchers.Main) {
                    _estadoLogin.value = EstadoLogin.Suspendido(estadoCuenta.motivoBaneo)
                }
                return
            }

            // 2. Delegar preparación de Room al Repositorio Deep
            repoDeep.finalizarAcceso(usuario)

            // [ELITE]: Marca presencia en vivo de una — no hace falta esperar al
            // primer onStart de ProcessLifecycleOwner (login exitoso ya implica
            // que la app está en primer plano).
            repoRemoto.actualizarPresencia(uid, true)

            // 🐛 FIX (31/08): faltaba acá — el listener de señales de chat (inbox_signals)
            // solo se activaba en el arranque en frío (PrestadorArranqueViewModel, cuando la
            // sesión YA estaba iniciada al abrir la app). Un login recién hecho en esta misma
            // sesión nunca lo activaba, así que el prestador no se enteraba de mensajes nuevos
            // hasta reiniciar la app por completo.
            chatRepository.inicializarEcosistemaChat(uid)

            withContext(Dispatchers.Main) {
                _tienePerfil.value = estadoCuenta.existe
                _estadoLogin.value = EstadoLogin.Exito
            }

        } catch (e: Exception) {
            android.util.Log.e("PrestadorLogin", "❌ [LOGIN_ERROR] ${e.message}")
            withContext(Dispatchers.Main) {
                _tienePerfil.value = true
                _estadoLogin.value = EstadoLogin.Exito
            }
        }
    }

    fun recuperarClave(correo: String) {
        viewModelScope.launch {
            _estadoLogin.value = EstadoLogin.Cargando
            authRepository.enviarCorreoRecuperacionClave(correo)
                .onSuccess {
                    _estadoLogin.value = EstadoLogin.Inactivo
                    _correoRecuperacionEnviado.value = true
                }
                .onFailure { _estadoLogin.value = EstadoLogin.Error(it.message ?: "Error de recuperación") }
        }
    }

    // [ALIAS COMPATIBILIDAD]
    fun login(email: String, pass: String) = iniciarSesion(email, pass)
    fun resetPassword(email: String) = recuperarClave(email)
    fun signInWithGoogle() { /* Lanzado por UI */ }
    fun resetLoginState() { _estadoLogin.value = EstadoLogin.Inactivo }
    fun cerrarSesionSuspendida() { authRepository.cerrarSesion() }
    fun resetPasswordEmailSentFlag() { _correoRecuperacionEnviado.value = false }
    
    val loginState = estadoLogin
    val hasProfile = tienePerfil
    val passwordResetEmailSent = correoRecuperacionEnviado
}
















































