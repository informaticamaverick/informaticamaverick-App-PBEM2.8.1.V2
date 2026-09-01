package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * --- SUCURSAL MAVERICK (Pilar #4 - El Punto de Venta POS) ---
 * 
 * [PROPÓSITO]: Representar el punto operativo físico donde ocurre la transacción o 
 * despacho de servicios, gestionando su propia ubicación y equipo humano.
 * 
 * [FUNCIONAMIENTO]: Actúa como un contenedor final de Horarios, Recursos y Staff.
 * La ubicación se obtiene mediante JOIN con [DireccionEntity] usando [id].
 */
@Keep
@Entity(
    tableName = "sucursales",
    indices = [
        Index(value = ["idEmpresaPadre"]),
        Index(value = ["idPropietario"])
    ]
)
data class SucursalEntity(
    @PrimaryKey val id: String, // UUID único
    val idEmpresaPadre: String, // Vínculo con EmpresaEntity
    val idPropietario: String, // Vínculo con CuentaEntity
    
    // --- SECTOR: OPERATIVO ---
    val nombre: String = "",
    val descripcion: String = "",
    val numeroTelefono: String = "",

    // --- SECTOR: MÉTRICAS LOCALES ---
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosRealizados: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val estaEnLinea: Boolean = false,

    // --- SECTOR: FLAGS DE SOBERANÍA (CAPACIDADES POS) ---
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val atiende24Horas: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val realizaEnvios: Boolean = false,
    val brindaTurnos: Boolean = false,
    val usaAgendaRecursos: Boolean = false,
    val capacidadSimultanea: Int = 1,
    
    // --- AUDITORÍA ---
    val ultimaSincronizacion: Long = System.currentTimeMillis(),
    val fechaCreacion: Long = System.currentTimeMillis()
)

