package com.example.myapplication.prestador.datos.repositorios

import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo
import com.example.myapplication.core.dominio.repository.TopicoRepositorio
import com.example.myapplication.core.datos.local.dao.SuscripcionTopicDao
import com.example.myapplication.core.datos.local.entidades.SuscripcionTopicEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- REPOSITORIO DE SINCRONIZACIÓN DE TÓPICOS PRESTADOR (PRO) ---
 * [PROPÓSITO]: Gestionar las suscripciones de red para la recepción de licitaciones (Concursos) y señales de zona.
 * [LEY #9]: Estándar Maverick. Especialista en la Higiene de Red Profesional.
 */
@Singleton
class SincPrestadorTopicksRepositorio @Inject constructor(
    private val topicRepo: TopicoRepositorio,
    private val suscripcionDao: SuscripcionTopicDao,
    private val generadorTopicos: GeneradorTópicosFCM
) {
    /**
     * 🔥 [ELITE]: Sincroniza la matriz completa de hilos de red (CPs x Categorías) del prestador.
     * [LEY #5]: Background Warm-up.
     */
    suspend fun sincronizarMatrizDeRed(maestro: PerfilPrestadorDeepModelo) {
        Log.d("SincPreTopics", "🔄 [SYNC_PRO_TOPICS] Sincronizando matriz de hilos profesionales...")
        val topicsNuevos = mutableSetOf<String>()
        
        val listaCPs = (maestro.prestador.direcciones.map { it.codigoPostal } + 
                        maestro.empresas.flatMap { it.sucursales }.mapNotNull { it.direccion?.codigoPostal })
                        .distinct().filter { it.isNotBlank() }
        
        val listaCats = (maestro.prestador.perfil.idCategorias + 
                         maestro.empresas.flatMap { it.empresa.idCategorias }).distinct()

        // 1. Suscripción a Zonas (Señales de Emergencia)
        listaCPs.forEach { cp -> 
            topicsNuevos.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.ZONA, cp)) 
        }

        // 2. Suscripción a Combinaciones (Concursos y Ofertas)
        listaCPs.forEach { cp ->
            val cpStd = NormalizadorDirecciones.limpiarCodigoPostal(cp)
            listaCats.forEach { cat ->
                // Canal de Licitaciones: C_4000_plomeria
                topicsNuevos.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.CONCURSO, cpStd, cat))
                // Canal de Monitoreo de Ofertas (Ecosistema): H_4000_plomeria
                topicsNuevos.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cpStd, cat))
            }
        }

        actualizarSuscripciones(topicsNuevos)
    }

    private suspend fun actualizarSuscripciones(nuevosTopics: Set<String>) {
        val actuales = suscripcionDao.obtenerSuscripcionesActivas().first().map { it.topic }.toSet()
        
        // 1. Abandonar hilos antiguos
        (actuales - nuevosTopics).forEach { topic ->
            android.util.Log.d("SincPreTopics", "🔌 [UNSUBSCRIBE] Abandonando canal: $topic")
            topicRepo.unsubscribeFromTopic(topic)
            suscripcionDao.eliminarSuscripcion(topic)
        }

        // 2. Suscribir nuevos hilos (Escalonado)
        (nuevosTopics - actuales).forEachIndexed { index, topic ->
            android.util.Log.d("SincPreTopics", "📡 [SUBSCRIBE] Uniéndose a canal: $topic")
            delay((200 * (index + 1)).milliseconds)
            topicRepo.subscribeToTopic(topic)
            suscripcionDao.insertarSuscripcion(SuscripcionTopicEntity(topic, "PRO_AUTO"))
        }
    }
}














































