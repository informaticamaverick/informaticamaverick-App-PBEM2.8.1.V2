package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.core.servicios.notificaciones.Notificador
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
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

/**
 * --- FIREBASE MESSAGING SERVICE (APP CLIENTE v1.0) ---
 * Motor de notificaciones en tiempo real para el cliente.
 */
@AndroidEntryPoint
class AppMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificacionRepository: com.example.myapplication.core.datos.repositorios.NotificacionRepositorio

    @Inject
    lateinit var notificador: Notificador

    @Inject
    lateinit var chatRepository: ChatMotorSincRepositorio

    private val jobScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference.child("status").child(uid).child("fcmToken").setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        android.util.Log.d("FCM_APP", "📩 [PUSH_RECIBIDA] Payload: ${message.data}")

        val type = message.data["type"] ?: "message"
        val title = message.notification?.title ?: message.data["title"] ?: "Nueva Notificación"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val chatId = message.data["chatId"] ?: ""

        // [ELITE]: Delegar notificación al Core (Sincronismo Wake-up)
        if (type == "message" && chatId.isNotBlank()) {
            val senderName = message.data["nombreRemoto"] ?: title
            android.util.Log.d("FCM_APP", "💬 Despertando hilo de chat: $chatId")
            
            // 🔥 [BIG LEAGUE]: Sincronización silenciosa en background
            chatRepository.observarChat(chatId)
            
            notificador.mostrarAvisoChat(chatId, senderName, body)
        } else {
            notificador.mostrarAvisoGeneral(title, body, message.data["chatId"] ?: message.data["tenderId"], type)
        }

        jobScope.launch {
            try {
                notificacionRepository.insertar(
                    com.example.myapplication.core.datos.local.entidades.NotificacionEntity(
                        tipo = type.uppercase(),
                        titulo = title,
                        mensaje = body,
                        fechaMs = System.currentTimeMillis(),
                        leida = false,
                        rutaAccion = message.data["chatId"] ?: message.data["tenderId"] ?: ""
                    )
                )
            } catch (e: Exception) {
                Log.e("FCM_APP", "Error persistiendo notif: ${e.message}")
            }
        }
    }

    /*
    private fun showSystemNotification(remoteMessage: RemoteMessage) {
        val channelId = "app_alerts"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas del Sistema", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Notificación"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Novedades en tu cuenta"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // [ELITE v2026.6]: Deep Linking enriquecido para Presupuestos y Promociones
            putExtra("target_type", remoteMessage.data["type"])
            putExtra("tender_id", remoteMessage.data["tenderId"])

            // Mapeo de Identidades para ruteo de Chat
            putExtra("promoId", remoteMessage.data["idPromocion"])
            putExtra("providerId", remoteMessage.data["idPrestador"])
            putExtra("companyId", remoteMessage.data["idEmpresa"])
            putExtra("branchId", remoteMessage.data["idSucursal"])
            putExtra("nombrePrestador", remoteMessage.data["nombrePrestador"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
    */
}


































