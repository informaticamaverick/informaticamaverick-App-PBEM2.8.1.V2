package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- SEARCH PROVIDER VIEWMODEL ---
 * Maneja la lógica de búsqueda de prestadores combinando Room y Firebase.
 * Sigue el patrón "Single Source of Truth".
 */
@HiltViewModel
class SearchProviderViewModel @Inject constructor(
    private val repository: ProviderRepository
) : ViewModel() {

    private val _zipCode = MutableStateFlow("")
    val zipCode: StateFlow<String> = _zipCode.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    /**
     * Flujo reactivo de prestadores filtrados.
     * Se actualiza automáticamente cuando cambia Room tras una sincronización con Firebase.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val providersResult: StateFlow<List<Provider>> = combine(_zipCode, _category) { zip, cat ->
        zip to cat
    }.flatMapLatest { (zip, cat) ->
        repository.getFilteredProviders(zip, cat)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Ejecuta la búsqueda.
     * 1. Actualiza los filtros locales (lo que dispara la observación en Room).
     * 2. Llama al repositorio para traer datos frescos de Firebase.
     */
    fun performSearch(zipCode: String, category: String) {
        _zipCode.value = zipCode
        _category.value = category
        
        viewModelScope.launch {
            _isSearching.value = true
            repository.searchAndSyncProviders(zipCode, category)
            _isSearching.value = false
        }
    }

    /**
     * Limpia los resultados actuales.
     */
    fun clearSearch() {
        _zipCode.value = ""
        _category.value = ""
        viewModelScope.launch {
            repository.clearProviders()
        }
    }
}
