package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.entidades.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PRESUPUESTOS (Atómico - v2026.ELITE) ---
 * 
 * [PROPÓSITO]: Orquestador centralizado de transacciones comerciales de presupuestos.
 * [LEY #2]: Persistencia Local-First con Room.
 * [LEY #8]: Tránsito Efímero vía RTDB y Respaldo vía Firestore.
 * [LEY #9]: Estándar Mav en Español.
 */
@Singleton
class PresupuestoRepositorio @Inject constructor(
    private val baseDeDatos: AppDatabase,
    private val firestore: FirebaseFirestore
) {
    private val presupuestoDao = baseDeDatos.presupuestoFinalDao()
    private val rtdb = FirebaseDatabase.getInstance().reference
    
    val todosLosPresupuestos: Flow<List<PresupuestoFinalEntity>> = presupuestoDao.obtenerTodos()

    fun obtenerPresupuestosPorConcurso(idConcurso: String): Flow<List<PresupuestoFinalEntity>> = 
        presupuestoDao.obtenerPorConcurso(idConcurso)

    /**
     * Presupuestos: Viajan por RTDB (Tránsito Efímero - Ley #8).
     * [ELITE]: Envía el snapshot final inmutable.
     */
    suspend fun enviarPresupuesto(presupuesto: PresupuestoFinalEntity, lineas: List<ProductoFinalEntity>, finanzas: List<FinanzaFinalEntity> = emptyList()) {
        android.util.Log.d("MavElite", "[ENVIO_PRESUPUESTO_ATÓMICO]")
        presupuestoDao.guardarPresupuestoCompleto(presupuesto, lineas, finanzas)
        
        val payload = mapOf(
            "cabecera" to presupuesto,
            "lineas" to lineas,
            "finanzas" to finanzas,
            "tipo" to "PRESUPUESTO_FINAL"
        )
        
        rtdb.child("transito_presupuestos")
            .child(presupuesto.idCliente)
            .child(presupuesto.idPresupuesto)
            .setValue(payload)
            .await()
    }

    suspend fun confirmarRecepcionYEliminarDeNube(idCliente: String, idPresupuesto: String) {
        rtdb.child("transito_presupuestos")
            .child(idCliente)
            .child(idPresupuesto)
            .removeValue()
            .await()
    }

    suspend fun actualizarEstadoPresupuesto(id: String, estado: EstadoPresupuesto) {
        presupuestoDao.actualizarEstado(id, estado)
    }

    fun obtenerPresupuestoPorId(id: String): Flow<com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems?> = 
        presupuestoDao.obtenerPorId(id)

    /**
     * 🔥 [ELITE]: Escucha presupuestos entrantes para una identidad (Ley #8).
     */
    fun observarPresupuestosEntrantes(idIdentidad: String) {
        val ref = rtdb.child("transito_presupuestos").child(idIdentidad)
        ref.addChildEventListener(object : com.google.firebase.database.ChildEventListener {
            override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                try {
                    val cabecera = snapshot.child("cabecera").getValue(PresupuestoFinalEntity::class.java)
                    val lineas = snapshot.child("lineas").children.mapNotNull { it.getValue(ProductoFinalEntity::class.java) }
                    val finanzas = snapshot.child("finanzas").children.mapNotNull { it.getValue(FinanzaFinalEntity::class.java) }
                    
                    if (cabecera != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            presupuestoDao.guardarPresupuestoCompleto(cabecera, lineas, finanzas)
                            confirmarRecepcionYEliminarDeNube(idIdentidad, cabecera.idPresupuesto)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PresupuestoRepo", "❌ Error al procesar presupuesto entrante", e)
                }
            }
            override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {}
            override fun onChildMoved(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun obtenerPresupuestosPorCliente(id: String): Flow<List<PresupuestoFinalEntity>> = 
        presupuestoDao.obtenerPorCliente(id)

    fun obtenerPresupuestosPorPrestador(id: String): Flow<List<PresupuestoFinalEntity>> =
        presupuestoDao.obtenerPorPrestador(id)

    fun obtenerPresupuestosPorIdentidad(id: String): Flow<List<PresupuestoFinalEntity>> =
        presupuestoDao.obtenerTodosParaPerfil(id)

    suspend fun eliminarPresupuesto(id: String) {
        android.util.Log.d("PresupuestoRepo", "🗑️ [ELIMINAR_PRESUPUESTO] ID: $id")
        presupuestoDao.eliminarPorId(id)
        // Eliminamos también del respaldo en la nube
        try {
            firestore.collection("presupuestos_finales").document(id).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("PresupuestoRepo", "⚠️ Error al eliminar respaldo de nube para: $id", e)
        }
    }

    suspend fun guardarPresupuestoCompleto(presupuesto: PresupuestoFinalEntity, lineas: List<ProductoFinalEntity>, finanzas: List<FinanzaFinalEntity> = emptyList()) {
        presupuestoDao.guardarPresupuestoCompleto(presupuesto, lineas, finanzas)
        // También sincronizamos con la nube (Firestore como respaldo)
        firestore.collection("presupuestos_finales").document(presupuesto.idPresupuesto).set(presupuesto).await()
    }
}

