package com.example.myapplication.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.mapeadores.PrestadorMappers
import com.example.myapplication.core.dominio.mapeadores.CategoriaMappers
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.datos.repositorios.ArmadorPerfilPrestadorRepositorio
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ARMADOR DE PERFIL PRESTADOR (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar la visualización y ensamblado de perfiles profesionales.
 * [LEY #9]: Estándar Maverick. Asegura la visualización del ecosistema completo.
 */
@HiltViewModel
class ArmadorPerfilPrestadorViewModel @Inject constructor(
    private val armadorRepo: ArmadorPerfilPrestadorRepositorio,
    private val motorLocal: MotorSincLocal,
    private val categoryRepository: CategoriaRepositorio
) : ViewModel() {

    private val _idPrestador = MutableStateFlow<String?>(null)
    
    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    /**
     * Catálogo maestro de categorías para los iconos de la UI.
     */
    val todasLasCategorias: StateFlow<List<CategoriaDominio>> = categoryRepository.todasLasCategorias
        .map { lista -> lista.map { CategoriaMappers.deEntidadADominio(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flujo reactivo del perfil del prestador polimórfico (Individuo/Empresa/Sucursal).
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val perfilPrestador: StateFlow<PrestadorDominio?> = _idPrestador
        .filterNotNull()
        .flatMapLatest { id -> armadorRepo.obtenerPerfilPolimorficoFlujo(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * 🔥 [ELITE]: Carga el perfil del prestador disparando la descarga profunda desde Firebase.
     */
    fun cargarPerfil(id: String) {
        if (_idPrestador.value == id) return
        _idPrestador.value = id
        
        viewModelScope.launch {
            _estaCargando.value = true
            // Disparar especialista de PULL
            motorLocal.impactarPrestadorDeep(id)
            _estaCargando.value = false
        }
    }
}

