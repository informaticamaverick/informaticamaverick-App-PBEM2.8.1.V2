package com.example.myapplication.core.domain.model

/**
 * --- ENUMERACIÓN DE TIPOS DE MENSAJE ---
 * Define el tipo de contenido que se está enviando en las conversaciones de chat.
 * Este archivo es compartido por el Cliente y el Prestador para asegurar que ambos
 * sepan interpretar los diferentes mensajes (Texto, Imágenes, Presupuestos, etc).
 */
enum class MessageType {
    TEXT,               // Texto simple
    IMAGE,              // Foto (Uri o Path)
    AUDIO,              // Nota de voz
    LOCATION,           // Ubicación (Lat/Lng)
    VISIT,              // Una cita técnica agendada
    BUDGET,             // Un presupuesto formal recibido
    TENDER,             // Invitación a Licitación enviada
    CALENDAR_INVITE,    // Invitación a elegir turno
    APPOINTMENT_RECEIPT,// Comprobante de turno confirmado
    BUDGET_REQUEST,     // Solicitud de presupuesto enviada por el cliente
    SYSTEM              // Mensajes generados por el sistema (ej: "Presupuesto Aceptado")
}
