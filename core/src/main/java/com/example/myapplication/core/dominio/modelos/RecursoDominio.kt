package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import java.util.UUID

/**
 * --- MODELO DE DOMINIO RECURSO (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa un activo de negocio (cancha, consultorio, etc.).
 */
@Keep
data class RecursoDominio(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val descripcion: String = "",
    val precioBase: Double = 0.0,
    val precioFormateado: String = "$0.00",
    val tipoRecurso: String = "GENERICO", 
    val estaActivo: Boolean = true,
    val capacidadMaxima: Int = 1,
    val capacidadFormateada: String = "1 pers.",
    val idSucursal: String? = null,
    val horario: HorarioDominio? = null,
    val tipoHorario: com.example.myapplication.core.datos.local.entidades.TipoHorario = com.example.myapplication.core.datos.local.entidades.TipoHorario.Horario_DisponibilidadTurnos
)

