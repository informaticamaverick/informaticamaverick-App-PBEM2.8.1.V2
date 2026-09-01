package com.example.myapplication.prestador.viewmodel.localidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.GeorefRepositorio
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE LOCALIDADES (V2026.12) ---
 * [UNIFICADO]: Consume GeorefRepositorio del Core.
 * [LEY #9]: Nombres en Español.
 */
@HiltViewModel
class LocalidadesViewModel @Inject constructor(
    private val repository: GeorefRepositorio
) : ViewModel() {

    private val _provincias = MutableStateFlow<List<String>>(emptyList())
    val provincias: StateFlow<List<String>> = _provincias

    private val _localidades = MutableStateFlow<List<DireccionDominio>>(emptyList())
    val localidades: StateFlow<List<DireccionDominio>> = _localidades

    init {
        viewModelScope.launch {
            try {
                _provincias.value = repository.obtenerProvincias()
            } catch (_: Exception) {}
        }
    }

    fun cargarLocalidades(provincia: String) {
        if (provincia.isBlank()) {
            _localidades.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _localidades.value = repository.obtenerLocalidades(provincia)
            } catch (_: Exception) {
                _localidades.value = emptyList()
            }
        }
    }

    fun cargarCodigoPostal(localidad: String, provincia: String, onResult: (String) -> Unit) {
        if (localidad.isBlank() || provincia.isBlank()) return
        viewModelScope.launch {
            try {
                // val cp = repository.obtenerCodigoPostal(localidad, provincia)
                onResult("")
            } catch (_: Exception) { /* CP queda vacío */ }
        }
    }
}














































