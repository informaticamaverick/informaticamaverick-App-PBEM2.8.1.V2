package com.example.myapplication.prestador.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.myapplication.core.datos.local.entidades.TipoProducto

/**
 * --- TABLA UNIFICADA DE ÍTEMS: CATÁLOGO + PRESUPUESTOS (v2026.ELITE) ---
 */
@Keep
@Entity(
    tableName = "productos",
    indices = [
        Index(value = ["idPropietario"]),
        Index(value = ["idCategoria"]),
        Index(value = ["idPresupuesto"]),
        Index(value = ["idBorrador"]),
        Index(value = ["sku"])
    ]
)
data class ProductoEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val idPropietario: String, // ID del Prestador/Empresa
    
    // --- VÍNCULOS DE TRANSACCIÓN (Null = Ítem del Catálogo) ---
    val idPresupuesto: String? = null,
    val idBorrador: String? = null,
    val idOriginal: String? = null, // 🔥 [SUPREME] Referencia al catálogo maestro

    // --- SECTOR: DATOS DEL ÍTEM ---
    val nombre: String,
    val descripcion: String = "",
    val precioCosto: Double = 0.0,
    val precioVenta: Double = 0.0,
    val cantidad: Int = 1, // Para presupuestos
    val impuestoDefault: Double = 0.0,
    val descuentoDefault: Double = 0.0,
    val interesDefault: Double = 0.0,
    val moneda: String = "ARS",
    val sku: String? = null,
    val codigoBarras: String? = null,
    val idCategoria: String = "GENERAL",

    // --- SECTOR: GESTIÓN ---
    val stockActual: Int = 0,
    val stockMinimo: Int = 0,
    val tipo: TipoProducto = TipoProducto.PRODUCTO,
    val estaActivo: Boolean = true,

    // --- SECTOR: MULTIMEDIA ---
    val urlImagen: String? = null,
    val miniaturaBase64: String? = null,

    // --- SECTOR: AUDITORÍA ---
    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimaSincronizacion: Long = System.currentTimeMillis()
)


