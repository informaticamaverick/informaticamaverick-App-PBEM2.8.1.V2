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
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificacionRepository: NotificacionRepository

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

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tenderId", tenderId)
            putExtra("chatId", chatId)
            putExtra("senderId", senderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (tenderId.isNotEmpty()) "tender_notifications" else "chat_messages"
        val channelName = if (tenderId.isNotEmpty()) "Licitaciones" else "Mensajes de chat"
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        // 🔥 [NUEVO] Registrar la notificación en la base de datos local (Modo Alertas)
        if (tenderId.isNotEmpty()) {
            jobScope.launch {
                notificacionRepository.guardar(
                    NotificacionItem(
                        tipo = TipoNotificacion.LICITACION,
                        titulo = title,
                        mensaje = body,
                        accionRoute = tenderId // Guardamos el tenderId para abrirlo luego
                    )
                )
            }
        }
    }
}