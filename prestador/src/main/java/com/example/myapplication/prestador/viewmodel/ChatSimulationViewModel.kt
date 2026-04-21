/**

package com.example.myapplication.prestador.viewmodel

import android.app.Application
import androidx.compose.runtime.saveable.listSaver
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.model.Message
//import com.example.myapplication.prestador.data.mock.ClientesMockData
import com.example.myapplication.prestador.data.repository.AppointmentRepository
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.prestador.utils.NotificationHelper
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.StringCharacterIterator
import javax.inject.Inject

@HiltViewModel
class ChatSimulationViewModel @Inject constructor(
    application: Application,
    private val appointmentRepository: AppointmentRepository,
    private val presupuestoRepository: PresupuestoRepository,
    private val providerRepository: ProviderRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val notificationHelper = NotificationHelper(context)
    private val processedMessageTimestamps = mutableMapOf<String, Long>()

    private val _serviceType = MutableStateFlow("TECHNICAL")
    val serviceType: StateFlow<String> = _serviceType.asStateFlow()

    private val _pendingOpenUserId = MutableStateFlow<String?>(null)
    val pendingOpenUserId: StateFlow<String?> = _pendingOpenUserId.asStateFlow()

    fun setPendingOpenUsersId(userId: String?) {
        _pendingOpenUserId.value = userId
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        println("🔐 ChatSimulation - Auth state changed: uid=${auth.currentUser?.uid}")
        cargarServiceType()
    }

    init {
        println("🚀 ChatSimulationViewModel CREADO")
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    private fun cargarServiceType() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            iniciarSimulacion("TECHNICAL")
            return
        }
        viewModelScope.launch {
            try {
                // Leer desde Firestore primero para tener el dato correcto del usuario
                val doc = FirebaseFirestore.getInstance()
                    .collection("providers").document(uid).get().await()
                val tipo = doc.getString("serviceType") ?: "TECHNICAL"
                _serviceType.value = tipo
                println("✅ ChatSimulation - serviceType cargado desde Firestore: $tipo")
                iniciarSimulacion(tipo)
            } catch (e: Exception) {
                // Fallback a Room si Firestore falla
                providerRepository.getProviderById(uid).collect { provider ->
                    val tipo = provider?.serviceType ?: "TECHNICAL"
                    _serviceType.value = tipo
                    println("✅ ChatSimulation - serviceType cargado desde Room: $tipo")
                    iniciarSimulacion(tipo)
                }
            }
        }
    }

    private fun iniciarSimulacion(serviceType: String) {
        startAutoResponseSimulation(serviceType)
        startSpontaneousMessages(serviceType)
    }

    private fun startAutoResponseSimulation(serviceType: String) {
        viewModelScope.launch {
            println("✅ Auto-respuesta iniciada para serviceType: $serviceType")
            while (true) {
                delay(2000)
                try {
                    val clientesFiltrados = ClientesMockData.clientes
                        .filter { it.serviceType == serviceType }

                    clientesFiltrados.forEach { cliente ->
                        val userId = cliente.id
                        val messages = ChatData.getMessagesForUser(userId)
                        messages.forEach { message ->
                            if (message.type == Message.MessageType.APPOINTMENT &&
                                message.appointmentStatus == Message.AppointmentProposalStatus.PENDING &&
                                message.appointmentId != null
                            ) {
                                synchronized(processedMessageTimestamps) {
                                    val lastProcessedTime = processedMessageTimestamps[message.id]
                                    if (lastProcessedTime == null || lastProcessedTime != message.timestamp) {
                                        processedMessageTimestamps[message.id] = message.timestamp
                                        println("📅 PROPUESTA: ${message.appointmentId} de ${cliente.nombre}")
                                        viewModelScope.launch {
                                            processAppointmentProposal(userId, cliente.nombreCompleto, message)
                                        }
                                    }
                                }
                            }

                            // Detectar cuando el prestador envía un presupuesto manualmente
                            if (message.type == Message.MessageType.BUDGET &&
                                message.isFromCurrentUser &&
                                message.budgetTotal != null
                            ) {
                                synchronized(processedMessageTimestamps) {
                                    val lastProcessedTime = processedMessageTimestamps[message.id]
                                    if (lastProcessedTime == null || lastProcessedTime != message.timestamp) {
                                        processedMessageTimestamps[message.id] = message.timestamp
                                        println("💰 PRESUPUESTO enviado al cliente ${cliente.nombre}")
                                        viewModelScope.launch {
                                            procesarPresupuestoEnviado(userId, cliente.nombreCompleto, message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Error en simulación: ${e.message}")
                }
            }
        }
    }

    /**
     * Procesa una propuesta de cita específica
     */
    private suspend fun processAppointmentProposal(
        userId: String,
        userName: String,
        message: Message
    ) {
        try {
            println("🚀 INICIANDO processAppointmentProposal para ${message.appointmentId}")
            val waitTime = (5000..10000).random().toLong()
            println("⏱️ Esperando ${waitTime}ms antes de responder a ${message.appointmentId}...")
            delay(waitTime)

            val accepted = (0..1).random() == 1
            println("🎲 Cliente ${if (accepted) "ACEPTA ✅" else "RECHAZA ❌"} - AppointmentId: ${message.appointmentId}")

            if (accepted) {
                // ✅ ACEPTAR CITA
                println("✅ Cliente $userId ACEPTA la cita ${message.appointmentId}")

                // 🎯 USAR NUEVO MANAGER INMUTABLE
                AppointmentRescheduleManager.updateAppointmentStatus(
                    clientId = userId,
                    appointmentId = message.appointmentId!!,
                    newStatus = Message.AppointmentProposalStatus.ACCEPTED
                )

                val acceptMessage = Message(
                    id = "msg_${System.currentTimeMillis()}_${userId}",
                    text = listOf(
                        "¡Perfecto! Confirmo la cita para ${message.appointmentDate} a las ${message.appointmentTime} ✅",
                        "Confirmo la cita, nos vemos el ${message.appointmentDate} 👍",
                        "Me parece bien, aceptada la cita para ${message.appointmentDate} a las ${message.appointmentTime}",
                        "¡Listo! Nos vemos el ${message.appointmentDate} a las ${message.appointmentTime} ✓"
                    ).random(),
                    isFromCurrentUser = false,
                    timestamp = System.currentTimeMillis(),
                    type = Message.MessageType.TEXT
                )
                ChatData.addMessageToUser(userId, acceptMessage)

                // 💾 GUARDAR/ACTUALIZAR CITA
                println("💾 Guardando/Actualizando cita en base de datos...")
                println("💾 appointmentId: ${message.appointmentId}")
                println("💾 appointmentDate: ${message.appointmentDate}")
                println("💾 appointmentTime: ${message.appointmentTime}")
                println("💾 appointmentTitle: ${message.appointmentTitle}")

                val providerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                println("💾 providerId: $providerId")

                // Verificar si la cita ya existe para mantener createdAt original
                val existingAppointment = try {
                    appointmentRepository.getAppointmentByIdSync(message.appointmentId!!)
                } catch (e: Exception) {
                    println("💾 No existe cita previa (es nueva): ${e.message}")
                    null
                }

                val createdTimestamp = existingAppointment?.createdAt ?: System.currentTimeMillis()
                val isUpdate = existingAppointment != null

                println("💾 ${if (isUpdate) "ACTUALIZANDO" else "CREANDO"} cita: ${message.appointmentId}")
                if (isUpdate) {
                    println("💾 Fecha original: ${existingAppointment?.date} ${existingAppointment?.time}")
                    println("💾 Nueva fecha: ${message.appointmentDate} ${message.appointmentTime}")
                }

                val appointmentEntity = AppointmentEntity(
                    id = message.appointmentId,
                    clientId = userId,
                    clientName = userName,
                    providerId = providerId,
                    service = message.appointmentTitle ?: existingAppointment?.service ?: "Servicio técnico",
                    date = message.appointmentDate ?: "",
                    time = message.appointmentTime ?: "",
                    duration = existingAppointment?.duration ?: 60,
                    status = "confirmed",
                    notes = if (isUpdate) "Cita reprogramada y confirmada por el cliente" else "Cita confirmada por el cliente",
                    proposedBy = "provider",
                    serviceType = existingAppointment?.serviceType ?: _serviceType.value,
                    createdAt = createdTimestamp,  // ✅ Mantener fecha de creación original
                    updatedAt = System.currentTimeMillis()  // ✅ Actualizar timestamp de modificación
                )

                println("💾 Intentando guardar cita...")
                println("💾 AppointmentEntity: id=${appointmentEntity.id}, date=${appointmentEntity.date}, time=${appointmentEntity.time}")

                try {
                    appointmentRepository.saveAppointment(appointmentEntity)
                    println("✅✅✅ Cita ${if (isUpdate) "ACTUALIZADA" else "GUARDADA"}: ${appointmentEntity.id}")
                    println("✅ Nueva fecha/hora: ${appointmentEntity.date} a las ${appointmentEntity.time}")
                    // Programar recordatorios 24h y 1h antes
                    com.example.myapplication.prestador.utils.AppointmentReminderScheduler.schedule(context, appointmentEntity)
                } catch (e: Exception) {
                    println("❌❌❌ ERROR guardando cita: ${e.message}")
                    e.printStackTrace()
                }

                // 📬 NOTIFICACIÓN (simulador desactivado)
                // notificationHelper.showAppointmentConfirmedNotification(
                //     context = context,
                //     clientName = userName,
                //     date = appointmentEntity.date,
                //     time = appointmentEntity.time
                // )

                // 🔄 CONTINUAR FLUJO: cliente pide presupuesto → prestador envía → cliente acepta → ubicación
                viewModelScope.launch {
                    simularFlujoPosAceptacion(userId, userName, appointmentEntity)
                }

            } else {
                // ❌ RECHAZAR CITA
                val motivoRechazo = listOf(
                    "disculpa, ese horario no me viene bien",
                    "no puedo en ese horario, ¿tienes otro disponible?",
                    "ese día tengo otro compromiso",
                    "¿podríamos cambiar la hora?"
                ).random()

                // 🎯 USAR NUEVO MANAGER INMUTABLE
                AppointmentRescheduleManager.updateAppointmentStatus(
                    clientId = userId,
                    appointmentId = message.appointmentId!!,
                    newStatus = Message.AppointmentProposalStatus.REJECTED
                )

                val rejectMessage = Message(
                    id = "msg_${System.currentTimeMillis()}_${userId}",
                    text = "Lo siento, $motivoRechazo 😕",
                    isFromCurrentUser = false,
                    timestamp = System.currentTimeMillis(),
                    type = Message.MessageType.TEXT
                )
                ChatData.addMessageToUser(userId, rejectMessage)

                // 💾 Marcar cita como cancelada en Room
                try {
                    appointmentRepository.updateAppointmentStatus(message.appointmentId!!, "cancelled")
                    com.example.myapplication.prestador.utils.AppointmentReminderScheduler.cancel(context, message.appointmentId!!)
                } catch (e: Exception) {
                    println("❌ Error actualizando estado cancelado: ${e.message}")
                }

                // notificationHelper.showMessageNotification(
                //     context = context,
                //     clientName = userName,
                //     message = "Rechazó la cita: $motivoRechazo"
                // )
            }

        } catch (e: Exception) {
            println("❌ Error procesando propuesta: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun startSpontaneousMessages(serviceType: String) {
        viewModelScope.launch {
            delay(10000)
            println("✅ Mensajes espontáneos iniciados para serviceType: $serviceType")

            val mensajesPorTipo = when (serviceType) {
                "TECHNICAL" -> listOf(
                    "Hola, tengo una pérdida de agua urgente",
                    "Buenos días, se me cortó la luz en casa",
                    "¿Podés venir a revisar el gas?",
                    "Necesito instalar un aire acondicionado",
                    "Se me rompió una cañería, es urgente",
                    "¿Cuánto sale revisar la instalación eléctrica?",
                    "Hola, ¿hacés reparaciones de electrodomésticos?",
                    "Necesito un plomero para mañana temprano"
                )
                "PROFESSIONAL" -> listOf(
                    "Hola, necesito una consulta con urgencia",
                    "Buenos días, ¿tiene turnos disponibles esta semana?",
                    "¿Cuánto dura la consulta inicial?",
                    "Necesito renovar mi consulta mensual",
                    "¿Trabaja con obras sociales?",
                    "Hola, me derivaron para una consulta",
                    "¿Tiene horarios por la tarde?",
                    "Necesito un certificado médico"
                )
                "RENTAL" -> listOf(
                    "Hola, quisiera ver disponibilidad del espacio",
                    "Buenos días, ¿el lugar tiene estacionamiento?",
                    "¿Cuántas personas entran en el salón?",
                    "Necesito alquilar para un evento corporativo",
                    "¿Incluye servicio de catering?",
                    "¿Tienen disponibilidad para el fin de semana?",
                    "Quiero ver el espacio antes de reservar",
                    "¿Cuál es el precio por hora?"
                )
                else -> listOf(
                    "Hola, ¿tienes disponibilidad esta semana?",
                    "Buenos días, necesito tu servicio",
                    "¿Cuánto cobras por una consulta?",
                    "Hola! Me recomendaron tu servicio"
                )
            }

            val clientesDelTipo = ClientesMockData.clientes.filter { it.serviceType == serviceType }

            while (true) {
                val waitTime = (120000..300000).random().toLong()
                delay(waitTime)
                try {
                    if (clientesDelTipo.isEmpty()) continue
                    val randomClient = clientesDelTipo.random()
                    val newMessage = Message(
                        id = "msg_spontaneous_${System.currentTimeMillis()}_${randomClient.id}",
                        text = mensajesPorTipo.random(),
                        isFromCurrentUser = false,
                        timestamp = System.currentTimeMillis(),
                        type = Message.MessageType.TEXT
                    )
                    if ((0..9).random() < 3) {
                        // 30% de chances: cliente pide cita
                        viewModelScope.launch {
                            simularPedidoCitaEspontaneo(randomClient.id, randomClient.nombreCompleto, serviceType)
                        }
                    } else {
                        //70% mensaje de texto normal
                        ChatData.addMessageToUser(randomClient.id, newMessage)
                        println("Espontáneo de ${newMessage.text}")
                        // notificationHelper.showMessageNotification(
                        //     context = context,
                        //     clientName = randomClient.nombre,
                        //     message = newMessage.text ?: "Nuevo mensaje"
                        // )
                    }
                } catch (e: Exception) {
                    println("❌ Error en mensaje espontáneo: ${e.message}")
                }
            }
        }
    }

    /**
     * Simula que un cliente pide una cita espontáneamente desde el chat
     */
    private suspend fun simularPedidoCitaEspontaneo(
        userId: String,
        userName: String,
        serviceType: String
    ) {
        try {
            val pedidoTexto = when (serviceType) {
                "TECHNICAL" -> listOf(
                    "Hola, ¿tenés disponibilidad esta semana para venir a revisar?",
                    "Nesecito coordinar una fecha para que vengas",
                    "¿cuándo podés venir?. Quiero agendar algo",

                )
                "PROFESSIONAL" -> listOf(
                    "¿Podría sacar un turno esta semana?",
                    "Hola, ¿tiene disponibilidad para una consulta?",
                    "Queria reservar un turno, ¿cuándo tiene?",
                    "¿Me puede dar un turno para la seman que viene?"
                )
                "RENTAL" -> listOf(
                    "Hola, ¿podríamos reservar el espacio?",
                    "¿Tiene disponibilidad para este fin de semana?",
                    "Quiero reservar una fecha, ¿hablamos?",
                    "¿Podemos coordinar la reserva del lugar?"
                )
                else -> listOf(
                    "Hola, ¿podemos agendar una cita?",
                    "¿Tenés disponibilidad para esta semana?",
                    "Queria coordirnar una fecha"
                )
            }.random()
            ChatData.addMessageToUser(userId, Message(
                id = "msg_pide_cita_${System.currentTimeMillis()}",
                text = pedidoTexto,
                isFromCurrentUser = false,
                timestamp = System.currentTimeMillis(),
                type = Message.MessageType.TEXT
            ))
            // notificationHelper.showMessageNotification(context, userName, pedidoTexto)
        } catch (e: Exception) {
            println("Error en pedido cita espontanéo: ${e.message}")
        }
    }

    /**
     * Simula el flujo completo post-aceptación:
     * cliente pide presupuesto → prestador lo envía → cliente acepta → cliente manda ubicación
     */
    private suspend fun simularFlujoPosAceptacion(
        userId: String,
        userName: String,
        cita: AppointmentEntity
    ) {
        try {
            // PASO 1: Cliente pide presupuesto
            delay((4000..7000).random().toLong())
            val pedidoMsg = listOf(
                "¿Me podrías enviar un presupuesto antes de la cita?",
                "¿Cuánto me saldría el trabajo? Me gustaría tener el presupuesto",
                "Perfecto, ¿podrías mandarme un presupuesto estimado?",
                "Antes de confirmar, ¿me enviás el presupuesto?"
            ).random()
            ChatData.addMessageToUser(userId, Message(
                id = "msg_pide_pres_${System.currentTimeMillis()}",
                text = pedidoMsg,
                isFromCurrentUser = false,
                timestamp = System.currentTimeMillis(),
                type = Message.MessageType.TEXT
            ))
            // notificationHelper.showMessageNotification(context, userName, pedidoMsg)

            // El prestador envía el presupuesto manualmente desde la UI
            // El simulador detecta el mensaje BUDGET en startAutoResponseSimulation y reacciona

        } catch (e: Exception) {
            println("❌ Error en flujo post-aceptación: ${e.message}")
        }
    }

    /**
     * Reacciona cuando el prestador envía un presupuesto manualmente:
     * cliente acepta → manda ubicación
     */
    private suspend fun procesarPresupuestoEnviado(
        userId: String,
        userName: String,
        message: Message
    ) {
        try {
            // Cliente acepta el presupuesto
            delay((5000..9000).random().toLong())
            val aceptaMsg = listOf(
                "Perfecto, acepto el presupuesto 👍",
                "Está bien ese precio, lo acepto ✅",
                "De acuerdo, aprobado el presupuesto",
                "Me parece bien, aceptado!"
            ).random()
            ChatData.addMessageToUser(userId, Message(
                id = "msg_acepta_pres_${System.currentTimeMillis()}",
                text = aceptaMsg,
                isFromCurrentUser = false,
                timestamp = System.currentTimeMillis(),
                type = Message.MessageType.TEXT
            ))
            // notificationHelper.showMessageNotification(context, userName, aceptaMsg)
            // notificationHelper.showPresupuestoAceptadoNotification(userName, message.budgetTotal ?: 0.0)

            // Programar auto-eliminación del presupuesto en 5 días
            com.example.myapplication.prestador.utils.PresupuestoCleanupScheduler
                .scheduleDelete(context, message.id)

            // Actualizar estado en BD si tenemos el id
            // (el estado también se puede actualizar manualmente desde la pantalla de presupuestos)
            try {
                presupuestoRepository.updateEstado(message.id, "Aceptado")
            } catch (e: Exception) {
                println("⚠️ No se pudo actualizar estado presupuesto: ${e.message}")
            }

            // Cliente manda su ubicación
            delay((3000..6000).random().toLong())
            val ubicaciones = listOf(
                Triple(-26.8241, -65.2226, "Av. Mate de Luna 1234, San Miguel de Tucuman"),
                Triple(-26.8083, -65.2176, "Av. Alem 500, Tucuman"),
                Triple(-26.8167, -65.2000, "Calle Corrientes 890, Tucuman"),
                Triple(-26.8300, -65.2100, "Av. Belgrano 456, Tucuman")
            ).random()
            ChatData.addMessageToUser(userId, Message(
                id = "msg_ubic_${System.currentTimeMillis()}",
                text = "Te comparto mi ubicación: ${ubicaciones.third}",
                isFromCurrentUser = false,
                timestamp = System.currentTimeMillis(),
                type = Message.MessageType.LOCATION,
                latitude = ubicaciones.first,
                longitude = ubicaciones.second
            ))
            // notificationHelper.showMessageNotification(context, userName, "📍 ${ubicaciones.third}")

        } catch (e: Exception) {
            println("❌ Error procesando presupuesto enviado: ${e.message}")
        }
    }
    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
        println("🔴 ChatSimulationViewModel DESTRUIDO")
    }
}
**/