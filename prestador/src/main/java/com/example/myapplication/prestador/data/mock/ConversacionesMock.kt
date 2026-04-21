/**

package com.example.myapplication.prestador.data.mock

import com.example.myapplication.prestador.data.model.Message
import java.util.UUID

/**
 * Conversaciones mock realistas con clientes
 * Incluye diferentes tipos de mensajes y propuestas de citas
 */
object ConversacionesMock {
    
    // Almacenamiento mutable de mensajes por cliente
    private val conversacionesMutables = mutableMapOf<String, MutableList<Message>>()
    
    /**
     * Obtiene mensajes de conversación para un cliente específico
     */
    fun obtenerMensajes(clienteId: String): List<Message> {
        // Si ya existe una conversación mutable, devolverla
        if (conversacionesMutables.containsKey(clienteId)) {
            return conversacionesMutables[clienteId]!!
        }
        
        // Si no, generar la conversación inicial
        val mensajesIniciales = when(clienteId) {
            "cliente_001" -> conversacionMariaGonzalez()
            "cliente_002" -> conversacionCarlosRodriguez()
            "cliente_003" -> conversacionAnaLopez()
            "cliente_004" -> conversacionJuanPerez()
            "cliente_005" -> conversacionLauraMartinez()
            "cliente_006" -> conversacionRobertoDiaz()
            "cliente_007" -> conversacionValeriaSanchez()
            "cliente_008" -> conversacionDiegoFernandez()
            "cliente_009" -> conversacionSofiaRomero()
            "cliente_010" -> conversacionMiguelTorres()
            // Para clientes 11-20, generar conversaciones genéricas con su ID
            else -> conversacionGenerica(clienteId)
        }
        
        // Guardar en el mapa mutable
        conversacionesMutables[clienteId] = mensajesIniciales.toMutableList()
        
        return conversacionesMutables[clienteId]!!
    }
    
