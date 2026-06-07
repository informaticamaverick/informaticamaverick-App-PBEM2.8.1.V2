package com.example.myapplication.prestador.data.local.entity

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en los modelos del módulo :core.
 * Los empleados ahora se gestionan mediante EmployeeProvider definido en
 * com.example.myapplication.core.domain.model.CompanyModelsProvider
 */

/*
import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
*/
