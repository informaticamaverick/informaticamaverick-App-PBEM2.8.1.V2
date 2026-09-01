package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep

/**
 * --- COMPONENTES COMUNES DE PRESUPUESTO (v2026.ELITE) ---
 */

@Keep
enum class EstadoPresupuesto { PENDIENTE, ACEPTADO, RECHAZADO, PAGADO, VENCIDO }

@Keep
enum class TipoProducto { PRODUCTO, SERVICIO, GASTO }

/**
 * --- DTO COMPARTIDO PARA TRÁNSITO DE PRODUCTOS (v2026.ELITE) ---
 */
@Keep
data class ProductoEliteSnapshot(
    val id: String,
    val idPropietario: String,
    val nombre: String,
    val descripcion: String = "",
    val precioVenta: Double = 0.0,
    val precioCosto: Double = 0.0,
    val idCategoria: String = "GENERAL",
    val tipo: TipoProducto = TipoProducto.PRODUCTO,
    val urlImagen: String? = null,
    val miniaturaBase64: String? = null,
    val stockActual: Int = 0,
    val impuestoDefault: Double = 0.0,
    val descuentoDefault: Double = 0.0,
    val sku: String? = null
)

@Keep
data class ArticuloPresupuesto(
    val id: Long = System.currentTimeMillis(),
    val idProducto: String? = null,
    val codigo: String = "",
    val descripcion: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val precioCosto: Double = 0.0,
    val porcentajeImpuesto: Double = 0.0,
    val porcentajeDescuento: Double = 0.0,
    val montoDescuento: Double = 0.0,
    val porcentajeInteres: Double = 0.0,
    val montoInteres: Double = 0.0,
    val urlImagen: String? = null,
    val miniaturaBase64: String? = null
)

@Keep
data class ServicioPresupuesto(
    val id: Long = System.currentTimeMillis(),
    val idProducto: String? = null,
    val codigo: String = "",
    val descripcion: String = "",
    val precioUnitario: Double = 0.0,
    val porcentajeDescuento: Double = 0.0,
    val montoDescuento: Double = 0.0,
    val porcentajeInteres: Double = 0.0,
    val montoInteres: Double = 0.0,
    val total: Double = 0.0,
    val urlImagen: String? = null,
    val miniaturaBase64: String? = null
)

@Keep
data class GastoVarioPresupuesto(
    val id: Long = System.currentTimeMillis(),
    val descripcion: String = "",
    val precioUnitario: Double = 0.0,
    val porcentajeDescuento: Double = 0.0,
    val montoDescuento: Double = 0.0,
    val porcentajeInteres: Double = 0.0,
    val montoInteres: Double = 0.0,
    val monto: Double = 0.0
)

@Keep
data class ImpuestoPresupuesto(
    val id: Long = System.currentTimeMillis(),
    val descripcion: String = "",
    val monto: Double = 0.0
)


