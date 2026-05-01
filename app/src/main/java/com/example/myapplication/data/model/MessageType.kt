package com.example.myapplication.data.model

// Este archivo define QUÉ tipo de cosa estamos enviando en el chat.
enum class MessageType {
    TEXT,       // Texto simple
    IMAGE,      // Foto (Uri o Path)
    AUDIO,      // Nota de voz
    LOCATION,   // Ubicación (Lat/Lng)
    VISIT,      // Una cita técnica agendada
    BUDGET,      // Un presupuesto formal recibido
    TENDER,    // 🔥 NUEVO: Invitación a Licitación enviada
    CALENDAR_INVITE, // 🔥 NUEVO: Invitación a elegir turno
    APPOINTMENT_RECEIPT, // 🔥 NUEVO: Comprobante de turno confirmado
    SYSTEM    // 🔥 AGREGA ESTA LÍNEA PARA QUITAR EL ERROR
}