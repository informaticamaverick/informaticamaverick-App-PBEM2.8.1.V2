package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * --- EMPRESA MAVERICK ELITE (Pilar #3 - La Entidad Legal) ---
 * 
 * [PROPÓSITO]: Representar la personalidad jurídica o marca comercial del prestador, 
 * centralizando la reputación corporativa y los datos fiscales (CUIT).
 * 
 * [FUNCIONAMIENTO]: Actúa como un contenedor legal. Puede tener múltiples [SucursalEntity] 
 * vinculadas, pero ella misma no opera geográficamente (lo hacen sus sucursales).
 * 
 * [RELACIÓN]: Se vincula a [CuentaEntity] (Propietario). Cuando está activa, 
 * toma la Soberanía de la App [LEY #6] bloqueando el perfil personal del profesional.
 */
@Keep
@Entity(
    tableName = "empresas",
    indices = [
        Index(value = ["idPropietario"]),
        Index(value = ["cuit"])
    ]
)
data class EmpresaEntity(
    @PrimaryKey val id: String, // UUID único
    val idPropietario: String, // Vínculo con CuentaEntity

    // --- SECTOR: DATOS CORPORATIVOS ---
    val nombre: String = "",
    val razonSocial: String = "",
    val descripcion: String = "",
    val cuit: String = "",
    val correoContacto: String = "",
    val urlFoto: String? = null,
    val miniaturaBase64: String? = null,
    
    // --- SECTOR: NEGOCIO Y MÉTRICAS ---
    val idCategorias: List<String> = emptyList(),
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosRealizados: Int = 0,
    val nivelElite: Int = 0,
    val estaVerificada: Boolean = false,
    
    // --- AUDITORÍA ---
    val ultimaSincronizacion: Long = System.currentTimeMillis(),
    val fechaCreacion: Long = System.currentTimeMillis()
)


































