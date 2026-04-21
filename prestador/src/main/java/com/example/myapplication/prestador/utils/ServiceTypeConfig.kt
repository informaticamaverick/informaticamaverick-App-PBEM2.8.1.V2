package com.example.myapplication.prestador.utils

import com.example.myapplication.prestador.data.model.ServiceType

/**
 * Configuración de terminología y comportamiento según tipo de servicio
 */
data class ServiceTypeConfig(
    // Nombres singulares
    val appointmentName: String,
    val appointmentNameCapitalized: String,
    
    // Nombres plurales
    val appointmentsName: String,
    val appointmentsNameCapitalized: String,
    
    // Verbos de acción
    val createAction: String,
    val rescheduleAction: String,
    val cancelAction: String,
    val confirmAction: String,
    
    // Títulos de pantalla
    val calendarTitle: String,
    val createDialogTitle: String,
    val detailsDialogTitle: String,
    
    // Mensajes de notificación
    val notificationAcceptedMessage: (clientName: String) -> String,
    val notificationRejectedMessage: (clientName: String, reason: String) -> String,
    val notificationPendingMessage: (clientName: String) -> String,
    
    // Mensajes de chat
    val proposalMessage: (date: String, time: String) -> String,
    val acceptedMessage: String,
    val rejectedMessage: String,
    
    // Estados
    val pendingStatus: String,
    val confirmedStatus: String,
    val cancelledStatus: String,
    val completedStatus: String,
    
    // Campos específicos
    val requiresSpace: Boolean, // true para RENTAL
    val requiresDuration: Boolean, // true para PROFESSIONAL
    val requiresUrgency: Boolean, // true para TECHNICAL
    val requiresPeopleCount: Boolean, // true para RENTAL
    
    // Etiquetas de campos
    val spaceLabel: String,
    val durationLabel: String,
    val urgencyLabel: String,
    val peopleCountLabel: String,
    
    // Placeholder para descripción
    val descriptionPlaceholder: String
)

/**
 * Obtiene la configuración según el tipo de servicio
 */
