package com.example.myapplication.core.dominio.motores

import com.example.myapplication.core.dominio.repository.TopicoRepositorio
import com.example.myapplication.core.datos.local.dao.SuscripcionTopicDao
import com.example.myapplication.core.datos.local.entidades.SuscripcionTopicEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- MOTOR DE TÓPICOS (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Gestión atómica de suscripciones a Firebase Cloud Messaging.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Singleton
class MotorTopicos @Inject constructor(
    private val repositorioTopic: TopicoRepositorio,
    private val suscripcionDao: SuscripcionTopicDao
) {
    private val topicosActivosLocal = mutableSetOf<String>()
    private val mutex = Mutex()

    /**
     * Sincroniza las suscripciones del dispositivo con un conjunto deseado de tópicos.
     */
    suspend fun sincronizarSuscripciones(topicsDeseados: Set<String>, origen: String = "AUTO") {
        mutex.withLock {
            android.util.Log.d("MotorTopicos", "🌐 Sincronizando topics. Deseados: ${topicsDeseados.size}")
            
            // 1. Cargar tópicos actuales si el set local está vacío
            if (topicosActivosLocal.isEmpty()) {
                val actuales = suscripcionDao.obtenerListaSuscripcionesActivas()
                topicosActivosLocal.addAll(actuales.map { it.topic })
            }

            // 2. Determinar qué eliminar y qué añadir
            val aEliminar = topicosActivosLocal - topicsDeseados
            val aSuscribir = topicsDeseados - topicosActivosLocal

            // 3. Ejecutar bajas
            aEliminar.forEach { topic ->
                repositorioTopic.unsubscribeFromTopic(topic)
                suscripcionDao.eliminarSuscripcion(topic)
                topicosActivosLocal.remove(topic)
                android.util.Log.d("MotorTopicos", "📉 Unsubscribed: $topic")
            }

            // 4. Ejecutar altas (con delay táctico para evitar saturación)
            aSuscribir.filter { it.isNotBlank() }.forEachIndexed { index, topic ->
                delay((200 * (index + 1)).milliseconds.coerceAtMost(2000.milliseconds))
                repositorioTopic.subscribeToTopic(topic)
                suscripcionDao.insertarSuscripcion(SuscripcionTopicEntity(topic, origen))
                topicosActivosLocal.add(topic)
                android.util.Log.d("MotorTopicos", "📈 Subscribed: $topic")
            }
        }
    }

    /**
     * Limpieza total de tópicos (útil al cerrar sesión).
     */
    suspend fun limpiarTodo() {
        mutex.withLock {
            topicosActivosLocal.forEach { topic ->
                repositorioTopic.unsubscribeFromTopic(topic)
            }
            topicosActivosLocal.clear()
            suscripcionDao.limpiarTodosLosTopics()
            android.util.Log.d("MotorTopicos", "🧹 Todos los tópicos eliminados.")
        }
    }
}

