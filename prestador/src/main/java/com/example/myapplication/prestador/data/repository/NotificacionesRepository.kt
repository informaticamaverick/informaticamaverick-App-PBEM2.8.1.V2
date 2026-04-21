package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.NotificacionDao
import com.example.myapplication.prestador.data.local.entity.NotificacionEntity
import com.example.myapplication.prestador.data.local.entity.toEntity
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface NotificacionRepository {
    fun getAllFlow():
            Flow<List<NotificacionItem>>
    fun getUnreadFlow():
            Flow<List<NotificacionItem>>
    fun getByTipoFlow(tipo: TipoNotificacion):
            Flow<List<NotificacionItem>>
    fun getUnreadCount(): Flow<Int>
    suspend fun guardar(notificacion: NotificacionItem): Long
    suspend fun marcarLeida(id: Long)
    suspend fun marcarTodasLeidas()
    suspend fun eliminar(id: Long)
    suspend fun eliminarTodas()
    suspend fun getTotalCount(): Int
}

class RoomNotificacionRepository @Inject
constructor(
    private val dao: NotificacionDao
) : NotificacionRepository {

    override fun getAllFlow(): Flow<List<NotificacionItem>> =
        dao.getAllFlow().map { list -> list.map { it.toModel() } }


    override fun getUnreadFlow(): Flow<List<NotificacionItem>> = dao.getUnreadFlow().map { list ->
            list.map { it.toModel() } }

    override fun getByTipoFlow(tipo: TipoNotificacion): Flow<List<NotificacionItem>> = dao.getByTipoFlow(tipo.name).map {
                list -> list.map { it.toModel() } }

    override fun getUnreadCount(): Flow<Int> = dao.getUnreadCount()

    override suspend fun guardar(notificacion: NotificacionItem): Long = dao.insert(notificacion.toEntity())

    override suspend fun marcarLeida(id: Long) = dao.marcarLeida(id)

    override suspend fun marcarTodasLeidas() = dao.marcarTodasLeidas()

    override suspend fun eliminar(id: Long) = dao.deleteById(id)

    override suspend fun eliminarTodas() = dao.deleteAll()

    override suspend fun getTotalCount(): Int = dao.getTotalCount()
}