package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO INVENTARIO (v2026.ELITE) ---
 * [PROPÓSITO]: Modelo unificado para representar tanto Recursos Físicos como Equipo Humano.
 */
@Keep
data class InventarioActivoDominio(
    val id: String,
    val nombre: String,
    val tipo: TipoActivo,
    val habilitado: Boolean,
    val categoria: String,
    val subTitulo: String,
    val idSucursal: String? = null,
    
    // --- CAMPOS EXTENDIDOS (MOLDE TURNERO) ---
    val equipamiento: String = "", // Solo Recursos
    val especialidad: String = "", // Solo Equipo
    val matricula: String = "",     // Solo Equipo
    val horario: HorarioDominio? = null,
    val idRecursoVinculado: String? = null // Solo Equipo
)

enum class TipoActivo {
    RECURSO, EQUIPO
}