    // Cliente VIP - María González (múltiples citas)
    private fun conversacionMariaGonzalez() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola! Necesito programar el mantenimiento mensual 😊",
            timestamp = System.currentTimeMillis() - 7200000, // Hace 2 horas
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola María! Por supuesto. ¿Qué día te viene mejor?",
            timestamp = System.currentTimeMillis() - 7000000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "¿Tenés disponible el viernes por la tarde?",
            timestamp = System.currentTimeMillis() - 6800000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Perfecto! Te propongo este horario:",
            timestamp = System.currentTimeMillis() - 6700000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis() - 6600000,
            isFromCurrentUser = true,
            type = Message.MessageType.APPOINTMENT,
            appointmentTitle = "Revisión instalación eléctrica",
            appointmentDate = "2026-02-21",
            appointmentTime = "15:00",
            appointmentId = "apt_maria_001",
            appointmentStatus = Message.AppointmentProposalStatus.PENDING
        )
    )
    
    // Carlos Rodríguez - Cita aceptada
    private fun conversacionCarlosRodriguez() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Buenos días! Consulta por un turno para la semana que viene",
            timestamp = System.currentTimeMillis() - 86400000, // Hace 1 día
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Buen día Carlos! Sí, tengo disponibilidad. ¿Preferís mañana o tarde?",
            timestamp = System.currentTimeMillis() - 86100000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Por la mañana mejor, tipo 10 o 11",
            timestamp = System.currentTimeMillis() - 85800000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Perfecto! Te confirmo para el lunes:",
            timestamp = System.currentTimeMillis() - 85500000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis() - 85200000,
            isFromCurrentUser = true,
            type = Message.MessageType.APPOINTMENT,
            appointmentTitle = "Reparación de plomería",
            appointmentDate = "2026-02-24",
            appointmentTime = "10:00",
            appointmentId = "apt_carlos_001",
            appointmentStatus = Message.AppointmentProposalStatus.ACCEPTED
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Perfecto!! Confirmo 👍 Nos vemos el lunes",
            timestamp = System.currentTimeMillis() - 85000000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        )
    )
    
    // Ana López - Cita rechazada
    private fun conversacionAnaLopez() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola! Quería sacar turno para esta semana si es posible",
            timestamp = System.currentTimeMillis() - 3600000, // Hace 1 hora
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola Ana! Sí, tengo un espacio el miércoles. Te va bien?",
            timestamp = System.currentTimeMillis() - 3500000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Sí, genial!",
            timestamp = System.currentTimeMillis() - 3400000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Te propongo este horario:",
            timestamp = System.currentTimeMillis() - 3300000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis() - 3200000,
            isFromCurrentUser = true,
            type = Message.MessageType.APPOINTMENT,
            appointmentTitle = "Reparación de cañería",
            appointmentDate = "2026-02-22",
            appointmentTime = "14:00",
            appointmentId = "apt_ana_001",
            appointmentStatus = Message.AppointmentProposalStatus.REJECTED,
            rejectionReason = "No puedo a esa hora, tengo una reunión de trabajo 😔"
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Ay no 😔 justo tengo una reunión de trabajo a esa hora. Tenés algo más temprano?",
            timestamp = System.currentTimeMillis() - 3100000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        )
    )
    
    // Juan Pérez - Conversación casual
    private fun conversacionJuanPerez() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Buenas! ¿Cuánto sale una revisión eléctrica?",
            timestamp = System.currentTimeMillis() - 1800000, // Hace 30 min
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola Juan! La revisión eléctrica sale $8000. Incluye diagnóstico completo.",
            timestamp = System.currentTimeMillis() - 1700000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Dale, me sirve. Tenés para mañana?",
            timestamp = System.currentTimeMillis() - 1600000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Sí! Mañana tengo disponible a las 11:00. Te va bien?",
            timestamp = System.currentTimeMillis() - 1500000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Te propongo instalación de aire acondicionado para el 27 de febrero a las 11:30",
            timestamp = System.currentTimeMillis() - 1400000,
            isFromCurrentUser = true,
            type = Message.MessageType.APPOINTMENT,
            appointmentTitle = "Instalación de aire acondicionado",
            appointmentDate = "2026-02-27",
            appointmentTime = "11:30",
            appointmentId = "apt_juan_001",
            appointmentStatus = Message.AppointmentProposalStatus.ACCEPTED
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Perfecto! Ahí estaré 👍",
            timestamp = System.currentTimeMillis() - 1300000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        )
    )
    
    // Laura Martínez - Con emojis
    private fun conversacionLauraMartinez() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Holaaaa! 👋✨",
            timestamp = System.currentTimeMillis() - 5400000, // Hace 1.5 horas
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Tengo un problema con la instalación eléctrica del garage 🔧",
            timestamp = System.currentTimeMillis() - 5300000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola Laura! 😊 Contame qué problema tenés.",
            timestamp = System.currentTimeMillis() - 5200000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Quiero un cambio radical, me animé a renovar todo el sistema 🤩",
            timestamp = System.currentTimeMillis() - 5100000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Entendido! Vamos a necesitar al menos 3 horas. Te propongo el sábado:",
            timestamp = System.currentTimeMillis() - 5000000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis() - 4900000,
            isFromCurrentUser = true,
            type = Message.MessageType.APPOINTMENT,
            appointmentTitle = "Revisión completa de instalaciones",
            appointmentDate = "2026-02-22",
            appointmentTime = "10:00",
            appointmentId = "apt_laura_001",
            appointmentStatus = Message.AppointmentProposalStatus.PENDING
        )
    )
    
    // Roberto Díaz - Cliente empresa
    private fun conversacionRobertoDiaz() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Buenas tardes. Soy de ACME Corp. Necesitamos coordinar el servicio para nuestro evento corporativo del mes que viene.",
            timestamp = System.currentTimeMillis() - 172800000, // Hace 2 días
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Buenas tardes Roberto! Perfecto, con gusto. ¿Cuántas personas serían?",
            timestamp = System.currentTimeMillis() - 172600000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Aproximadamente 15 personas. El evento es el 15 de marzo.",
            timestamp = System.currentTimeMillis() - 172400000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        )
    )
    
    // Valeria Sánchez - Primera vez
    private fun conversacionValeriaSanchez() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola! Me recomendaron tus servicios 😊",
            timestamp = System.currentTimeMillis() - 10800000, // Hace 3 horas
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola Valeria! Muchas gracias por la recomendación! ¿En qué te puedo ayudar?",
            timestamp = System.currentTimeMillis() - 10700000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Necesito que revisen la instalación de gas. Sería para la próxima semana",
            timestamp = System.currentTimeMillis() - 10600000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        )
    )
    
    // Diego Fernández - Consulta rápida
    private fun conversacionDiegoFernandez() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola, trabajas los domingos?",
            timestamp = System.currentTimeMillis() - 600000, // Hace 10 min
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola Diego! Sí, domingos de 10 a 15hs. Necesitás turno?",
            timestamp = System.currentTimeMillis() - 500000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        )
    )
    
    // Sofía Romero - Cliente regular
    private fun conversacionSofiaRomero() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola! Como siempre, necesito mi turno quincenal jaja",
            timestamp = System.currentTimeMillis() - 43200000, // Hace 12 horas
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola Sofía! Jaja ya sabía que me ibas a escribir 😄 Jueves a las 16?",
            timestamp = System.currentTimeMillis() - 43100000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Perfecto!! Ya lo anoto en mi agenda ✅",
            timestamp = System.currentTimeMillis() - 43000000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Genial! Te mando la confirmación:",
            timestamp = System.currentTimeMillis() - 42900000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis() - 42800000,
            isFromCurrentUser = true,
            type = Message.MessageType.APPOINTMENT,
            appointmentTitle = "Consulta de seguimiento",
            appointmentDate = "2026-02-20",
            appointmentTime = "16:00",
            appointmentId = "apt_sofia_001",
            appointmentStatus = Message.AppointmentProposalStatus.ACCEPTED
        )
    )
    
    // Miguel Torres - Consulta con ubicación
    private fun conversacionMiguelTorres() = listOf(
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola! Donde queda el local?",
            timestamp = System.currentTimeMillis() - 900000, // Hace 15 min
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Hola Miguel! Te mando la ubicación:",
            timestamp = System.currentTimeMillis() - 850000,
            isFromCurrentUser = true,
            type = Message.MessageType.TEXT
        ),
        Message(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis() - 800000,
            isFromCurrentUser = true,
            type = Message.MessageType.LOCATION,
            latitude = -34.603722,
            longitude = -58.381592
        ),
        Message(
            id = UUID.randomUUID().toString(),
            text = "Ah perfecto, queda cerca de mi trabajo! Voy a sacar turno",
            timestamp = System.currentTimeMillis() - 750000,
            isFromCurrentUser = false,
            type = Message.MessageType.TEXT
        )
    )
    
    // Conversación default para clientes sin conversación definida
    // Conversación genérica para clientes sin conversación específica
    private fun conversacionGenerica(clienteId: String): List<Message> {
        val mensajesIniciales = listOf(
            "Buenos días, me gustaría agendar una cita",
            "Hola! Me recomendaron sus servicios",
            "¿Tiene disponibilidad para esta semana?",
            "Buenas tardes, necesito su servicio",
            "Hola, quisiera información sobre sus tarifas",
            "Me interesa contratar sus servicios",
            "¿Atiende a domicilio?",
            "Necesito agendar con urgencia",
            "Hola! ¿Cuál es su horario de atención?",
            "Buenos días, ¿trabaja los fines de semana?"
        )
        
        val respuestasPrestador = listOf(
            "¡Hola! Claro, con gusto. ¿Para cuándo necesitas?",
            "¡Bienvenido/a! Sí, tengo disponibilidad. ¿Qué día prefieres?",
            "Hola! Por supuesto, déjame revisar mi agenda",
            "¡Buenos días! Sí, puedo atenderte. ¿Qué servicio necesitas?",
            "Hola! Con mucho gusto te ayudo. Cuéntame más detalles",
            "¡Hola! Claro, te envío la información",
            "Buenos días! Sí, sin problema. ¿Para qué fecha?",
            "Hola! Perfecto, puedo ayudarte con eso"
        )
        
        // Generar conversación simple con 2-3 mensajes
        val numMensajes = (2..3).random()
        val mensajesGenerados = mutableListOf<Message>()
        
        // Primer mensaje del cliente
        mensajesGenerados.add(
            Message(
                id = "msg_${clienteId}_1",
                text = mensajesIniciales.random(),
                timestamp = System.currentTimeMillis() - (3600000 * 2), // Hace 2 horas
                isFromCurrentUser = false,
                type = Message.MessageType.TEXT
            )
        )
        
        // Respuesta del prestador
        if (numMensajes >= 2) {
            mensajesGenerados.add(
                Message(
                    id = "msg_${clienteId}_2",
                    text = respuestasPrestador.random(),
                    timestamp = System.currentTimeMillis() - (3600000), // Hace 1 hora
                    isFromCurrentUser = true,
                    type = Message.MessageType.TEXT
                )
            )
        }
        
        // Mensaje adicional del cliente si corresponde
        if (numMensajes >= 3) {
            val respuestasCliente = listOf(
                "Perfecto, gracias!",
                "Excelente, le escribo luego",
                "Ok, lo confirmo pronto",
                "Muchas gracias por la información",
                "Perfecto, entonces acordamos",
                "Genial, nos vemos!"
            )
            
            mensajesGenerados.add(
                Message(
                    id = "msg_${clienteId}_3",
                    text = respuestasCliente.random(),
                    timestamp = System.currentTimeMillis() - (1800000), // Hace 30 min
                    isFromCurrentUser = false,
                    type = Message.MessageType.TEXT
                )
            )
        }
        
        return mensajesGenerados
    }
    
    /**
     * Agrega un mensaje a la conversación de un cliente específico
     */
    fun agregarMensaje(clienteId: String, message: Message) {
        // Si no existe la conversación, inicializarla con los mensajes existentes
        if (!conversacionesMutables.containsKey(clienteId)) {
            conversacionesMutables[clienteId] = obtenerMensajes(clienteId).toMutableList()
        }
        
        // Agregar el nuevo mensaje
        conversacionesMutables[clienteId]?.add(message)
    }
    
    /**
     * Actualiza una propuesta de cita existente con nueva fecha/hora y vuelve a PENDING
     * Usado para reprogramación de citas
     */
    fun actualizarPropuestaCita(
        clienteId: String,
        appointmentId: String,
        nuevaFecha: String,
        nuevaHora: String
    ) {
        println("🔄 actualizarPropuestaCita llamada:")
        println("  ClientId: $clienteId")
        println("  AppointmentId: $appointmentId")
        println("  Nueva fecha: $nuevaFecha")
        println("  Nueva hora: $nuevaHora")
        
        if (!conversacionesMutables.containsKey(clienteId)) {
            println("  📝 Inicializando conversacionesMutables para $clienteId")
            conversacionesMutables[clienteId] = obtenerMensajes(clienteId).toMutableList()
        }
        
        conversacionesMutables[clienteId]?.let { mensajes ->
            println("  📊 Total mensajes: ${mensajes.size}")
            
            // Debug: mostrar todos los appointmentIds
            mensajes.filter { it.type == Message.MessageType.APPOINTMENT }.forEach {
                println("    - Mensaje con appointmentId: ${it.appointmentId}, status: ${it.appointmentStatus}")
            }
            
            val index = mensajes.indexOfFirst { it.appointmentId == appointmentId }
            
            if (index != -1) {
                val mensajeOriginal = mensajes[index]
                println("  ✅ Mensaje encontrado en índice: $index")
                println("    Fecha anterior: ${mensajeOriginal.appointmentDate} ${mensajeOriginal.appointmentTime}")
                println("    Estado anterior: ${mensajeOriginal.appointmentStatus}")
                
                val mensajeActualizado = mensajeOriginal.copy(
                    appointmentDate = nuevaFecha,
                    appointmentTime = nuevaHora,
                    appointmentStatus = Message.AppointmentProposalStatus.PENDING,
                    rejectionReason = null,  // Limpiar motivo de rechazo anterior
                    timestamp = System.currentTimeMillis(),  // Actualizar timestamp
                    text = "Te propongo reprogramar la cita:"
                )
                mensajes[index] = mensajeActualizado
                println("  ✅ Propuesta actualizada: $appointmentId -> $nuevaFecha $nuevaHora (PENDING)")
                println("    Nuevo timestamp: ${mensajeActualizado.timestamp}")
            } else {
                println("  ⚠️ No se encontró mensaje con appointmentId: $appointmentId")
            }
        }
    }
    
    /**
     * Actualiza el estado de un mensaje de propuesta de cita
     */
    fun actualizarEstadoPropuesta(
        clienteId: String,
        appointmentId: String,
        nuevoEstado: Message.AppointmentProposalStatus,
        motivoRechazo: String? = null
    ) {
        if (!conversacionesMutables.containsKey(clienteId)) {
            conversacionesMutables[clienteId] = obtenerMensajes(clienteId).toMutableList()
        }
        
        conversacionesMutables[clienteId]?.let { mensajes ->
            // 🔍 Buscar el mensaje con este appointmentId que esté PENDING
            val index = mensajes.indexOfFirst { 
                it.appointmentId == appointmentId && 
                it.appointmentStatus == Message.AppointmentProposalStatus.PENDING 
            }
            
            if (index != -1) {
                val mensajeOriginal = mensajes[index]
                val mensajeActualizado = mensajeOriginal.copy(
                    appointmentStatus = nuevoEstado,
                    rejectionReason = motivoRechazo
                )
                mensajes[index] = mensajeActualizado
                println("✅ Estado actualizado: ${mensajeOriginal.id} -> $nuevoEstado")
            } else {
                println("⚠️ No se encontró mensaje PENDING con appointmentId: $appointmentId")
            }
        }
    }
}
**/