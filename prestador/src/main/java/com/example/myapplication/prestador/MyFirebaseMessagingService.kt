package com.example.myapplication.prestador

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.core.data.local.dao.UserDao
import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.core.data.repository.ChatRepository
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import com.example.myapplication.prestador.data.repository.AppSettingsRepository
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificacionRepository: NotificacionRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    @Inject
    lateinit var userDao: UserDao

    @Inject
    lateinit var providerDao: ProviderDao

    private val jobScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Sincronizar Token FCM en Firestore para ruteo de notificaciones
        FirebaseFirestore.getInstance()
            .collection("providers")
            .document(uid)
            .set(hashMapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e("FCM_TOKEN", "Error guardando token en Firestore: ${e.message}")
            }
            
        // También en Realtime Database para coherencia rápida si es necesario
        FirebaseDatabase.getInstance().reference
            .child("status")
            .child(uid)
            .child("fcmToken")
            .setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d("FCM_RECEIVE", "Mensaje recibido de: ${message.from}")
        Log.d("FCM_RECEIVE", "Data payload: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "Nueva Notificación"
        val body = message.notification?.body ?: message.data["body"] ?: "Tienes una nueva actualización."
        val tenderId = message.data["tenderId"] ?: ""
        val chatId = message.data["chatId"] ?: ""
        val senderId = message.data["senderId"] ?: ""

        // ─── LÓGICA DE SUSCRIPCIÓN (SSOT CORE) ───────────────────────────────────
        // Verificamos el estado de suscripción desde UserEntity (Fuente de Verdad)
        val isSubscribed = runBlocking {
            userDao.getUserOnce()?.isSubscribed ?: providerDao.getProviderById(FirebaseAuth.getInstance().currentUser?.uid ?: "")?.isSubscribed ?: false
        }

        // Blindaje de Contenido (Upsell)
        // Si no es premium y es una licitación, ocultamos detalles específicos.
        val isTender = tenderId.isNotEmpty()
        val displayTitle = if (isSubscribed || !isTender) title else "¡Nueva Oportunidad Disponible!"
        val displayBody = if (isSubscribed || !isTender) body else "Hay una licitación para tus servicios. Hazte premium para participar."

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tenderId", if (isSubscribed) tenderId else "") 
            putExtra("chatId", chatId)
            putExtra("senderId", senderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // ─── FILTROS DE PREFERENCIAS (APP SETTINGS) ───────────────────────────────
        val msgType = message.data["type"] ?: ""
        val notifEnabled = runBlocking {
            when {
                chatId.isNotEmpty() && tenderId.isEmpty() -> appSettingsRepository.notifMessages.first()
                msgType == "presupuesto" -> appSettingsRepository.notifPresupuestos.first()
                msgType == "pedido" -> appSettingsRepository.notifPedidos.first()
                else -> true
            }
        }
        
        if (!notifEnabled) return

        // ─── CANALES DE NOTIFICACIÓN (UX ELITE) ──────────────────────────────────
        val channelId = if (isTender) "tender_notifications" else "chat_messages"
        val channelName = if (isTender) "Licitaciones y Concursos" else "Mensajes Directos"
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notificaciones críticas de negocio y comunicación"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: Usar un icono de silueta blanca
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        // ─── PERSISTENCIA LOCAL (LEY #2) ─────────────────────────────────────────
        jobScope.launch {
            try {
                notificacionRepository.guardar(
                    NotificacionItem(
                        tipo = if (isTender) TipoNotificacion.LICITACION else TipoNotificacion.MENSAJE,
                        titulo = displayTitle,
                        mensaje = displayBody,
                        accionRoute = if (isTender) tenderId else chatId 
                    )
                )
            } catch (e: Exception) {
                Log.e("FCM_SAVE", "Error persistiendo notificación: ${e.message}")
            }
        }
    }
}
