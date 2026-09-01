package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.myapplication.core.dominio.modelos.TipoDireccion
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- DIRECCIÓN (Atómico) ---
 * 
 * [PROPÓSITO]: Persistir la ubicación física exacta de cualquier entidad del 
 * ecosistema (Usuario, Empresa o Sucursal).
 * 
 * [FUNCIONAMIENTO]: Almacena datos geográficos detallados. Se vincula mediante 
 * IDs al pilar correspondiente.
 * 
 * [RELACIÓN]: Provee la base para el espejado táctico de geolocalización. 
 */
@IgnoreExtraProperties
@Entity(
    tableName = "direcciones",
    indices = [
        Index(value = ["idPropietario"]),
        Index(value = ["idSucursal"]),
        Index(value = ["idReferencia"])
    ]
)
data class DireccionEntity(
    @PrimaryKey val id: String = "",
    val idPropietario: String = "", // [PROPIETARIO]: Dueño raíz (Usuario/Prestador/Empresa).
    val idSucursal: String? = null, // [SUCURSAL]: ID específico para POS/Sucursales.
    val idReferencia: String? = null, // [REFERENCIA]: Comodín para Mensajes, Presupuestos, etc.

    val tieneLocalFisico: Boolean = false,
    val tipo: TipoDireccion = TipoDireccion.PERFIL_USUARIO,

    // --- SECTOR: DATOS GEOGRÁFICOS ---
    val etiqueta: String = "",
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val departamento: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val pais: String = "Argentina",
    val codigoPostal: String = "",

    // --- SECTOR: VERIFICACIÓN ---
    val estaVerificadaGps: Boolean = false,
    val precisionGps: Float = 0f,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val geohash: String = "",

    // --- SINCRO ---
    val ultimaSincronizacion: Long = System.currentTimeMillis()
) {
    // --- ALIASES DE COMPATIBILIDAD ---
    @get:Exclude
    val calleYNumero: String get() = "$calle $numero".trim()
}

