package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.NotificacionDao
import com.example.myapplication.core.datos.local.entidades.NotificacionEntity
import com.example.myapplication.core.dominio.modelos.ElementoNotificacion
import com.example.myapplication.core.dominio.modelos.TipoNotificacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE NOTIFICACIONES (Atómico) ---
 */
@Singleton
class NotificacionRepositorio @Inject constructor(
    private val dao: NotificacionDao
) {
    fun obtenerConteoNoLeidas(): Flow<Int> = dao.obtenerConteoNoLeidas()

    fun obtenerTodas(): Flow<List<ElementoNotificacion>> = 
        dao.obtenerTodas().map { lista -> lista.map { it.aModelo() } }

    fun obtenerNoLeidas(): Flow<List<ElementoNotificacion>> = 
        dao.obtenerNoLeidas().map { lista -> lista.map { it.aModelo() } }

    fun obtenerPorTipo(tipo: TipoNotificacion): Flow<List<ElementoNotificacion>> = 
        dao.obtenerPorTipo(tipo).map { lista -> lista.map { it.aModelo() } }

    suspend fun marcarComoLeida(id: Long) = dao.marcarComoLeida(id)
    suspend fun marcarTodasComoLeidas() = dao.marcarTodasComoLeidas()
    suspend fun eliminarPorId(id: Long) = dao.eliminarPorId(id)

    suspend fun insertar(notificacion: NotificacionEntity) =
        dao.insertar(notificacion)
}
