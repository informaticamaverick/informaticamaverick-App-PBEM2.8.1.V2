package com.example.myapplication.prestador

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.core.datos.local.dao.CuentaDao
import com.example.myapplication.core.datos.repositorios.NotificacionRepositorio
import com.example.myapplication.core.dominio.modelos.ElementoNotificacion
import com.example.myapplication.core.dominio.modelos.TipoNotificacion
import com.example.myapplication.core.datos.local.entidades.NotificacionEntity
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.servicios.notificaciones.Notificador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AppMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var cuentaDao: CuentaDao

    @Inject
    lateinit var notificacionRepository: NotificacionRepositorio

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
        android.util.Log.d("FCM_PRO", "📩 [PUSH_RECIBIDA] Payload: ${message.data}")

        val tenderId = message.data["tenderId"] ?: ""
        val title = message.notification?.title ?: message.data["title"] ?: "Nueva Notificación"
        val body = message.notification?.body ?: message.data["body"] ?: "Tienes una nueva actualización."
        val chatId = message.data["chatId"] ?: ""
        val type = message.data["type"] ?: (if (tenderId.isNotEmpty()) "LICITACION" else "message")

        val isSubscribed = runBlocking {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (uid.isBlank()) false
            else cuentaDao.obtenerPorIdSync(uid)?.estaSuscrito ?: false
        }

        val isTender = tenderId.isNotEmpty()
        val displayTitle = if (isSubscribed || !isTender) title else "¡Nueva Oportunidad Disponible!"
        val displayBody = if (isSubscribed || !isTender) body else "Hay una licitación para tus servicios. Hazte premium para participar."

        // [ELITE]: Delegar notificación al Core
        if (type == "message" && chatId.isNotBlank()) {
            android.util.Log.d("FCM_PRO", "💬 Despertando hilo de chat: $chatId")
            
            // 🔥 [BIG LEAGUE]: Sincronización silenciosa en background
            chatRepository.observarChat(chatId)
            
            notificador.mostrarAvisoChat(chatId, title, body)
        } else {
            notificador.mostrarAvisoGeneral(displayTitle, displayBody, tenderId.ifBlank { chatId }, type)
        }

        jobScope.launch {
            try {
                notificacionRepository.insertar(
                    NotificacionEntity(
                        tipo = if (isTender) TipoNotificacion.LICITACION.name else TipoNotificacion.MENSAJE.name,
                        titulo = displayTitle,
                        mensaje = displayBody,
                        fechaMs = System.currentTimeMillis(),
                        leida = false,
                        rutaAccion = if (isTender) "tender/$tenderId" else chatId
                    )
                )
            } catch (e: Exception) {
                Log.e("FCM_SAVE", "Error persistiendo notificación: ${e.message}")
            }
        }
    }
}
















































