package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- IDENTIDAD USUARIO MAVERICK (Pilar #5 - El Humano Cliente) ---
 * 
 * [PROPÓSITO]: Representar el perfil del usuario final (Cliente) en el ecosistema 
 * para gestionar sus datos personales, alias e identidad digital.
 * 
 * [FUNCIONAMIENTO]: Actúa como una entidad de Room pura. Se vincula a [CuentaEntity] 
 * mediante el ID (UID de Firebase).
 * 
 * [RELACIÓN]: Es la base del Ecosistema de Cliente. A diferencia del Prestador, 
 * esta entidad no contiene métricas comerciales ni configuraciones de recursos, 
 * enfocándose en la simplicidad de la experiencia del consumidor.
 */
@Keep
@IgnoreExtraProperties
@Entity(
    tableName = "identidades_usuario",
    indices = [
        Index(value = ["correoElectronico"])
    ]
)
data class IdentidadUsuarioEntity(
    @PrimaryKey val id: String = "", // UID de Firebase
    
    // --- SECTOR: IDENTIDAD HUMANA ---
    val nombre: String = "",
    val apellido: String = "",
    val nombreVisible: String = "",
    val correoElectronico: String = "",
    val numeroTelefono: String = "",
    val cuitCuil: String = "",
    val biografia: String = "",
    val urlFotoPerfil: String? = null,
    val miniaturaBase64: String? = null,
    
    // --- SECTOR: REPUTACIÓN CLIENTE (Ley de Confianza) ---
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosContratados: Int = 0,
    
    // --- SECTOR: ESTADO ---
    val estaEnLinea: Boolean = false,
    val ultimaSincronizacion: Long = System.currentTimeMillis(),
    val fechaCreacion: Long = System.currentTimeMillis()
)

