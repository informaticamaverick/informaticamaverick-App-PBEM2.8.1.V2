package com.example.myapplication.prestador.data.model

/**
 * Tipos de servicio que puede ofrecer un prestador
 */
enum class ServiceType(val displayName: String, val description: String) {
    TECHNICAL(
        displayName = "Servicios Técnicos",
        description = "Plomería, electricidad, mantenimiento, etc."
    ),
    PROFESSIONAL(
        displayName = "Profesional con Agenda",
        description = "Médicos, psicólogos, abogados, etc."
    ),
    RENTAL(
        displayName = "Alquiler de Espacios",
        description = "Canchas, salones, estudios, etc."
    ),
    OTHER(
        displayName = "Otros Servicios",
        description = "Otros tipos de servicios"
    );
    
    companion object {
        fun fromString(value: String?): ServiceType {
            return values().find { it.name == value } ?: TECHNICAL
        }
    }
}
