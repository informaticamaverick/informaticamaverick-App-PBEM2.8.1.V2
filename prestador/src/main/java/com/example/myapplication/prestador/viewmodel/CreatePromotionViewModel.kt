package com.example.myapplication.prestador.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.model.PromotionStatus
import com.example.myapplication.prestador.data.model.PromotionType
import com.example.myapplication.prestador.data.model.ProviderPromotion
import com.example.myapplication.prestador.data.repository.PromotionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de crear promociones
 * 
 * Maneja la lógica de creación y guardado de promociones en la BD
 */
@HiltViewModel
class CreatePromotionViewModel @Inject constructor(
    private val promotionRepository: PromotionRepository
) : ViewModel() {
    
    // Estado de la promoción en proceso
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * CREAR y GUARDAR promoción en la base de datos
     */
    fun createPromotion(
        providerId: String,
        providerName: String,
        providerImageUrl: String?,
        type: PromotionType,
        title: String,
        description: String,
        imageUrls: List<Uri>,
        discount: String,
        categories: Set<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Validaciones
                if (title.isBlank()) {
                    _errorMessage.value = "El título es obligatorio"
                    _isLoading.value = false
                    return@launch
                }
                
                if (description.isBlank()) {
                    _errorMessage.value = "La descripción es obligatoria"
                    _isLoading.value = false
                    return@launch
                }
                
                if (imageUrls.isEmpty()) {
                    _errorMessage.value = "Agrega al menos una imagen"
                    _isLoading.value = false
                    return@launch
                }
                
                if (categories.isEmpty()) {
                    _errorMessage.value = "Selecciona al menos una categoría"
                    _isLoading.value = false
                    return@launch
                }
                
                // Crear el modelo de promoción
                val promotion = ProviderPromotion.create(
                    providerId = providerId,
                    providerName = providerName,
                    providerImageUrl = providerImageUrl,
                    type = type,
                    title = title,
                    description = description,
                    imageUrls = imageUrls.map { it.toString() }, // Convertir Uri a String
                    discount = discount.toIntOrNull(),
                    categories = categories.toList(),
                    rating = 4.5f // TODO: Obtener el rating real del prestador
                )
                
                // Guardar en la base de datos
                val promotionId = promotionRepository.createPromotionFromModel(promotion)
                
                _isLoading.value = false
                _successMessage.value = "¡Promoción publicada exitosamente!"
                
                // Callback de éxito
                onSuccess()
                
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al publicar: ${e.message}"
            }
        }
    }
    
    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }

    fun deletePromotion(promotionId: String) {
        viewModelScope.launch {
            try {
                promotionRepository.deletePromotion(promotionId)
                _successMessage.value = "Promoción eliminada"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar: ${e.message}"
            }
        }
    }
    
    /**
     * OBTENER todas las promociones del prestador (para pruebas)
     */
    fun getPromotions(providerId: String) = promotionRepository.getPromotionsAsModel(providerId)

    /**
     * OBTENER promoción por ID como modelo de UI
     */
    fun getPromotionByIdAsModel(promotionId: String) =
        promotionRepository.getPromotionByIdAsModel(promotionId)

    /**
     * ARCHIVAR promoción (cambia estado a ARCHIVED)
     */
    fun archivePromotion(promotionId: String) {
        viewModelScope.launch {
            try {
                promotionRepository.updatePromotionStatus(promotionId, "ARCHIVED")
                _successMessage.value = "Promoción archivada"
            } catch (e: Exception) {
                _errorMessage.value = "Error al archivar: ${e.message}"
            }
        }
    }

    /**
     * ACTUALIZAR promoción existente
     */
    fun updatePromotion(
        existing: com.example.myapplication.prestador.data.model.ProviderPromotion,
        type: com.example.myapplication.prestador.data.model.PromotionType,
        title: String,
        description: String,
        imageUrls: List<String>,
        discount: String,
        categories: Set<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (title.isBlank()) {
                    _errorMessage.value = "El título es obligatorio"
                    _isLoading.value = false
                    return@launch
                }
                if (description.isBlank()) {
                    _errorMessage.value = "La descripción es obligatoria"
                    _isLoading.value = false
                    return@launch
                }
                if (imageUrls.isEmpty()) {
                    _errorMessage.value = "Agrega al menos una imagen"
                    _isLoading.value = false
                    return@launch
                }
                if (categories.isEmpty()) {
                    _errorMessage.value = "Selecciona al menos una categoría"
                    _isLoading.value = false
                    return@launch
                }

                val updated = existing.copy(
                    type = type,
                    title = title,
                    description = description,
                    imageUrls = imageUrls,
                    discount = discount.toIntOrNull(),
                    categories = categories.toList()
                )
                promotionRepository.updatePromotionFromModel(updated)

                _isLoading.value = false
                _successMessage.value = "¡Promoción actualizada!"
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al actualizar: ${e.message}"
            }
        }
    }
    
    /**
     * OBTENER solo promociones activas
     */
    fun getActivePromotions(providerId: String) = promotionRepository.getActivePromotions(providerId)
}
