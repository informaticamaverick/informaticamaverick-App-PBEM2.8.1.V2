package com.example.myapplication.prestador.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.core.datos.local.entidades.TipoPresupuesto

/**
 * --- CABECERA DE PRESUPUESTO - COCINA PRIVADA (v2026.ELITE) ---
 * [PROPÓSITO]: Representar el folder contenedor de una oferta en el taller del prestador.
 * [LEY #16]: Tabla Tablita Tablón. Se eliminan las listas embebidas.
 */
@Keep
@Entity(
    tableName = "presupuestos",
    indices = [
        Index(value = ["idConcurso"]),
        Index(value = ["idPrestador"]),
        Index(value = ["idCliente"]),
        Index(value = ["idCategoria"])
    ]
)
data class PresupuestoEntity(
    @PrimaryKey val idPresupuesto: String = java.util.UUID.randomUUID().toString(),
    val idCliente: String = "",
    val idPrestador: String = "",
    val idConcurso: String? = null,
    val idCategoria: String? = null,
    val tipo: TipoPresupuesto = TipoPresupuesto.NUEVO,
    
    // Metadatos Visuales
    val nombrePrestador: String = "",
    val nombreEmpresaPrestador: String? = null,
    val urlFotoPrestador: String? = null,
    val urlMiniatura: String? = null,
    
    val numeroPresupuesto: String? = null,
    val tituloTrabajo: String? = null,
    
    // Totales Calculados
    val subtotal: Double = 0.0,
    val montoImpuestos: Double = 0.0,
    val montoDescuento: Double = 0.0,
    val montoInteres: Double = 0.0,
    val totalGeneral: Double = 0.0,
    
    // Cláusulas
    val diasValidez: Int = 7,
    val notas: String? = null,
    val metodosPago: String? = null,
    val infoGarantia: String? = null,
    val tiempoEjecucion: String? = null,
    
    val estado: EstadoPresupuesto = EstadoPresupuesto.PENDIENTE,
    val leido: Boolean = false,
    val marcaTiempo: Long = System.currentTimeMillis(),
    val ultimaActualizacion: Long = System.currentTimeMillis()
)


