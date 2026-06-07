package com.example.myapplication.prestador.data.local.entity

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en los modelos del módulo :core.
 * Las direcciones ahora se gestionan mediante AddressUnico definido en
 * com.example.myapplication.core.domain.model.AddressUnico
 */

/*
import androidx.room.Entity
import androidx.room.PrimaryKey

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
*/
