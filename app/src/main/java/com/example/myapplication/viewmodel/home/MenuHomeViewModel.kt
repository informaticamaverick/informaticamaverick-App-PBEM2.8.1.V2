package com.example.myapplication.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.dominio.modelos.UsuarioDominio
import com.example.myapplication.datos.repositorios.UsuarioAutenticacionRepositorio
import com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- MENU HOME VIEWMODEL (EL COORDINADOR DE OPCIONES) ---
 * [ELITE]: Gestiona la lógica del menú lateral/inferior de la Home.
 * [FIX]: Se eliminó la inyección de otros ViewModels para cumplir con las reglas de Hilt.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class MenuHomeViewModel @Inject constructor(
    private val authRepository: UsuarioAutenticacionRepositorio,
    private val consultasUserRepo: ConsultasUsuarioRepositorio,
    private val sincRepo: com.example.myapplication.datos.repositorios.SincUsuarioRepositorio,
    private val coordinador: CoordinadorAcciones,
    private val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion // 🔥 [NEW]
) : ViewModel() {

    // --- FLUJO DE DATOS DEL USUARIO (SSOT) ---
    val usuarioState: StateFlow<UsuarioDominio?> = authRepository.observarUsuarioActual()
        .flatMapLatest { usuario ->
            if (usuario == null) flowOf(null)
            else consultasUserRepo.obtenerCuentaMaestroUsuarioFlujo(usuario.uid)
        }
        .map { cuenta -> 
            cuenta?.usuario?.perfil
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val estaVisible = navCoordinador.estaMenuLateralAbierto // 🔥 [FIX]

    fun establecerVisibilidad(visible: Boolean) {
        navCoordinador.establecerEstaMenuLateralAbierto(visible) // 🔥 [FIX]
    }

    /**
     * Cierra la sesión del usuario de forma segura.
     */
    fun cerrarSesion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            android.util.Log.d("MenuHomeVM", "🚪 [LOGOUT] Cerrando sesión...")
            authRepository.obtenerUsuarioActual()?.uid?.let { sincRepo.actualizarPresencia(it, false) }
            authRepository.cerrarSesion()
            onSuccess()
        }
    }

    /**
     * Alterna entre modo cliente y modo empresa.
     */
    fun alternarModoEmpresa(idEmpresa: String?) {
        coordinador.seleccionarPerfil(idEmpresa)
    }
}


