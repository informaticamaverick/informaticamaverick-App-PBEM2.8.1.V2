package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.EventoDao
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE EVENTOS Y AGENDA (Atómico) ---
 * [ELITE v2026.FINAL]: Orquestador centralizado de citas, turnos y visitas.
 */
@Singleton
class EventoRepositorio @Inject constructor(
    private val eventoDao: EventoDao,
    private val firestore: FirebaseFirestore
) {
    fun obtenerPorCliente(idCliente: String): Flow<List<EventoEntity>> =
        eventoDao.obtenerPorCliente(idCliente)

    /**
     * [LEY #14]: Filtrado en la fuente (SQL-First).
     */
    fun buscarPorCliente(idCliente: String, query: String): Flow<List<EventoEntity>> =
        eventoDao.buscarPorCliente(idCliente, query)

    fun obtenerPorPrestador(idPrestador: String): Flow<List<EventoEntity>> =
        eventoDao.obtenerPorPropietarioSucursal(idPrestador)

    fun obtenerPorChat(idChat: String): Flow<List<EventoEntity>> =
        eventoDao.obtenerPorChat(idChat)

    suspend fun eliminarMasivo(ids: List<String>) {
        eventoDao.eliminarMasivo(ids)
    }

    suspend fun actualizarEstado(id: String, estado: EstadoEvento) {
        eventoDao.actualizarEstado(id, estado)
        firestore.collection("eventos").document(id).update("estado", estado.name).await()
    }

    suspend fun insertar(evento: EventoEntity) {
        eventoDao.insertar(evento)
        firestore.collection("eventos").document(evento.id).set(evento).await()
    }
}


































