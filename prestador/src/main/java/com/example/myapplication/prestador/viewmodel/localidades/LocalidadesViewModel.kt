package com.example.myapplication.prestador.viewmodel.localidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.repository.GeorefRepository
import com.example.myapplication.prestador.data.model.Localidad
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalidadesViewModel @Inject constructor(
    private val repository: GeorefRepository
) : ViewModel() {

    private val _provincias = MutableStateFlow<List<String>>(emptyList())
    val provincias: StateFlow<List<String>> = _provincias

    private val _localidades = MutableStateFlow<List<Localidad>>(emptyList())
    val localidades: StateFlow<List<Localidad>> = _localidades

    init {
        viewModelScope.launch {
            try {
                _provincias.value = repository.getProvincias() }
            catch (_: Exception) {}
        }
    }

    fun cargarLocalidades(provincia: String) {
        if (provincia.isBlank()) {
            _localidades.value = emptyList(); return
        }
        viewModelScope.launch {
            try {
                _localidades.value = repository.getLocalidades(provincia)
            } catch (_: Exception) {
                _localidades.value = emptyList()
            }
        }
    }

    fun cargarCodigoPostal(localidad: String, provincia: String, onResult: (String) -> Unit) {
        if (localidad.isBlank() || provincia.isBlank()) return
        viewModelScope.launch {
            try {
                val cp = repository.getCodigoPostalPorLocalidad(localidad, provincia)
                onResult(cp)
            } catch (_: Exception) { /* CP queda vacío */ }
        }
    }
}