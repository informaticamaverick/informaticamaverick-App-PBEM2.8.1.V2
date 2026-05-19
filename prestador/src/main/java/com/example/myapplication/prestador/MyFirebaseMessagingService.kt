package com.example.myapplication.prestador

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import com.example.myapplication.prestador.data.repository.AppSettingsRepository
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificacionRepository: NotificacionRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    private val jobScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("providers")
            .document(uid)
            .set(hashMapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d("FCM_RECEIVE", "Mensaje recibido de: ${message.from}")
        Log.d("FCM_RECEIVE", "Data payload: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "Nueva Licitación"
        val body = message.notification?.body ?: message.data["body"] ?: "Hay una nueva oportunidad en tu zona."
        val tenderId = message.data["tenderId"] ?: ""
        val chatId = message.data["chatId"] ?: ""
        val senderId = message.data["senderId"] ?: ""

        // ─── SECCIÓN: LÓGICA DE NOTIFICACIÓN PREMIUM (UPSWELL) ──────────────────────────
        // Verificamos si el prestador está suscripto para decidir qué mostrar.
        // Usamos una llamada simple para obtener el estado del prestador (Room como SSOT)
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        var isPremium = false
        
        if (userId != null) {
            // Nota: En un entorno de producción, inyectar el repositorio es mejor.
            // Para evitar problemas de inyección, creamos la DB y usamos runBlocking para llamadas suspend
            val db = androidx.room.Room.databaseBuilder(
                applicationContext,
                com.example.myapplication.prestador.data.local.database.PrestadorDatabase::class.java,
                "prestador-database"
            ).build()
            
            isPremium = kotlinx.coroutines.runBlocking {
                db.providerDao().getProviderByIdOnce(userId)?.isSubscribed ?: false
            }
        }

        val displayTitle = if (isPremium) title else "¡Nuevas Oportunidades!"
        val displayBody = if (isPremium) body else "Hay Licitaciones o Concursos públicos para tus servicios. Hazte premium para poder participar."

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tenderId", if (isPremium) tenderId else "") // Solo enviamos ID si es premium
            putExtra("chatId", chatId)
            putExtra("senderId", senderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        // ──────────────────────────────────────────────────────────────────────────────

        val msgType = message.data["type"] ?: ""
        val notifEnabled = kotlinx.coroutines.runBlocking {
            when {
                chatId.isNotEmpty() && tenderId.isEmpty() ->
                    appSettingsRepository.notifMessages.first()

                msgType == "presupuesto" ->
                    appSettingsRepository.notifPresupuestos.first()

                msgType == "pedido" ->
                    appSettingsRepository.notifPedidos.first()

                else -> true
            }
        }
        if (!notifEnabled) return

        val channelId = if (tenderId.isNotEmpty()) "tender_notifications" else "chat_messages"
        val channelName = if (tenderId.isNotEmpty()) "Licitaciones" else "Mensajes de chat"
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        // ─── SECCIÓN: PERSISTENCIA LOCAL (NOTIFICACIONES) ──────────────────────────
        // Guardamos la notificación en la base de datos local para que aparezca 
        // en la pantalla de alertas/notificaciones del usuario.
        jobScope.launch {
            notificacionRepository.guardar(
                NotificacionItem(
                    tipo = if (tenderId.isNotEmpty()) TipoNotificacion.LICITACION else TipoNotificacion.MENSAJE,
                    titulo = displayTitle,
                    mensaje = displayBody,
                    accionRoute = tenderId.ifEmpty { chatId } 
                )
            )
        }
    }
}