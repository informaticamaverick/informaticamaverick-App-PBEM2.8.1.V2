package com.example.myapplication.core.dominio.mapeadores.discovery

import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.modelos.discovery.IndicePromocionShallowDominio
import com.example.myapplication.core.dominio.mapeadores.shallow.PrestadorShallowMappers
import com.example.myapplication.core.dominio.modelos.shallow.PrestadorShallowDominio
import com.google.firebase.firestore.DocumentSnapshot

/**
 * --- ÍNDICE PROMOCIÓN SHALLOW MAPPER (v2026.ELITE) ---
 */
object IndicePromocionShallowMappers {

    fun deDominioAShallow(
        promo: Promocion,
        prestadorShallow: PrestadorShallowDominio
    ): IndicePromocionShallowDominio {
        return IndicePromocionShallowDominio(
            idPromocion = promo.id,
            idPropietario = prestadorShallow.idPropietario.ifBlank { prestadorShallow.id },
            tipoIdentidad = "PROMO",
            titulo = promo.titulo,
            descripcion = promo.descripcion,
            urlImagenes = promo.urlImagenes,
            tipo = promo.tipo,
            estado = promo.estado,
            tipoPromocion = promo.tipoPromocion,
            porcentajeDescuento = promo.porcentajeDescuento,
            idCategorias = promo.idCategorias,
            fechaCreacion = promo.fechaCreacion,
            fechaExpiracion = promo.fechaExpiracion,
            filtrosBusqueda = promo.filtrosBusqueda,
            emisor = prestadorShallow
        )
    }

    fun deDominioAMapa(dominio: IndicePromocionShallowDominio): Map<String, Any?> {
        fun procesarCampoImagen(campo: Any?): String? {
            return when (campo) {
                is ByteArray -> com.example.myapplication.core.utilidades.ImageUtils.bytesToBase64(campo)
                else -> {
                    val s = campo?.toString()
                    if (s?.startsWith("[B@") == true) null else s
                }
            }
        }

        val miniLimpia = procesarCampoImagen(dominio.emisor.miniaturaBase64 ?: dominio.emisor.urlFoto)

        return mapOf(
            "id" to dominio.idPromocion,
            "idPropietario" to dominio.idPropietario,
            "tipoIdentidad" to dominio.tipoIdentidad,
            "titulo" to dominio.titulo,
            "descripcion" to dominio.descripcion,
            "urlImagenes" to dominio.urlImagenes,
            "tipo" to dominio.tipo.name,
            "estado" to dominio.estado.name,
            "tipoPromocion" to dominio.tipoPromocion.name,
            "porcentajeDescuento" to dominio.porcentajeDescuento,
            "idCategorias" to dominio.idCategorias,
            "fechaCreacion" to dominio.fechaCreacion,
            "fechaExpiracion" to dominio.fechaExpiracion,
            "filtrosBusqueda" to dominio.filtrosBusqueda,
            "nombreVisible" to dominio.emisor.nombreVisible,
            "urlMiniatura" to miniLimpia,
            "miniaturaBase64" to miniLimpia,
            "emisor" to PrestadorShallowMappers.deDominioAMapa(dominio.emisor).toMutableMap().apply {
                this["miniaturaBase64"] = miniLimpia
                this["urlFoto"] = miniLimpia
            }
        )
    }

    fun desdeFirestore(doc: DocumentSnapshot): IndicePromocionShallowDominio? {
        if (!doc.exists()) return null
        return try {
            val d = doc.data ?: return null
            val emisorMap = d["emisor"] as? Map<*, *>
            
            val nombreEmisor = d["nombreVisible"] as? String ?: emisorMap?.get("nombreVisible") as? String ?: ""
            val miniEmisor = d["miniaturaBase64"] as? String ?: d["urlMiniatura"] as? String ?: emisorMap?.get("miniaturaBase64") as? String ?: emisorMap?.get("urlFoto") as? String
            val idPropietario = d["idPropietario"] as? String ?: emisorMap?.get("idPropietario") as? String ?: emisorMap?.get("id") as? String ?: ""

            IndicePromocionShallowDominio(
                idPromocion = d["id"] as? String ?: doc.id,
                idPropietario = idPropietario,
                tipoIdentidad = d["tipoIdentidad"] as? String ?: "PROMO",
                titulo = d["titulo"] as? String ?: "",
                descripcion = d["descripcion"] as? String ?: "",
                urlImagenes = (d["urlImagenes"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                tipo = TipoPromocion.desdeNombre(d["tipo"] as? String ?: "PROMOCION"),
                estado = EstadoPromocion.desdeNombre(d["estado"] as? String ?: "ACTIVA"),
                tipoPromocion = TipoCategoriaPromo.desdeNombre(d["tipoPromocion"] as? String ?: "SERVICIO"),
                porcentajeDescuento = (d["porcentajeDescuento"] as? Number)?.toInt(),
                idCategorias = (d["idCategorias"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                fechaCreacion = (d["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                fechaExpiracion = (d["fechaExpiracion"] as? Number)?.toLong() ?: 0L,
                filtrosBusqueda = (d["filtrosBusqueda"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                emisor = PrestadorShallowDominio(
                    id = emisorMap?.get("id") as? String ?: idPropietario,
                    idPropietario = idPropietario,
                    nombreVisible = nombreEmisor,
                    urlFoto = miniEmisor,
                    miniaturaBase64 = miniEmisor,
                    reputacion = (emisorMap?.get("reputacion") as? Number)?.toFloat() ?: 0f,
                    estaEnLinea = emisorMap?.get("estaEnLinea") as? Boolean ?: false,
                    estaSuscrito = emisorMap?.get("estaSuscrito") as? Boolean ?: false
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("IndicePromocionMappers", "❌ Error al mapear desde Firestore: ${e.message}")
            null
        }
    }

    fun deShallowAEntidad(dominio: IndicePromocionShallowDominio): com.example.myapplication.core.datos.local.entidades.PromocionEntity {
        val gson = com.google.gson.Gson()
        return com.example.myapplication.core.datos.local.entidades.PromocionEntity(
            id = dominio.idPromocion,
            idPrestador = dominio.emisor.idPropietario,
            idEmpresa = if (dominio.emisor.id != dominio.emisor.idPropietario) dominio.emisor.id else null,
            nombrePrestador = dominio.emisor.nombreVisible,
            urlFotoPrestador = (dominio.emisor.miniaturaBase64 ?: dominio.emisor.urlFoto)?.toString(),
            estaVerificado = false, 
            estaSuscrito = dominio.emisor.estaSuscrito,
            tipo = if (dominio.tipo == TipoPromocion.HISTORIA) "STORY" else "PROMOTION",
            titulo = dominio.titulo,
            descripcion = dominio.descripcion,
            imageUrlsJson = gson.toJson(dominio.urlImagenes),
            filtrosBusquedaJson = gson.toJson(dominio.filtrosBusqueda),
            porcentajeDescuento = dominio.porcentajeDescuento,
            fechaCreacion = dominio.fechaCreacion,
            fechaExpiracion = dominio.fechaExpiracion,
            estado = dominio.estado.name,
            tipoPromocion = dominio.tipoPromocion.name,
            reputacion = dominio.emisor.reputacion
        )
    }
}
