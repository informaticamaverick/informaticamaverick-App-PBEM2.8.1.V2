package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * --- CUENTA MAVERICK ELITE (Pilar #1 - El Maestro de Control) ---
 * 
 * [PROPÓSITO]: Centralizar la autenticación, el estado de suscripción y el 
 * control de soberanía (qué perfil tiene el mando de la App).
 * 
 * [FUNCIONAMIENTO]: Actúa como el 'Root' del ecosistema. Almacena el ID del 
 * perfil activo y decide si se prioriza la visualización empresarial o personal.
 * 
 * [RELACIÓN]: Es la entidad madre. Todos los pilares (Usuario, Prestador, Empresa) 
 * dependen del ID de esta cuenta para validar su propiedad.
 */
@Keep
@Entity(
    tableName = "cuentas",
    indices = [
        Index(value = ["correoGoogle"]),
        Index(value = ["idPerfilActivo"])
    ]
)
data class CuentaEntity(
    @PrimaryKey val id: String = "", // UID de Firebase
    val correoGoogle: String = "",
    val idPerfilActivo: String? = null, // ID de la Identidad activa
    val priorizarEmpresa: Boolean = false,
    
    // --- SECTOR: SUSCRIPCIÓN ---
    val estaSuscrito: Boolean = false,
    val nivelSuscripcion: String = "BRONCE",
    val estadoSuscripcion: String = "INACTIVO",
    val fechaVencimiento: Long? = null,
    
    // --- AUDITORÍA ---
    val ultimaSincronizacion: Long = System.currentTimeMillis(),
    val fechaCreacion: Long = System.currentTimeMillis()
)


































