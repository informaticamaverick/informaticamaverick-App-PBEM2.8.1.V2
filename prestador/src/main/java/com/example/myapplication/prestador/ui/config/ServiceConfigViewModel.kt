package com.example.myapplication.prestador.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ServiceConfig(
    val hasPhysicalStore: Boolean = false,
    val is24Hours: Boolean = false,
    val hasHomeVisits: Boolean = false,
    val hasStoreAppointments: Boolean = false,
    val services: List<String> = emptyList()
)

@HiltViewModel
class ServiceConfigViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _configState = MutableStateFlow<ServiceConfig?>(null)
    val configState: StateFlow<ServiceConfig?> = _configState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadCurrentConfig() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val document = firestore.collection("providers")
                    .document(userId)
                    .get()
                    .await()

                if (document.exists()) {
                    val config = ServiceConfig(
                        hasPhysicalStore = document.getBoolean("hasPhysicalStore") ?: false,
                        is24Hours = document.getBoolean("is24Hours") ?: false,
                        hasHomeVisits = document.getBoolean("isHomeService") ?: false,
                        hasStoreAppointments = document.getBoolean("hasStoreAppointments") ?: false,
                        services = (document.get("servicios") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    )
                    _configState.value = config
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar configuración: ${e.message}"
            }
        }
    }

    fun saveConfiguration(
        hasPhysicalStore: Boolean,
        is24Hours: Boolean,
        hasHomeVisits: Boolean,
        hasStoreAppointments: Boolean,
        services: List<String>
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val updates = hashMapOf<String, Any>(
                    "hasPhysicalStore" to hasPhysicalStore,
                    "is24Hours" to is24Hours,
                    "isHomeService" to hasHomeVisits,
                    "hasStoreAppointments" to hasStoreAppointments,
                    "servicios" to services,
                    "lastUpdated" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("providers")
                    .document(userId)
                    .update(updates)
                    .await()

                _configState.value = ServiceConfig(
                    hasPhysicalStore, is24Hours, hasHomeVisits, hasStoreAppointments, services
                )
                _isLoading.value = false
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }
}
