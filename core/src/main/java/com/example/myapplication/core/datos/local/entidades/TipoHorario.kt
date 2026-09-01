package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep

/**
 * --- TIPOS DE HORARIOS (v2026.RESOURCES) ---
 */
@Keep
enum class TipoHorario {
    Horario_Atencion,             // Marco general del local
    Horario_DisponibilidadTurnos, // Específico para espacios y staff local
    Horario_DisponibilidadVisitas // Específico para personal de campo y agenda móvil
}
