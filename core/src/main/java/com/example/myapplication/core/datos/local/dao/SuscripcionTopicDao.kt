package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.SuscripcionTopicEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE SUSCRIPCIONES A TÓPICOS (CONTROL DE SEÑALES v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar la persistencia de tópicos de red.
 */
@Dao
interface SuscripcionTopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSuscripcion(suscripcion: SuscripcionTopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSuscripciones(lista: List<SuscripcionTopicEntity>)

    @Query("SELECT * FROM suscripciones_topic WHERE estaActiva = 1")
    fun obtenerSuscripcionesActivas(): Flow<List<SuscripcionTopicEntity>>

    @Query("SELECT * FROM suscripciones_topic WHERE estaActiva = 1")
    suspend fun obtenerListaSuscripcionesActivas(): List<SuscripcionTopicEntity>

    @Query("SELECT * FROM suscripciones_topic WHERE topic = :topic LIMIT 1")
    suspend fun obtenerSuscripcion(topic: String): SuscripcionTopicEntity?

    @Query("UPDATE suscripciones_topic SET estaActiva = 0 WHERE topic = :topic")
    suspend fun marcarComoInactiva(topic: String)

    @Query("DELETE FROM suscripciones_topic WHERE topic = :topic")
    suspend fun eliminarSuscripcion(topic: String)

    @Query("DELETE FROM suscripciones_topic")
    suspend fun limpiarTodosLosTopics()
}

































