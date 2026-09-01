package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep

/**
 * --- VISIBILIDAD DE RECURSOS (v2026.ELITE_SCHEDULING) ---
 */
@Keep
enum class VisibilidadRecurso {
    PRIVADO,    // Solo el prestador lo ve (Uso por invitación vía chat)
    PUBLICO     // Visible en el perfil para reserva directa (Futuro)
}