fun getServiceTypeConfig(serviceType: ServiceType): ServiceTypeConfig {
    return when (serviceType) {
        ServiceType.TECHNICAL -> ServiceTypeConfig(
            appointmentName = "servicio",
            appointmentNameCapitalized = "Servicio",
            appointmentsName = "servicios",
            appointmentsNameCapitalized = "Servicios",
            createAction = "Crear Servicio",
            rescheduleAction = "Reprogramar",
            cancelAction = "Cancelar Servicio",
            confirmAction = "Confirmar Servicio",
            calendarTitle = "Mis Servicios",
            createDialogTitle = "Nuevo Servicio",
            detailsDialogTitle = "Detalles del Servicio",
            notificationAcceptedMessage = { clientName -> "$clientName aceptó el servicio" },
            notificationRejectedMessage = { clientName, reason -> "$clientName rechazó el servicio: $reason" },
            notificationPendingMessage = { clientName -> "$clientName solicitó un servicio" },
            proposalMessage = { date, time -> "Te propongo realizar el servicio el $date a las $time" },
            acceptedMessage = "Servicio aceptado ✅",
            rejectedMessage = "Servicio rechazado ❌",
            pendingStatus = "Pendiente",
            confirmedStatus = "Confirmado",
            cancelledStatus = "Cancelado",
            completedStatus = "Completado",
            requiresSpace = false,
            requiresDuration = false,
            requiresUrgency = true,
            requiresPeopleCount = false,
            spaceLabel = "",
            durationLabel = "",
            urgencyLabel = "Urgencia",
            peopleCountLabel = "",
            descriptionPlaceholder = "Describe el problema o trabajo a realizar..."
        )
        
        ServiceType.PROFESSIONAL -> ServiceTypeConfig(
            appointmentName = "cita",
            appointmentNameCapitalized = "Cita",
            appointmentsName = "citas",
            appointmentsNameCapitalized = "Citas",
            createAction = "Agendar Cita",
            rescheduleAction = "Reprogramar Cita",
            cancelAction = "Cancelar Cita",
            confirmAction = "Confirmar Cita",
            calendarTitle = "Mis Citas",
            createDialogTitle = "Nueva Cita",
            detailsDialogTitle = "Detalles de la Cita",
            notificationAcceptedMessage = { clientName -> "$clientName aceptó la cita" },
            notificationRejectedMessage = { clientName, reason -> "$clientName rechazó la cita: $reason" },
            notificationPendingMessage = { clientName -> "$clientName solicitó una cita" },
            proposalMessage = { date, time -> "Te propongo una cita el $date a las $time" },
            acceptedMessage = "Cita confirmada ✅",
            rejectedMessage = "Cita rechazada ❌",
            pendingStatus = "Pendiente",
            confirmedStatus = "Confirmada",
            cancelledStatus = "Cancelada",
            completedStatus = "Completada",
            requiresSpace = false,
            requiresDuration = true,
            requiresUrgency = false,
            requiresPeopleCount = false,
            spaceLabel = "",
            durationLabel = "Duración",
            urgencyLabel = "",
            peopleCountLabel = "",
            descriptionPlaceholder = "Motivo de la consulta..."
        )
        
        ServiceType.RENTAL -> ServiceTypeConfig(
            appointmentName = "reserva",
            appointmentNameCapitalized = "Reserva",
            appointmentsName = "reservas",
            appointmentsNameCapitalized = "Reservas",
            createAction = "Nueva Reserva",
            rescheduleAction = "Modificar Reserva",
            cancelAction = "Cancelar Reserva",
            confirmAction = "Confirmar Reserva",
            calendarTitle = "Mis Reservas",
            createDialogTitle = "Nueva Reserva",
            detailsDialogTitle = "Detalles de la Reserva",
            notificationAcceptedMessage = { clientName -> "$clientName confirmó la reserva" },
            notificationRejectedMessage = { clientName, reason -> "$clientName canceló la reserva: $reason" },
            notificationPendingMessage = { clientName -> "$clientName solicitó una reserva" },
            proposalMessage = { date, time -> "Te propongo reservar el $date a las $time" },
            acceptedMessage = "Reserva confirmada ✅",
            rejectedMessage = "Reserva cancelada ❌",
            pendingStatus = "Pendiente",
            confirmedStatus = "Confirmada",
            cancelledStatus = "Cancelada",
            completedStatus = "Finalizada",
            requiresSpace = true,
            requiresDuration = true,
            requiresUrgency = false,
            requiresPeopleCount = true,
            spaceLabel = "Espacio",
            durationLabel = "Duración",
            urgencyLabel = "",
            peopleCountLabel = "Cantidad de Personas",
            descriptionPlaceholder = "Detalles de la reserva..."
        )
        
        ServiceType.OTHER -> ServiceTypeConfig(
            appointmentName = "cita",
            appointmentNameCapitalized = "Cita",
            appointmentsName = "citas",
            appointmentsNameCapitalized = "Citas",
            createAction = "Nueva Cita",
            rescheduleAction = "Reprogramar",
            cancelAction = "Cancelar",
            confirmAction = "Confirmar",
            calendarTitle = "Mis Citas",
            createDialogTitle = "Nueva Cita",
            detailsDialogTitle = "Detalles",
            notificationAcceptedMessage = { clientName -> "$clientName aceptó la cita" },
            notificationRejectedMessage = { clientName, reason -> "$clientName rechazó: $reason" },
            notificationPendingMessage = { clientName -> "$clientName solicitó una cita" },
            proposalMessage = { date, time -> "Te propongo el $date a las $time" },
            acceptedMessage = "Aceptado ✅",
            rejectedMessage = "Rechazado ❌",
            pendingStatus = "Pendiente",
            confirmedStatus = "Confirmado",
            cancelledStatus = "Cancelado",
            completedStatus = "Completado",
            requiresSpace = false,
            requiresDuration = false,
            requiresUrgency = false,
            requiresPeopleCount = false,
            spaceLabel = "",
            durationLabel = "",
            urgencyLabel = "",
            peopleCountLabel = "",
            descriptionPlaceholder = "Detalles..."
        )
    }
}
