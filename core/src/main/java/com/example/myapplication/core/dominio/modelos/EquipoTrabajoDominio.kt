package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import java.util.UUID

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Modelo de Equipo de Trabajo (UiModel Unificado)
 * [PROPÓSITO]: Representar al staff o personal con datos listos para UI.
 * [FUNCIONAMIENTO INTERNO]: Contiene datos puros y versiones formateadas ("masticadas").
 * [RELACIÓN]: Se vincula con 'EquipoTrabajoEntity'. Reemplaza a 'EquipoTrabajo' y 'EquipoUiModel'.
 */
@Keep
data class EquipoTrabajoDominio(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val apellido: String = "",
    val nombreCompleto: String = "",
    val cargo: String = "",
    val detalle: String = "",
    val iniciales: String = "",
    val avatarEmoji: String = "👤",
    val idSucursal: String? = null,
    val horario: HorarioDominio? = null,
    val estaHabilitado: Boolean = true,
    val idRecursoVinculado: String? = null,
    val tipoHorario: com.example.myapplication.core.datos.local.entidades.TipoHorario = com.example.myapplication.core.datos.local.entidades.TipoHorario.Horario_DisponibilidadTurnos
)

