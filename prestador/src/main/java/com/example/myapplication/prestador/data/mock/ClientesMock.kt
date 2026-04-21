

/**
package com.example.myapplication.prestador.data.mock

/**
 * Datos mock de clientes para pruebas de la app prestador
 * Modelo completo que soporta: Citas, Chat, Presupuestos, Dashboard
 */

data class ClienteMock(
    // ========== DATOS BÁSICOS ==========
    val id: String,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val direccion: String,
    val notas: String = "",
    
    // ========== PARA CHAT ==========
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val ultimaConexion: Long = System.currentTimeMillis(),
    val ultimoMensaje: String? = null,
    val ultimoMensajeTimestamp: Long = System.currentTimeMillis(),
    val mensajesNoLeidos: Int = 0,
    
    // ========== PARA PRESUPUESTOS ==========
    val tipoCliente: String = "PARTICULAR", // PARTICULAR, EMPRESA
    val cuit: String? = null,
    val razonSocial: String? = null,
    val condicionIVA: String = "CONSUMIDOR_FINAL", // CONSUMIDOR_FINAL, RESPONSABLE_INSCRIPTO, MONOTRIBUTISTA, EXENTO
    
    // ========== HISTORIAL Y ESTADÍSTICAS ==========
    val totalCitas: Int = 0,
    val citasCompletadas: Int = 0,
    val citasCanceladas: Int = 0,
    val totalPresupuestos: Int = 0,
    val presupuestosAprobados: Int = 0,
    val montoTotalGastado: Double = 0.0,
    val esClienteFrecuente: Boolean = false,
    val fechaPrimerContacto: Long = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
    
    // ========== PREFERENCIAS ==========
    val prefiereWhatsApp: Boolean = false,
    val prefiereEmail: Boolean = false,
    val prefiereLlamada: Boolean = false,
    val horarioPreferido: String? = null, // "mañana", "tarde", "noche", "flexible"
    val diasPreferidos: List<String> = emptyList(), // "lunes", "martes", etc.
    
    // ========== ESTADO Y CLASIFICACIÓN ==========
    val activo: Boolean = true,
    val bloqueado: Boolean = false,
    val esFavorito: Boolean = false,
    val esVIP: Boolean = false,
    val nivelConfianza: Int = 3, // 1-5 (1=nuevo, 5=muy confiable)
    val calificacionPromedio: Float = 0f, // 0-5 estrellas

    //===========TIPO DE SERVICIO ===========
    val serviceType: String = "TECHINICAL", // TECHNICAL, PROFESSIONAL, RENTAL
    
    // ========== METADATA ==========
    val createdAt: Long = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val nombreCompleto: String
        get() = "$nombre $apellido"
    
    val iniciales: String
        get() = "${nombre.firstOrNull()?.uppercase() ?: ""}${apellido.firstOrNull()?.uppercase() ?: ""}"
    
    val clienteDesde: String
        get() {
            val dias = (System.currentTimeMillis() - fechaPrimerContacto) / (1000 * 60 * 60 * 24)
            return when {
                dias < 30 -> "Nuevo"
                dias < 180 -> "${dias / 30} meses"
                else -> "${dias / 365} años"
            }
        }
}

object ClientesMockData {
    
    val clientes = listOf(
        // Cliente 1: VIP - Cliente frecuente con historial extenso
        ClienteMock(
            id = "cliente_001",
            nombre = "María",
            apellido = "González",
            telefono = "+54 9 11 2345-6789",
            email = "maria.gonzalez@email.com",
            direccion = "Av. Corrientes 1234, CABA",
            notas = "Cliente frecuente, prefiere turnos por la mañana",
            isOnline = true,
            ultimoMensaje = "Gracias por el excelente servicio!",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000),
            mensajesNoLeidos = 0,
            totalCitas = 15,
            citasCompletadas = 14,
            citasCanceladas = 1,
            totalPresupuestos = 8,
            presupuestosAprobados = 7,
            montoTotalGastado = 45000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "mañana",
            diasPreferidos = listOf("lunes", "miércoles", "viernes"),
            esFavorito = true,
            esVIP = true,
            nivelConfianza = 5,
            calificacionPromedio = 4.8f,
            serviceType = "TECHNICAL"
        ),
        
