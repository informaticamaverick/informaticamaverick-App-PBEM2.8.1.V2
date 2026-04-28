package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val saludo: String = "",
    val nombrePrestador: String = "",
    val gananciasSemanales: Double = 0.0,
    val serviceType: String = "",
    val isLoading: Boolean = true,
    val imageBase64: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val presupuestoRepository: PresupuestoRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        calcularSaludo()
        cargarServiceType()
    }

    private fun calcularSaludo() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val saludo = when {
            hora < 12 -> "Buenos días"
            hora < 18 -> "Buenas tardes"
            else -> "Buenas noches"

        }

        _uiState.update { it.copy(saludo = saludo) }
    }

    private fun cargarServiceType() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Siempre cargar desde Firebase para garantizar datos del usuario correcto
                val doc = firestore.collection("providers").document(uid).get().await()
                val tipo = doc.getString("serviceType") ?: "TECHNICAL"
                val perfil = doc.get("perfil") as? Map<String, Any>
                val nombre = perfil?.get("nombre") as? String ?: doc.getString("nombre") ?: ""
                val imagen = perfil?.get("imageBase64") as? String ?: doc.getString("imageBase64") ?: doc.getString("imageUrl")
                _uiState.update { it.copy(serviceType = tipo, nombrePrestador = nombre, imageBase64 = imagen) }
                cargarDatos(tipo)
            } catch (e: Exception) {
                // Fallback a Room si Firebase falla
                providerRepository.getProviderById(uid).collect { provider ->
                    val tipo = provider?.serviceType ?: "TECHNICAL"
                    _uiState.update {
                        it.copy(serviceType = tipo, nombrePrestador = provider?.name ?: "", imageBase64 = provider?.imageUrl)
                    }
                    cargarDatos(tipo)
                }
            }
        }
    }

    private fun cargarDatos(serviceType: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val fromDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            val ganancias = presupuestoRepository.getGananciasDesde(uid, fromDate)
            _uiState.update { it.copy(gananciasSemanales = ganancias, isLoading = false) }
        }
    }
}