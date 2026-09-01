package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.dominio.repository.TopicoRepositorio
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE TOPICS FIREBASE (Atómico) ---
 * 
 * [PROPÓSITO]: Gestionar las suscripciones de Firebase Cloud Messaging (FCM) 
 * segmentadas por Código Postal y Categoría.
 * 
 * [FUNCIONAMIENTO]: Utiliza el SDK de Firebase Messaging para suscribir o 
 * desvincular al dispositivo de hilos de notificaciones específicos.
 * 
 * [RELACIÓN]: Implementa la interfaz [TopicoRepositorio]. Es una herramienta 
 * vital para el sistema de Licitaciones (Tenders) y Promociones, permitiendo
 * que ambas Apps reciban alertas geolocalizadas.
 */
@Singleton
class FirebaseTopicRepositorio @Inject constructor() : TopicoRepositorio {

    override suspend fun subscribeToTopic(topic: String): Result<Unit> = runCatching {
        android.util.Log.d("FCM_TOPIC", "📡 [SUBSCRIBE_START] Intentando suscribirse a: $topic")
        FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
        android.util.Log.d("FCM_TOPIC", "✅ [SUBSCRIBE_OK] Suscripción exitosa a: $topic")
        Unit
    }

    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> = runCatching {
        android.util.Log.d("FCM_TOPIC", "🔌 [UNSUBSCRIBE_START] Intentando cancelar suscripción a: $topic")
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
        android.util.Log.d("FCM_TOPIC", "✅ [UNSUBSCRIBE_OK] Cancelación exitosa de: $topic")
        Unit
    }
}


































