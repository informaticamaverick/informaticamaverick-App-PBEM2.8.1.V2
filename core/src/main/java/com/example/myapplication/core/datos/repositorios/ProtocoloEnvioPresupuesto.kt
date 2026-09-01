package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.entidades.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
@Singleton
class ProtocoloEnvioPresupuesto @Inject constructor() {
    private val rtdb = FirebaseDatabase.getInstance().reference

    /**
     * Envía una señal de aviso al cliente sobre un nuevo presupuesto disponible.
     * En lugar de enviar todo el JSON, enviamos un puntero y metadatos mínimos.
     */
    suspend fun enviarAvisoAlCliente(presupuesto: PresupuestoFinalEntity) {
        android.util.Log.d("ProtocoloEnvio", "📡 [ENVIO_SEÑAL] Enviando aviso de presupuesto al cliente: ${presupuesto.idCliente}")
        
        val mapaAviso = mapOf(
            "idPresupuesto" to presupuesto.idPresupuesto,
            "idPrestador" to presupuesto.idPrestador,
            "nombrePrestador" to presupuesto.nombrePrestador,
            "titulo" to presupuesto.tituloTrabajo,
            "total" to presupuesto.totalGeneral,
            "marcaTiempo" to System.currentTimeMillis()
        )

        rtdb.child("transito_presupuestos")
            .child(presupuesto.idCliente)
            .child(presupuesto.idPresupuesto)
            .setValue(mapaAviso)
            .await()
    }
}
**/
