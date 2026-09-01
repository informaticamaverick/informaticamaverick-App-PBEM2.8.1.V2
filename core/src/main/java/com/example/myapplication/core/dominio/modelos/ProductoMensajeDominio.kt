package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO: PRODUCTO EN MENSAJERÍA (v2026.ELITE) ---
 * [PROPÓSITO]: Snapshot comercial de un producto enviado en un chat.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class ProductoMensajeDominio(
    val idProducto: String,
    val idMensajeOriginal: String? = null,
    val titulo: String,
    val descripcion: String = "",
    val marca: String = "App",
    val idCategoria: String = "GENERAL",
    val esServicio: Boolean = false,
    val urlImagen: String = "",
    val miniaturaBase64: String? = null,
    val precioActual: Double = 0.0,
    val precioAnterior: Double? = null,
    val porcentajeDescuento: Int = 0,
    val cuotasTexto: String = "",
    val envioGratis: Boolean = false,
    val costoEnvio: Double? = null,
    val tipoEnvio: String = "CONVENIR", // CONVENIR, COBRO_CLIENTE, GRATIS
    val metodoPago: String = "EFECTIVO", // EFECTIVO, TARJETA_SIN_INTERES, TARJETA_INTERES, TRANSFERENCIA
    val estaSolicitado: Boolean = false
)
