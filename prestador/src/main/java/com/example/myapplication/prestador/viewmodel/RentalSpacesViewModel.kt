package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.RentalSpaceEntity
import com.example.myapplication.prestador.data.repository.RentalSpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel para gestionar espacios de alquiler
 */
@HiltViewModel
class RentalSpacesViewModel @Inject constructor(
    private val rentalSpaceRepository: RentalSpaceRepository
) : ViewModel() {
    
    private val _providerId = MutableStateFlow<String?>(null)
    
    /**
     * Lista de espacios del prestador
     */
    val rentalSpaces: StateFlow<List<RentalSpaceEntity>> = _providerId
        .filterNotNull()
        .flatMapLatest { providerId ->
            rentalSpaceRepository.getAllSpacesByProviderId(providerId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Contador de espacios
     */
    val spacesCount: StateFlow<Int> = rentalSpaces
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    /**
     * Establecer el ID del prestador
     */
    fun setProviderId(providerId: String) {
        _providerId.value = providerId
    }
    
    /**
     * Agregar un nuevo espacio
     */
    fun addSpace(
        name: String,
        description: String?,
        pricePerHour: Double,
        blockDuration: Int
    ) {
        val providerId = _providerId.value ?: return
        
        // Validar datos
        if (!validateSpaceData(name, pricePerHour, blockDuration)) {
            return
        }
        
        viewModelScope.launch {
            val space = RentalSpaceEntity(
                id = UUID.randomUUID().toString(),
                providerId = providerId,
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                pricePerHour = pricePerHour,
                blockDuration = blockDuration,
                isActive = true
            )
            rentalSpaceRepository.insertSpace(space)
        }
    }
    
    /**
     * Actualizar un espacio existente
     */
    fun updateSpace(
        id: String,
        name: String,
        description: String?,
        pricePerHour: Double,
        blockDuration: Int,
        isActive: Boolean
    ) {
        val providerId = _providerId.value ?: return
        
        // Validar datos
        if (!validateSpaceData(name, pricePerHour, blockDuration)) {
            return
        }
        
        viewModelScope.launch {
            val space = RentalSpaceEntity(
                id = id,
                providerId = providerId,
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                pricePerHour = pricePerHour,
                blockDuration = blockDuration,
                isActive = isActive,
                updatedAt = System.currentTimeMillis()
            )
            rentalSpaceRepository.updateSpace(space)
        }
    }
    
    /**
     * Eliminar un espacio
     */
    fun deleteSpace(space: RentalSpaceEntity) {
        viewModelScope.launch {
            rentalSpaceRepository.deleteSpace(space)
        }
    }
    
    /**
     * Activar/desactivar un espacio
     */
    fun toggleSpaceActiveStatus(id: String, isActive: Boolean) {
        viewModelScope.launch {
            rentalSpaceRepository.updateSpaceActiveStatus(id, isActive)
        }
    }
    
    /**
     * Validar datos del espacio
     */
    private fun validateSpaceData(
        name: String,
        pricePerHour: Double,
        blockDuration: Int
    ): Boolean {
        // Nombre no puede estar vacío
        if (name.trim().isBlank()) {
            return false
        }
        
        // Precio debe ser positivo
        if (pricePerHour <= 0) {
            return false
        }
        
        // Duración de bloque debe ser válida (60, 90, 120)
        if (blockDuration !in listOf(60, 90, 120)) {
            return false
        }
        
        return true
    }
}
