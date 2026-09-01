package com.example.myapplication.prestador.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.Keep
import com.example.myapplication.core.datos.local.entidades.TipoPresupuesto

/**
 * --- CABECERA DE BORRADOR - COCINA PRIVADA (v2026.ELITE) ---
 * [PROPÓSITO]: Soportar el auto-guardado mientras el prestador redacta.
 * [LEY #16]: Tabla Tablita Tablón. Los ítems viven en la tabla productos_mav.
 */
@Keep
@Entity(tableName = "borradores_presupuesto")
data class BorradorPresupuestoEntity(
    @PrimaryKey val idBorrador: String, // Usualmente idCliente o idConcurso
    val idPrestador: String,
    val idDireccionCliente: String? = null,
    val tituloTrabajo: String = "",
    val idCategoria: String? = null,
    val tipo: TipoPresupuesto = TipoPresupuesto.NUEVO,
    
    val subtotal: Double = 0.0,
    val totalGeneral: Double = 0.0,
    
    val idIdentidadEmisora: String? = null,
    val direccionManual: String? = null,
    val metodosPago: String? = null,
    val diasValidez: Int = 15,
    val notas: String? = null,
    val numeroPresupuesto: String? = null,
    
    // Campos de dirección
    val calleManual: String? = null,
    val numeroManual: String? = null,
    val pisoManual: String? = null,
    val deptoManual: String? = null,
    val localidadManual: String? = null,
    val provinciaManual: String? = null,
    val cpManual: String? = null,

    val ultimaModificacion: Long = System.currentTimeMillis()
)

