package com.example.myapplication.core.dominio.modelos.discovery

import androidx.annotation.Keep
import com.example.myapplication.core.dominio.modelos.shallow.PrestadorShallowDominio
import com.example.myapplication.core.dominio.modelos.TipoPromocion
import com.example.myapplication.core.dominio.modelos.EstadoPromocion
import com.example.myapplication.core.dominio.modelos.TipoCategoriaPromo

/**
 * --- ÍNDICE DE DESCUBRIMIENTO: PROMOCIÓN SHALLOW (v2026.ELITE) ---
 * [PROPÓSITO]: Agrupar los datos de la oferta con la identidad del emisor para el índice.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class IndicePromocionShallowDominio(
    val idPromocion: String = "",
    val idPropietario: String = "", // 🔥 [ELITE] Alineación con reglas de seguridad
    val tipoIdentidad: String = "PROMO", // 🔥 [ELITE] Alineación con reglas de seguridad
    val titulo: String = "",
    val descripcion: String = "",
    val urlImagenes: List<String> = emptyList(),
    val tipo: TipoPromocion = TipoPromocion.PROMOCION,
    val estado: EstadoPromocion = EstadoPromocion.ACTIVA,
    val tipoPromocion: TipoCategoriaPromo = TipoCategoriaPromo.SERVICIO,
    val porcentajeDescuento: Int? = null,
    val idCategorias: List<String> = emptyList(), // 🔥 [ELITE] Rubros específicos de la oferta
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaExpiracion: Long = System.currentTimeMillis(),
    val filtrosBusqueda: List<String> = emptyList(),
    
    // --- EL CORAZÓN: IDENTIDAD EMBEBIDA ---
    val emisor: PrestadorShallowDominio = PrestadorShallowDominio()
)

