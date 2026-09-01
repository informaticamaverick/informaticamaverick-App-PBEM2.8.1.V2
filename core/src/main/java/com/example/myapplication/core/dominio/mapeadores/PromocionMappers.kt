package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.dominio.modelos.*
import com.google.firebase.firestore.DocumentSnapshot
import java.util.concurrent.TimeUnit

/**
 * --- PROMOCIÓN MAPPER (ELITE v2026.8) ---
 * [ELITE SSOT]: Centraliza la transformación de Promociones e Historias.
 * Implementa el formateo de tiempo relativo para el PromocionDominio.
 */
object PromocionMappers {

    /**
     * Convierte una entidad local en un Mapa optimizado para Firestore (Shallow).
     */
    fun aMapaFirestore(promo: Promocion): Map<String, Any?> {
        return mapOf(
            "id" to promo.id,
            "idPrestador" to promo.idPrestador,
            "idEmpresa" to promo.idEmpresa,
            "idSucursal" to promo.idSucursal,
            "titulo" to promo.titulo,
            "descripcion" to promo.descripcion,
            "tipo" to promo.tipo.name,
            "urlImagenes" to promo.urlImagenes,
            "idCategorias" to promo.idCategorias,
            "codigoPostal" to promo.codigoPostal,
            "porcentajeDescuento" to promo.porcentajeDescuento,
            "etiquetaPromocion" to promo.etiquetaPromocion,
            "fechaCreacion" to promo.fechaCreacion,
            "fechaExpiracion" to promo.fechaExpiracion,
            "estado" to promo.estado.name,
            "tipoPromocion" to promo.tipoPromocion.name,
            "conteoLikes" to promo.conteoLikes,
            "conteoVistas" to promo.conteoVistas,
            "conteoComentarios" to promo.conteoComentarios,
            "filtrosBusqueda" to promo.filtrosBusqueda,
            
            // --- Datos Shallow del Prestador (Ley #2) ---
            "nombrePrestador" to promo.nombrePrestador,
            "urlFotoPrestador" to promo.urlFotoPrestador,
            "reputacion" to promo.reputacion,
            "estaVerificado" to promo.estaVerificado,
            "estaSuscrito" to promo.estaSuscrito
        )
    }

    /**
     * Convierte un documento de Firestore en un objeto de dominio.
     */
    fun desdeFirestore(doc: DocumentSnapshot): Promocion? {
        if (!doc.exists()) return null
        return try {
            val d = doc.data ?: return null
            Promocion(
                id = d["id"] as? String ?: doc.id,
                idPrestador = d["idPrestador"] as? String ?: "",
                idEmpresa = d["idEmpresa"] as? String,
                idSucursal = d["idSucursal"] as? String,
                titulo = d["titulo"] as? String ?: "",
                descripcion = d["descripcion"] as? String ?: "",
                tipo = TipoPromocion.desdeNombre(d["tipo"] as? String ?: "PROMOCION"),
                urlImagenes = (d["urlImagenes"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                idCategorias = (d["idCategorias"] as? List<*>)?.map { it.toString() } ?: (d["categorias"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                codigoPostal = d["codigoPostal"] as? String,
                porcentajeDescuento = (d["porcentajeDescuento"] as? Number)?.toInt(),
                etiquetaPromocion = d["etiquetaPromocion"] as? String,
                fechaCreacion = (d["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                fechaExpiracion = (d["fechaExpiracion"] as? Number)?.toLong() ?: 0L,
                estado = EstadoPromocion.desdeNombre(d["estado"] as? String ?: "ACTIVA"),
                tipoPromocion = TipoCategoriaPromo.desdeNombre(d["tipoPromocion"] as? String ?: "SERVICIO"),
                conteoLikes = (d["conteoLikes"] as? Number)?.toInt() ?: 0,
                conteoVistas = (d["conteoVistas"] as? Number)?.toInt() ?: 0,
                conteoComentarios = (d["conteoComentarios"] as? Number)?.toInt() ?: 0,
                filtrosBusqueda = (d["filtrosBusqueda"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                nombrePrestador = d["nombrePrestador"] as? String ?: "Prestador",
                urlFotoPrestador = d["urlFotoPrestador"] as? String,
                reputacion = (d["reputacion"] as? Number)?.toFloat() ?: 0f,
                estaVerificado = d["estaVerificado"] as? Boolean ?: false,
                estaSuscrito = d["estaSuscrito"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            android.util.Log.e("PromocionMappers", "❌ Error al mapear promo: ${e.message}")
            null
        }
    }

    /**
     * Transforma un objeto de dominio en un Modelo de UI listo para Compose.
     */
    fun aUiModel(p: Promocion): PromocionDominio {
        val ahora = System.currentTimeMillis()
        val esHistoria = p.tipo == TipoPromocion.HISTORIA
        
        return PromocionDominio(
            id = p.id,
            idPrestador = p.idPrestador,
            titulo = p.titulo,
            descripcion = p.descripcion,
            urlImagen = p.urlImagenes.firstOrNull(),
            urlMiniaturaPrestador = p.urlFotoPrestador,
            nombrePrestador = p.nombrePrestador,
            reputacion = p.reputacion,
            estaVerificado = p.estaVerificado,
            tiempoRelativo = calcularTiempoRelativo(p.fechaCreacion, p.fechaExpiracion, esHistoria),
            etiquetaOferta = p.etiquetaPromocion ?: p.porcentajeDescuento?.let { "$it% OFF" },
            esHistoria = esHistoria,
            leGustaAlUsuario = p.leGustaAlUsuario,
            conteoLikes = p.conteoLikes,
            esNuevo = (ahora - p.fechaCreacion) < TimeUnit.HOURS.toMillis(12)
        )
    }

    private fun calcularTiempoRelativo(creacion: Long, expiracion: Long, esHistoria: Boolean): String {
        val ahora = System.currentTimeMillis()
        
        return if (esHistoria) {
            val restante = expiracion - ahora
            if (restante <= 0) "Expirado"
            else {
                val horas = TimeUnit.MILLISECONDS.toHours(restante)
                if (horas > 0) "Faltan ${horas}h" 
                else "Faltan ${TimeUnit.MILLISECONDS.toMinutes(restante)}min"
            }
        } else {
            val transcurrido = ahora - creacion
            when {
                transcurrido < TimeUnit.MINUTES.toMillis(1) -> "Justo ahora"
                transcurrido < TimeUnit.HOURS.toMillis(1) -> "Hace ${TimeUnit.MILLISECONDS.toMinutes(transcurrido)}min"
                transcurrido < TimeUnit.DAYS.toMillis(1) -> "Hace ${TimeUnit.MILLISECONDS.toHours(transcurrido)}h"
                else -> "Hace ${TimeUnit.MILLISECONDS.toDays(transcurrido)}d"
            }
        }
    }
}



