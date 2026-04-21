package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ENTIDAD DE BASE DE DATOS: EMPLEADO
 * Representa a los empleados/colaboradores que trabajan con el prestador
 * Se muestra al cliente cuando se agenda una visita técnica
 */
@Entity(tableName = "empleados")
data class EmpleadoEntity(
    @PrimaryKey
    val id: String,
    val prestadorId: String,  // ID del prestador al que pertenece
    val nombre: String,
    val apellido: String,
    val dni: String,  // Solo visible para el prestador
    val activo: Boolean = true,  // Soft delete: false = eliminado
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Nombre completo para mostrar
     */
    fun nombreCompleto(): String = "$nombre $apellido"
    
    /**
     * DNI ofuscado para mostrar (solo últimos 3 dígitos)
     * Ejemplo: "12345678" -> "****5678"
     */
    fun dniOfuscado(): String {
        if (dni.length < 4) return dni
        return "****" + dni.takeLast(4)
    }
}
