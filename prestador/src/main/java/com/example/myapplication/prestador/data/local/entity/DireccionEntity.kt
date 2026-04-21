package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad reutilizable para presentar una direccion completa.
 * Seusa para prestado, casa central, sucursales, etc.
 */

@Entity(tableName = "direcciones")
data class DireccionEntity(
    @PrimaryKey
    val id: String,
    val referenciaId: String, //ID del prestador, empresa o sucursal al que pertenece
    val referenciaTipo: String, //"PRESTADO, EMPRESA, SUCURSAL"
    val pais: String = "Argentina",
    val provincia: String? = null,
    val localidad: String? = null,
    val codigoPostal: String? = null,
    val calle: String? = null,
    val numero: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)