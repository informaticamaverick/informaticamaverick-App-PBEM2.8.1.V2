/**

package com.example.myapplication.prestador.data.mock

import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Datos mock de citas para pruebas
 * Estas citas pueden ser usadas para poblar la base de datos en desarrollo
 */

object AppointmentsMockData {
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Genera citas de ejemplo para un prestador
     * @param providerId ID del prestador
     * @return Lista de citas mock
     */
    fun generarCitasEjemplo(providerId: String): List<AppointmentEntity> {
        val hoy = Calendar.getInstance()
       // val clientes = ClientesMockData.clientes
        
        return listOf(
            // Citas de hoy
            AppointmentEntity(
                id = "apt_001",
                clientId = clientes[0].id,
                clientName = clientes[0].nombreCompleto,
                providerId = providerId,
                service = "Reparación de instalación eléctrica",
                date = dateFormatter.format(hoy.time),
                time = "09:00",
                duration = 120,
                status = "confirmado",
                notes = "Revisar tomas en cocina",
                proposedBy = "provider",
                serviceType = "TECHNICAL"
            ),
            AppointmentEntity(
                id = "apt_002",
                clientId = clientes[1].id,
                clientName = clientes[1].nombreCompleto,
                providerId = providerId,
                service = "Consulta inicial",
                date = dateFormatter.format(hoy.time),
                time = "11:30",
                duration = 60,
                status = "pendiente",
                notes = "",
                proposedBy = "cliente",
                serviceType = "PROFESSIONAL"
            ),
            AppointmentEntity(
                id = "apt_003",
                clientId = clientes[2].id,
                clientName = clientes[2].nombreCompleto,
                providerId = providerId,
                service = "Mantenimiento preventivo",
                date = dateFormatter.format(hoy.time),
                time = "15:00",
                duration = 90,
                status = "confirmado",
                notes = "Cliente prefiere comunicación por WhatsApp",
                proposedBy = "provider",
                serviceType = "TECHNICAL"
            ),
            
            // Citas de mañana
            AppointmentEntity(
                id = "apt_004",
                clientId = clientes[3].id,
                clientName = clientes[3].nombreCompleto,
                providerId = providerId,
                service = "Instalación de equipo",
                date = dateFormatter.format(hoy.apply { add(Calendar.DAY_OF_YEAR, 1) }.time),
                time = "10:00",
                duration = 180,
                status = "confirmado",
                notes = "Traer equipo desde depósito",
                proposedBy = "provider",
                serviceType = "TECHNICAL"
            ),
            AppointmentEntity(
                id = "apt_005",
                clientId = clientes[4].id,
                clientName = clientes[4].nombreCompleto,
                providerId = providerId,
                service = "Revisión de espacio",
                date = dateFormatter.format(hoy.time),
                time = "14:00",
                duration = 60,
                status = "pendiente",
                notes = "Solicitar factura A",
                proposedBy = "cliente",
                serviceType = "RENTAL"
            ),
            
            // Citas de la próxima semana
            AppointmentEntity(
                id = "apt_006",
                clientId = clientes[5].id,
                clientName = clientes[5].nombreCompleto,
                providerId = providerId,
                service = "Reparación urgente",
                date = dateFormatter.format(hoy.apply { add(Calendar.DAY_OF_YEAR, 2) }.time),
                time = "09:30",
                duration = 120,
                status = "confirmado",
                notes = "Urgencia - Prioridad alta",
                proposedBy = "cliente",
                serviceType = "TECHNICAL"
            ),
            AppointmentEntity(
                id = "apt_007",
                clientId = clientes[6].id,
                clientName = clientes[6].nombreCompleto,
                providerId = providerId,
                service = "Consulta profesional",
                date = dateFormatter.format(hoy.apply { add(Calendar.DAY_OF_YEAR, 1) }.time),
                time = "11:00",
                duration = 90,
                status = "confirmado",
                notes = "Cliente VIP",
                proposedBy = "provider",
                serviceType = "PROFESSIONAL"
            ),
            
            // Citas pasadas (completadas/canceladas)
            AppointmentEntity(
                id = "apt_008",
                clientId = clientes[7].id,
                clientName = clientes[7].nombreCompleto,
                providerId = providerId,
                service = "Mantenimiento rutinario",
                date = dateFormatter.format(hoy.apply { add(Calendar.DAY_OF_YEAR, -1) }.time),
                time = "16:00",
                duration = 60,
                status = "completado",
                notes = "Trabajo completado satisfactoriamente",
                proposedBy = "provider",
                serviceType = "TECHNICAL"
            ),
            AppointmentEntity(
                id = "apt_009",
                clientId = clientes[8].id,
                clientName = clientes[8].nombreCompleto,
                providerId = providerId,
                service = "Evaluación de proyecto",
                date = dateFormatter.format(hoy.apply { add(Calendar.DAY_OF_YEAR, -2) }.time),
                time = "14:30",
                duration = 120,
                status = "cancelado",
                notes = "Cliente canceló con 24hs de anticipación",
                proposedBy = "cliente",
                serviceType = "PROFESSIONAL"
            ),
            AppointmentEntity(
                id = "apt_010",
                clientId = clientes[9].id,
                clientName = clientes[9].nombreCompleto,
                providerId = providerId,
                service = "Alquiler de espacio - Evento",
                date = dateFormatter.format(hoy.apply { add(Calendar.DAY_OF_YEAR, 5) }.time),
                time = "10:00",
                duration = 480, // 8 horas
                status = "confirmado",
                notes = "Evento corporativo - Verificar disponibilidad fin de semana",
                proposedBy = "cliente",
                serviceType = "RENTAL"
            )
        )
    }
    
    /**
     * Obtiene estadísticas rápidas de las citas
     */
    fun getEstadisticas(citas: List<AppointmentEntity>): AppointmentStats {
        return AppointmentStats(
            total = citas.size,
            pendientes = citas.count { it.status == "pendiente" },
            confirmadas = citas.count { it.status == "confirmado" },
            completadas = citas.count { it.status == "completado" },
            canceladas = citas.count { it.status == "cancelado" }
        )
    }
}

/**
 * Clase para estadísticas de citas
 */
data class AppointmentStats(
    val total: Int,
    val pendientes: Int,
    val confirmadas: Int,
    val completadas: Int,
    val canceladas: Int
)
**/