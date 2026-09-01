package com.example.myapplication.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.mapeadores.PrestadorMappers
import com.example.myapplication.core.utilidades.filtroDeTexto
import com.example.myapplication.coordinadores.CoordinadorAcciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- FAVORITOS VIEWMODEL (EL OBRERO DE LOS PREFERIDOS) ---
 * [ELITE]: Gestiona la lista de prestadores favoritos de forma independiente.
 */
@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val accesoDirectoRepositorio: AccesoDirectoRepositorio,
    private val searchDao: com.example.myapplication.core.datos.local.dao.ResultadoBusquedaPrestadorDao,
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor,
    val coordinator: CoordinadorAcciones,
    val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion // 🔥 [NEW]
) : ViewModel() {

    private val _mostrarPanelFavoritos = MutableStateFlow(false)
    val mostrarPanelFavoritos: StateFlow<Boolean> = _mostrarPanelFavoritos.asStateFlow()
    fun establecerMostrarPanelFavoritos(visible: Boolean) { 
        _mostrarPanelFavoritos.value = visible 
        navCoordinador.actualizarVisibilidadHoja(visible) // 🔥 [FIX]
    }

    /**
     * IDs de los prestadores marcados como favoritos en el contexto 'home'.
     */
    val favoriteIds: StateFlow<Set<String>> = accesoDirectoRepositorio.obtenerShortcutsPorContexto("home")
        .map { list -> 
            list.filter { it.tipo == "provider" }.map { it.idDestino }.toSet() 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /**
     * 🔥 [ELITE]: Flujo reactivo de prestadores favoritos.
     * Cruza los IDs de accesos directos con la base de datos local y la ubicación actual.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val favoriteProviders: StateFlow<List<PrestadorDominio>> = favoriteIds
        .flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList())
            else {
                combine(
                    searchDao.obtenerPorIds(ids.toList()),
                    coordinator.direccionActiva
                ) { views, direccion ->
                    views.map { it.aModeloDominio(direccion?.latitud, direccion?.longitud) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun manageShortcut(id: String, add: Boolean, label: String? = null, icon: String? = null) {
        viewModelScope.launch {
            if (add) accesoDirectoRepositorio.agregarShortcut("home", id, "provider", label, icon)
            else accesoDirectoRepositorio.eliminarShortcut("home", id)
        }
    }
}
