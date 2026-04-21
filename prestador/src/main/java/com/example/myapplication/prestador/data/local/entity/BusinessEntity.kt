package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de base de datos: Business (negocio)
 * Guarda la información del negocio del prestador
 */

@Entity(
    tableName = "business",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["providerId"])]
)
data class BusinessEntity(
    @PrimaryKey
    val id: String,
    val providerId: String,
    val nombreNegocio: String,
    val razonSocial: String,
    val cuitNegocio: String,
    val direccion: String,
    val codigoPostal: String,
    val telefono: String? = null,
    val email: String? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null,

    //Direcion casa central
    val direccionId:String? = null,

    //referente de casa central
    val referenteId: String? = null,

    //Categoria (JSON, hasta 5)
    val categorias: String = "[]",

    //Imagenes de productos/servicios
    val imagenesProductos: String = "[]",
    //Servicios que ofrece
    val atencion24hs: Boolean = false,
    val localComercial: Boolean = false,
    val visitaDomicilio: Boolean = false,
    val envios: Boolean = false,
    val turnos: Boolean = false,
    val horario: String? = null,

    //Estado
    val verificado: Boolean = false,
    val rating: Float = 0f,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)