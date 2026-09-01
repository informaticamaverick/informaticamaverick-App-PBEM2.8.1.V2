package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- MODELO DE CONCURSO PÚBLICO (v2026.ELITE) ---
 * [PROPÓSITO]: Representar una licitación o proyecto publicado por el cliente.
 * [LEY #9]: Estándar Mav en Español.
 * [LEY #14]: El Embudo. Soporta filtrado por SQL.
 */
@Keep
@Entity(
    tableName = "concursos_publicos",
    indices = [
        Index(value = ["idCategoria"]),
        Index(value = ["estado"]),
        Index(value = ["idCliente"])
    ]
)
data class ConcursoPublicoEntity(
    @PrimaryKey val idConcurso: String = "",
    val titulo: String = "",
    val idCliente: String = "",
    val descripcion: String = "",
    val idCategoria: String = "",
    val estado: String = "ABIERTA",
    val estaActivo: Boolean = true,
    val marcaTiempo: Long = System.currentTimeMillis(),
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaFin: Long = 0,
    val conteoPresupuestos: Int = 0,
    val fechaCancelacion: Long? = null,
    val idPrestadorAdjudicado: String? = null,
    val nombrePrestadorAdjudicado: String? = null,
    val idPresupuestoAdjudicado: String? = null,
    val urlFotoPrestadorAdjudicado: String? = null,
    val miniaturaPrestadorAdjudicado: String? = null,
    val numeroPresupuesto: String? = null,
    val tituloTrabajo: String? = null,
    val exigeVisita: Boolean = false,
    val exigeMetodoPago: Boolean = false,
    val exigeGarantia: Boolean = false,
    val exigeDocPrestador: Boolean = false,
    val direccionCalle: String? = null,
    val direccionNumero: String? = null,
    val direccionLocalidad: String? = null,
    val direccionCodigoPostal: String? = null,
    val tipoUbicacion: String? = null,
    val nombreCliente: String? = null,
    val idEmpresa: String? = null,
    val nombreEmpresa: String? = null,
    val idSucursal: String? = null,
    val nombreSucursal: String? = null,
    val miniaturaCliente: String? = null,
    val estaSuscrito: Boolean = false,
    val tieneMiPresupuesto: Boolean = false,
    val fechaExpiracion: Long? = null,
    val claveBusqueda: String? = null,
    val filtrosBusqueda: List<String> = emptyList(),
    val urlImagenes: List<String> = emptyList()
)
