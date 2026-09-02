package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- IDENTIDAD PRESTADOR MAVERICK (Pilar #2 - El Humano Profesional) ---
 * 
 * [PROPÓSITO]: Gestionar el perfil profesional del trabajador independiente o referente 
 * de empresa, acumulando métricas de reputación y flags de capacidad.
 * 
 * [FUNCIONAMIENTO]: Almacena datos soberanos del profesional. Se vincula a [CuentaEntity] 
 * 1:1. Permite el funcionamiento de la App sin necesidad de una Empresa legal.
 * 
 * [RELACIÓN]: Es el pilar fundamental del 'Ecosistema Profesional'. Sus flags (24hs, 
 * Domicilio) son utilizados por el motor de búsqueda para filtrar resultados al Cliente.
 */
@Keep
@IgnoreExtraProperties
@Entity(
    tableName = "prestadores",
    indices = [
        Index(value = ["cuitCuil"])
    ]
)
data class IdentidadPrestadorEntity(
    @PrimaryKey val id: String = "", // UID de Firebase
    
    // --- SECTOR: DATOS PERSONALES ---
    val nombre: String = "",
    val apellido: String = "",
    val nombreVisible: String = "",
    val biografia: String = "",
    val cuitCuil: String = "",
    val correoElectronico: String = "",
    val numeroTelefono: String = "",
    val urlFotoPerfil: String? = null,
    val miniaturaBase64: String? = null,
    
    // --- SECTOR: COMPETENCIAS ---
    val idCategorias: List<String> = emptyList(),
    val especialidades: String? = null,
    val matricula: String? = null,
    val matriculaFotoUrl: String? = null,

    // --- SECTOR: MÉTRICAS Y ESTATUS ---
    val reputacion: Float = 5f, // Arranca en 5.0, baja según cómo lo califique el cliente
    val totalReseñas: Int = 0,
    val trabajosRealizados: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val nivelElite: Int = 0,
    val estaVerificado: Boolean = false,
    val estaEnLinea: Boolean = false,

    // --- SECTOR: FLAGS DE SOBERANÍA PERSONAL ---
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val atiende24Horas: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val realizaEnvios: Boolean = false,
    val brindaTurnos: Boolean = false,
    val usaAgendaRecursos: Boolean = false,
    val capacidadSimultanea: Int = 1,

    // --- SECTOR: DESCUBRIMIENTO (Ley #4) ---
    val tieneLocalFisico: Boolean = false,

    // --- CONTROL DE CARGA ---
    val esCargaCompleta: Boolean = false,

    // --- AUDITORÍA ---
    val ultimaSincronizacion: Long = System.currentTimeMillis(),
    val fechaCreacion: Long = System.currentTimeMillis()
)

