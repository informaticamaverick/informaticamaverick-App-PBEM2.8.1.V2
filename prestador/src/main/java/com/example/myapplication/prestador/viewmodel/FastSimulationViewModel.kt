/**


package com.example.myapplication.prestador.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.dao.ClienteDao
//import com.example.myapplication.prestador.data.mock.ClientesMockData
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FastSimulationViewModel @Inject constructor(
    application: Application,
    private val clienteDao: ClienteDao
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)

    private val serviciosFast = listOf(
        Pair("Reparación eléctrica urgente", "Electricidad"),
        Pair("Péra de agua en cocina", "Plomería"),
        Pair("Instalación de aire acondicionado", "Climatización"),
        Pair("Gasista urgente", "Gas"),
        Pair("Pintura interior", "Pintura"),
        Pair("Reparación de cañería", "Plomería"),
        Pair("Cortocircuito en tablero", "Electricidad"),
        Pair("Destapación de cañería", "Plomería"),
        Pair("Revisión de caldera", "Gas"),
        Pair("Reparación de electrodoméstio", "Electrónica")
    )

    init {
        iniciarSimulacion()
    }

    @SuppressLint("MissingPermission")
    private fun iniciarSimulacion() {
        viewModelScope.launch {
            // Espera inicial antes de la primera solicitud
            delay(15_000)
            println("⚡ FastSimulation iniciada")

            while (true) {
                try {
                    // Obtiene clientes reales de Room, si no hay usa los mock
                    val clientes = clienteDao.getAllClientes().let { flow ->
                        var lista = emptyList<com.example.myapplication.prestador.data.local.entity.ClienteEntity>()
                        val job = viewModelScope.launch { flow.collect { lista = it } }
                        delay(300)
                        job.cancel()
                        lista
                    }

                    val (clienteNombre, clienteId) = if (clientes.isNotEmpty()) {
                        val c = clientes.random()
                        Pair(c.nombre, c.id)
                    } else {
                      //  val mock = ClientesMockData.clientes.random()
                       // Pair(mock.nombreCompleto, mock.id)
                    }

                    val location = try { fusedLocation.lastLocation.await() } catch (e: Exception) { null }
                    val lat = location?.latitude ?: (-26.82 + (Math.random() * 0.05 - 0.025))
                    val lng = location?.longitude ?: (-65.21 + (Math.random() * 0.05 - 0.025))

                    val (titulo, categoria) = serviciosFast.random()
                    val urgente = (0..3).random() == 0 // 25% de probabilidad de urgente

                    firestore.collection("solicitudes_fast").add(
                        mapOf(
                            "titulo" to titulo,
                            "descripcion" to titulo,
                            "clienteNombre" to clienteNombre,
                            "clienteId" to clienteId,
                            "lat" to lat,
                            "lng" to lng,
                            "urgente" to urgente,
                            "estado" to "pendiente",
                            "categoria" to categoria,
                            "creadoEn" to System.currentTimeMillis()
                        )
                    ).await()

                    println("⚡ FastSimulation: nueva solicitud '$titulo' de $clienteNombre (urgente=$urgente)")

                } catch (e: Exception) {
                    println("❌ FastSimulation error: ${e.message}")
                }

                // Espera entre 2 y 5 minutos para la próxima solicitud
                val espera = (120_000L..300_000L).random()
                println("⚡ FastSimulation: próxima solicitud en ${espera / 1000}s")
                delay(espera)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("🔴 FastSimulationViewModel DESTRUIDO")
    }
}

private fun LongRange.random(): Long = (first + (Math.random() * (last - first)).toLong())
**/