        // Cliente 2: Nuevo - Primera interacción
        ClienteMock(
            id = "cliente_002",
            nombre = "Juan",
            apellido = "Pérez",
            telefono = "+54 9 11 3456-7890",
            email = "juan.perez@email.com",
            direccion = "Calle Falsa 567, CABA",
            notas = "Primera vez",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (24 * 60 * 60 * 1000),
            ultimoMensaje = "¿Cuánto sale una revisión?",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (5 * 60 * 60 * 1000),
            mensajesNoLeidos = 1,
            totalCitas = 1,
            citasCompletadas = 0,
            prefiereWhatsApp = false,
            prefiereLlamada = true,
            horarioPreferido = "tarde",
            nivelConfianza = 1,
            fechaPrimerContacto = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
            serviceType = "TECHNICAL"
        ),
        
        // Cliente 3: Empresa - Con CUIT
        ClienteMock(
            id = "cliente_003",
            nombre = "Ana",
            apellido = "Martínez",
            telefono = "+54 9 11 4567-8901",
            email = "ana.martinez@email.com",
            direccion = "San Martín 890, Vicente López",
            notas = "Alergias: polvo",
            isOnline = true,
            ultimoMensaje = "Necesito presupuesto para la oficina",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (30 * 60 * 1000),
            mensajesNoLeidos = 2,
            tipoCliente = "EMPRESA",
            cuit = "30-71234567-8",
            razonSocial = "Martinez & Asociados SRL",
            condicionIVA = "RESPONSABLE_INSCRIPTO",
            totalCitas = 6,
            citasCompletadas = 5,
            totalPresupuestos = 4,
            presupuestosAprobados = 3,
            montoTotalGastado = 35000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000),
            prefiereEmail = true,
            horarioPreferido = "flexible",
            nivelConfianza = 4,
            calificacionPromedio = 4.5f,
            serviceType = "TECHNICAL"
        ),
        
        // Cliente 4: Regular - Buen historial
        ClienteMock(
            id = "cliente_004",
            nombre = "Carlos",
            apellido = "Rodríguez",
            telefono = "+54 9 11 5678-9012",
            email = "carlos.rodriguez@email.com",
            direccion = "Belgrano 321, Olivos",
            notas = "Pago en efectivo",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (48 * 60 * 60 * 1000),
            ultimoMensaje = "Perfecto, nos vemos el martes",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (12 * 60 * 60 * 1000),
            totalCitas = 8,
            citasCompletadas = 7,
            citasCanceladas = 1,
            totalPresupuestos = 5,
            presupuestosAprobados = 4,
            montoTotalGastado = 28000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (240L * 24 * 60 * 60 * 1000),
            prefiereLlamada = true,
            horarioPreferido = "tarde",
            diasPreferidos = listOf("martes", "jueves"),
            nivelConfianza = 4,
            calificacionPromedio = 4.3f,
            serviceType = "TECHNICAL"
        ),
        
        // Cliente 5: Empresa Premium
        ClienteMock(
            id = "cliente_005",
            nombre = "Laura",
            apellido = "Fernández",
            telefono = "+54 9 11 6789-0123",
            email = "laura.fernandez@email.com",
            direccion = "Mitre 654, San Isidro",
            notas = "Empresa - Solicitar factura A",
            isOnline = true,
            ultimoMensaje = "Cuando puedas envíame el presupuesto",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000),
            mensajesNoLeidos = 1,
            tipoCliente = "EMPRESA",
            cuit = "30-65432109-5",
            razonSocial = "Fernández Consulting SA",
            condicionIVA = "RESPONSABLE_INSCRIPTO",
            totalCitas = 12,
            citasCompletadas = 11,
            totalPresupuestos = 10,
            presupuestosAprobados = 9,
            montoTotalGastado = 65000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (300L * 24 * 60 * 60 * 1000),
            prefiereEmail = true,
            horarioPreferido = "mañana",
            esFavorito = true,
            esVIP = true,
            nivelConfianza = 5,
            calificacionPromedio = 4.9f,
            serviceType = "TECHNICAL"
        ),
        
        // Cliente 6: Ocasional
        ClienteMock(
            id = "cliente_006",
            nombre = "Diego",
            apellido = "López",
            telefono = "+54 9 11 7890-1234",
            email = "diego.lopez@email.com",
            direccion = "Rivadavia 432, Martínez",
            notas = "",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
            totalCitas = 3,
            citasCompletadas = 3,
            totalPresupuestos = 2,
            presupuestosAprobados = 2,
            montoTotalGastado = 12000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (150L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "flexible",
            nivelConfianza = 3,
            serviceType = "TECHNICAL"
        ),
        
        // Cliente 7: VIP Activo
        ClienteMock(
            id = "cliente_007",
            nombre = "Sofia",
            apellido = "García",
            telefono = "+54 9 11 8901-2345",
            email = "sofia.garcia@email.com",
            direccion = "Maipú 876, Acassuso",
            notas = "Cliente VIP",
            isOnline = true,
            ultimoMensaje = "Excelente como siempre!",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (3 * 60 * 60 * 1000),
            totalCitas = 20,
            citasCompletadas = 19,
            citasCanceladas = 1,
            totalPresupuestos = 15,
            presupuestosAprobados = 14,
            montoTotalGastado = 85000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (450L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "tarde",
            diasPreferidos = listOf("lunes", "miércoles", "viernes"),
            esFavorito = true,
            esVIP = true,
            nivelConfianza = 5,
            calificacionPromedio = 5.0f,
            serviceType = "TECHNICAL"
        ),
        
        // Cliente 8: Con descuento
        ClienteMock(
            id = "cliente_008",
            nombre = "Pablo",
            apellido = "Sánchez",
            telefono = "+54 9 11 9012-3456",
            email = "pablo.sanchez@email.com",
            direccion = "9 de Julio 234, Beccar",
            notas = "Descuento 10%",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (24 * 60 * 60 * 1000),
            ultimoMensaje = "Dale, el viernes a las 10 está bien",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (18 * 60 * 60 * 1000),
            totalCitas = 10,
            citasCompletadas = 9,
            totalPresupuestos = 6,
            presupuestosAprobados = 5,
            montoTotalGastado = 32000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (200L * 24 * 60 * 60 * 1000),
            prefiereLlamada = true,
            horarioPreferido = "mañana",
            nivelConfianza = 4,
            calificacionPromedio = 4.4f,
            serviceType = "PROFESSIONAL"
        ),
        
        // Cliente 9: Transferencia bancaria
        ClienteMock(
            id = "cliente_009",
            nombre = "Valentina",
            apellido = "Romero",
            telefono = "+54 9 11 0123-4567",
            email = "valentina.romero@email.com",
            direccion = "Santa Fe 765, Florida",
            notas = "Prefiere pagos por transferencia",
            isOnline = true,
            ultimoMensaje = "Ya te transferí",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (6 * 60 * 60 * 1000),
            mensajesNoLeidos = 0,
            totalCitas = 5,
            citasCompletadas = 5,
            totalPresupuestos = 3,
            presupuestosAprobados = 3,
            montoTotalGastado = 18000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (120L * 24 * 60 * 60 * 1000),
            prefiereEmail = true,
            horarioPreferido = "tarde",
            nivelConfianza = 4,
            calificacionPromedio = 4.7f,
            serviceType = "PROFESSIONAL"
        ),
        
        // Cliente 10: Fin de semana
        ClienteMock(
            id = "cliente_010",
            nombre = "Martín",
            apellido = "Torres",
            telefono = "+54 9 11 1234-5678",
            email = "martin.torres@email.com",
            direccion = "Libertador 543, La Lucila",
            notas = "Consultar disponibilidad fines de semana",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000),
            ultimoMensaje = "¿Trabajas los sábados?",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (72 * 60 * 60 * 1000),
            mensajesNoLeidos = 0,
            totalCitas = 4,
            citasCompletadas = 3,
            citasCanceladas = 1,
            totalPresupuestos = 2,
            presupuestosAprobados = 1,
            montoTotalGastado = 9500.0,
            fechaPrimerContacto = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "flexible",
            diasPreferidos = listOf("sábado", "domingo"),
            nivelConfianza = 3,
            serviceType = "PROFESSIONAL"

        ),
        
        // Cliente 11: Recordatorio
        ClienteMock(
            id = "cliente_011",
            nombre = "Camila",
            apellido = "Díaz",
            telefono = "+54 9 11 2345-6780",
            email = "camila.diaz@email.com",
            direccion = "Pueyrredón 987, CABA",
            notas = "Recordar confirmar cita 24hs antes",
            isOnline = true,
            ultimoMensaje = "Ok, confirmado para mañana",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (4 * 60 * 60 * 1000),
            mensajesNoLeidos = 0,
            totalCitas = 7,
            citasCompletadas = 6,
            totalPresupuestos = 4,
            presupuestosAprobados = 3,
            montoTotalGastado = 21000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (160L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "mañana",
            nivelConfianza = 4,
            calificacionPromedio = 4.6f,
            serviceType = "PROFESSIONAL"

        ),
        
        // Cliente 12: Básico
        ClienteMock(
            id = "cliente_012",
            nombre = "Lucas",
            apellido = "Moreno",
            telefono = "+54 9 11 3456-7891",
            email = "lucas.moreno@email.com",
            direccion = "Cabildo 2134, CABA",
            notas = "",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000),
            totalCitas = 2,
            citasCompletadas = 2,
            montoTotalGastado = 8000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
            prefiereLlamada = true,
            nivelConfianza = 3,
            serviceType = "PROFESSIONAL"
        ),
        
        // Cliente 13: Antiguo
        ClienteMock(
            id = "cliente_013",
            nombre = "Florencia",
            apellido = "Gutiérrez",
            telefono = "+54 9 11 4567-8902",
            email = "florencia.gutierrez@email.com",
            direccion = "Callao 456, CABA",
            notas = "Cliente desde 2024",
            isOnline = true,
            ultimoMensaje = "Gracias! Siempre impecable",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000),
            totalCitas = 18,
            citasCompletadas = 17,
            totalPresupuestos = 12,
            presupuestosAprobados = 11,
            montoTotalGastado = 52000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (700L * 24 * 60 * 60 * 1000),
            prefiereEmail = true,
            horarioPreferido = "tarde",
            esFavorito = true,
            nivelConfianza = 5,
            calificacionPromedio = 4.8f,
            serviceType = "PROFESSIONAL"
        ),
        
        // Cliente 14: Urgencias
        ClienteMock(
            id = "cliente_014",
            nombre = "Federico",
            apellido = "Silva",
            telefono = "+54 9 11 5678-9013",
            email = "federico.silva@email.com",
            direccion = "Lavalle 789, CABA",
            notas = "Trabajo urgente preferible",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (12 * 60 * 60 * 1000),
            ultimoMensaje = "Necesito que vengas YA",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (15 * 60 * 60 * 1000),
            mensajesNoLeidos = 1,
            totalCitas = 9,
            citasCompletadas = 8,
            totalPresupuestos = 7,
            presupuestosAprobados = 6,
            montoTotalGastado = 34000.0,
            esClienteFrecuente = true,
            fechaPrimerContacto = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            prefiereLlamada = true,
            horarioPreferido = "flexible",
            nivelConfianza = 4,
            calificacionPromedio = 4.2f,
            serviceType = "RENTAL"
        ),
        
        // Cliente 15: Flexible
        ClienteMock(
            id = "cliente_015",
            nombre = "Micaela",
            apellido = "Castro",
            telefono = "+54 9 11 6789-0124",
            email = "micaela.castro@email.com",
            direccion = "Tucumán 321, CABA",
            notas = "Horarios flexibles",
            isOnline = true,
            ultimoMensaje = "Cuando puedas está bien",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (8 * 60 * 60 * 1000),
            totalCitas = 6,
            citasCompletadas = 5,
            totalPresupuestos = 3,
            presupuestosAprobados = 3,
            montoTotalGastado = 16000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (100L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "flexible",
            nivelConfianza = 4,
            calificacionPromedio = 4.5f,
            serviceType = "RENTAL"
        ),
        
        // Cliente 16: WhatsApp
        ClienteMock(
            id = "cliente_016",
            nombre = "Agustín",
            apellido = "Vargas",
            telefono = "+54 9 11 7890-1235",
            email = "agustin.vargas@email.com",
            direccion = "Córdoba 654, CABA",
            notas = "Prefiere comunicación por WhatsApp",
            isOnline = true,
            ultimoMensaje = "👍",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000),
            mensajesNoLeidos = 0,
            totalCitas = 4,
            citasCompletadas = 4,
            totalPresupuestos = 2,
            presupuestosAprobados = 2,
            montoTotalGastado = 11000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (75L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "noche",
            nivelConfianza = 3,
            serviceType = "RENTAL"
        ),
        
        // Cliente 17: Básico sin historial
        ClienteMock(
            id = "cliente_017",
            nombre = "Catalina",
            apellido = "Ruiz",
            telefono = "+54 9 11 8901-2346",
            email = "catalina.ruiz@email.com",
            direccion = "Paraguay 987, CABA",
            notas = "",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000),
            totalCitas = 1,
            citasCompletadas = 1,
            montoTotalGastado = 5000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (40L * 24 * 60 * 60 * 1000),
            nivelConfianza = 2,
            serviceType = "RENTAL"
        ),
        
        // Cliente 18: Mascota
        ClienteMock(
            id = "cliente_018",
            nombre = "Tomás",
            apellido = "Méndez",
            telefono = "+54 9 11 9012-3457",
            email = "tomas.mendez@email.com",
            direccion = "Uruguay 234, CABA",
            notas = "Mascota en casa (perro grande)",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (36 * 60 * 60 * 1000),
            ultimoMensaje = "Mi perro es amigable, no te preocupes",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (48 * 60 * 60 * 1000),
            totalCitas = 5,
            citasCompletadas = 4,
            totalPresupuestos = 3,
            presupuestosAprobados = 2,
            montoTotalGastado = 14000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (110L * 24 * 60 * 60 * 1000),
            prefiereLlamada = true,
            horarioPreferido = "tarde",
            nivelConfianza = 3,
            calificacionPromedio = 4.0f,
            serviceType = "RENTAL"
        ),
        
        // Cliente 19: Recomendada
        ClienteMock(
            id = "cliente_019",
            nombre = "Julieta",
            apellido = "Ortiz",
            telefono = "+54 9 11 0123-4568",
            email = "julieta.ortiz@email.com",
            direccion = "Viamonte 543, CABA",
            notas = "Cliente recomendada por María González",
            isOnline = true,
            ultimoMensaje = "María me recomendó, ¿tienes tiempo esta semana?",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (10 * 60 * 60 * 1000),
            mensajesNoLeidos = 1,
            totalCitas = 2,
            citasCompletadas = 1,
            totalPresupuestos = 1,
            presupuestosAprobados = 1,
            montoTotalGastado = 7000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (20L * 24 * 60 * 60 * 1000),
            prefiereWhatsApp = true,
            horarioPreferido = "mañana",
            nivelConfianza = 3,
            calificacionPromedio = 5.0f,
            serviceType = "RENTAL"
        ),
        
        // Cliente 20: Edificio sin ascensor
        ClienteMock(
            id = "cliente_020",
            nombre = "Matías",
            apellido = "Navarro",
            telefono = "+54 9 11 1234-5679",
            email = "matias.navarro@email.com",
            direccion = "Juncal 876, CABA",
            notas = "Edificio sin ascensor, piso 3",
            isOnline = false,
            ultimaConexion = System.currentTimeMillis() - (60 * 60 * 60 * 1000),
            ultimoMensaje = "Hay 3 pisos, avísame si es problema",
            ultimoMensajeTimestamp = System.currentTimeMillis() - (96 * 60 * 60 * 1000),
            totalCitas = 3,
            citasCompletadas = 3,
            totalPresupuestos = 2,
            presupuestosAprobados = 2,
            montoTotalGastado = 10000.0,
            fechaPrimerContacto = System.currentTimeMillis() - (80L * 24 * 60 * 60 * 1000),
            prefiereEmail = true,
            horarioPreferido = "mañana",
            nivelConfianza = 3,
            calificacionPromedio = 4.3f,
            serviceType = "RENTAL"
        )
    )
    
    /**
     * Buscar clientes por nombre, teléfono o email
     */
    fun buscarClientes(query: String): List<ClienteMock> {
        if (query.isBlank()) return clientes
        
        val queryLower = query.lowercase().trim()
        return clientes.filter { 
            it.nombreCompleto.lowercase().contains(queryLower) ||
            it.telefono.contains(queryLower) ||
            it.email.lowercase().contains(queryLower)
        }
    }
    
    /**
     * Obtener cliente por ID
     */
    fun getClienteById(id: String): ClienteMock? {
        return clientes.find { it.id == id }
    }
    
    /**
     * Obtener los 5 clientes más recientes
     */
    fun getClientesRecientes(): List<ClienteMock> {
        return clientes.sortedByDescending { it.ultimoMensajeTimestamp }.take(5)
    }
    
    /**
     * Obtener clientes VIP
     */
    fun getClientesVIP(): List<ClienteMock> {
        return clientes.filter { it.esVIP }
    }
    
    /**
     * Obtener clientes frecuentes
     */
    fun getClientesFrecuentes(): List<ClienteMock> {
        return clientes.filter { it.esClienteFrecuente }
    }
    
    /**
     * Obtener clientes con mensajes no leídos
     */
    fun getClientesConMensajesNoLeidos(): List<ClienteMock> {
        return clientes.filter { it.mensajesNoLeidos > 0 }
            .sortedByDescending { it.ultimoMensajeTimestamp }
    }
    
    /**
     * Obtener clientes online
     */
    fun getClientesOnline(): List<ClienteMock> {
        return clientes.filter { it.isOnline }
    }
    
    /**
     * Obtener clientes por tipo (PARTICULAR/EMPRESA)
     */
    fun getClientesPorTipo(tipo: String): List<ClienteMock> {
        return clientes.filter { it.tipoCliente == tipo }
    }
    
    /**
     * Obtener estadísticas generales
     */
    fun getEstadisticas(): EstadisticasClientes {
        return EstadisticasClientes(
            totalClientes = clientes.size,
            clientesVIP = clientes.count { it.esVIP },
            clientesFrecuentes = clientes.count { it.esClienteFrecuente },
            clientesOnline = clientes.count { it.isOnline },
            mensajesNoLeidos = clientes.sumOf { it.mensajesNoLeidos },
            montoTotalGenerado = clientes.sumOf { it.montoTotalGastado },
            promedioCalificacion = clientes.filter { it.calificacionPromedio > 0 }
                .map { it.calificacionPromedio }.average().toFloat()
        )
    }
}

/**
 * Clase para estadísticas de clientes
 */
data class EstadisticasClientes(
    val totalClientes: Int,
    val clientesVIP: Int,
    val clientesFrecuentes: Int,
    val clientesOnline: Int,
    val mensajesNoLeidos: Int,
    val montoTotalGenerado: Double,
    val promedioCalificacion: Float
)
**/