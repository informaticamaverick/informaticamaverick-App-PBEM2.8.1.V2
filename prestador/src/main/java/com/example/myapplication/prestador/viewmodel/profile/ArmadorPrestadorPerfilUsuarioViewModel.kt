package com.example.myapplication.prestador.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.IdentidadUsuarioDao
import com.example.myapplication.core.datos.local.dao.CuentaDao
import com.example.myapplication.core.datos.local.dao.DireccionDao
import com.example.myapplication.core.dominio.mapeadores.UsuarioMappers
import com.example.myapplication.core.dominio.modelos.UsuarioDominio
import com.example.myapplication.core.dominio.modelos.UsuarioDominioCompleto
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ARMADOR DE PERFIL DE USUARIO PARA EL PRESTADOR (PRO) ---
 * [PROPÓSITO]: Orquestar la visualización de perfiles de clientes para el prestador.
 * [LEY #9]: Estándar Maverick. Especialista en la identidad del Cliente desde la App Naranja.
 */
@HiltViewModel
class ArmadorPrestadorPerfilUsuarioViewModel @Inject constructor(
    private val usuarioDao: IdentidadUsuarioDao,
    private val direccionDao: DireccionDao,
    private val cuentaDao: CuentaDao,
    private val motorLocal: MotorSincLocal
) : ViewModel() {

    private val _uidCliente = MutableStateFlow<String?>(null)

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    /**
     * Flujo reactivo del perfil del cliente (SSOT Local-First).
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val perfilUsuario: StateFlow<UsuarioDominio?> = _uidCliente
        .filterNotNull()
        .flatMapLatest { uid ->
            combine(
                usuarioDao.obtenerPorId(uid),
                direccionDao.obtenerPorPropietario(uid),
                cuentaDao.obtenerPorId(uid)
            ) { perfil, direcciones, cuenta ->
                if (perfil == null) null
                else UsuarioMappers.deEntidadAModeloUi(perfil, cuenta)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * 🔥 [ELITE]: Carga el perfil del cliente disparando la descarga profunda.
     */
    fun cargarPerfil(uid: String) {
        if (_uidCliente.value == uid) return
        _uidCliente.value = uid

        viewModelScope.launch {
            _estaCargando.value = true
            motorLocal.impactarUsuarioDeep(uid)
            _estaCargando.value = false
        }
    }
}

















































