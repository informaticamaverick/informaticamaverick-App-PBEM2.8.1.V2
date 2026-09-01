package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.myapplication.core.dominio.modelos.*

/**
 * --- ENTIDAD DE PERSISTENCIA: PROMOCIONES (SSOT 2026) ---
 */
@Entity(
    tableName = "promociones",
    indices = [
        Index(value = ["idPrestador"]),
        Index(value = ["fechaExpiracion"]),
        Index(value = ["idPrestador", "fechaCreacion"]),
        Index(value = ["idCategoria"]),
        Index(value = ["geohash"])
    ]
)
data class PromocionEntity(
    @PrimaryKey
    val id: String,
    val idPrestador: String,
    val idEmpresa: String? = null,
    val idSucursal: String? = null,
    val nombrePrestador: String,
    val urlFotoPrestador: String? = null,
    val estaVerificado: Boolean = false,
    val estaSuscrito: Boolean = false, 
    
    val tipo: String, 
    val titulo: String,
    val descripcion: String,
    
    val imageUrlsJson: String = "[]",
    val tagsJson: String = "[]",
    val idCategoriasJson: String = "[]",
    val idCategoria: String? = null,
    val geohash: String? = null,
    val filtrosBusquedaJson: String = "[]",
    val codigoPostal: String? = null,
    
    val porcentajeDescuento: Int? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaExpiracion: Long = 0,
    
    val estado: String = "ACTIVA", 
    val tipoPromocion: String = "SERVICIO", 
    val etiquetaPromocion: String? = null, 
    val conteoLikes: Int = 0,
    val conteoVistas: Int = 0,
    val conteoComentarios: Int = 0,
    val reputacion: Float = 0f
) {
    fun aModelo(): Promocion {
        val gson = com.google.gson.Gson()
        val typeListString = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        
        return Promocion(
            id = id,
            idPrestador = idPrestador,
            idEmpresa = idEmpresa,
            idSucursal = idSucursal,
            nombrePrestador = nombrePrestador,
            urlFotoPrestador = urlFotoPrestador,
            estaVerificado = estaVerificado,
            estaSuscrito = estaSuscrito,
            tipo = if (tipo == "STORY") TipoPromocion.HISTORIA else TipoPromocion.PROMOCION,
            titulo = titulo,
            descripcion = descripcion,
            urlImagenes = try { gson.fromJson(imageUrlsJson, typeListString) } catch (e: Exception) { emptyList() },
            idCategorias = try { gson.fromJson(idCategoriasJson, typeListString) } catch (e: Exception) { emptyList() },
            filtrosBusqueda = try { gson.fromJson(filtrosBusquedaJson, typeListString) } catch (e: Exception) { emptyList() },
            codigoPostal = codigoPostal,
            porcentajeDescuento = porcentajeDescuento,
            fechaCreacion = fechaCreacion,
            fechaExpiracion = fechaExpiracion,
            estado = EstadoPromocion.desdeNombre(estado),
            tipoPromocion = if (tipoPromocion == "PRODUCTO") TipoCategoriaPromo.PRODUCTO else TipoCategoriaPromo.SERVICIO,
            etiquetaPromocion = etiquetaPromocion,
            conteoLikes = conteoLikes,
            conteoVistas = conteoVistas,
            conteoComentarios = conteoComentarios,
            leGustaAlUsuario = false, // Se resolverá vía joined DTO en el DAO si es necesario
            reputacion = reputacion
        )
    }
}

fun Promocion.toEntity(): PromocionEntity {
    val gson = com.google.gson.Gson()
    return PromocionEntity(
        id = id,
        idPrestador = idPrestador,
        idEmpresa = idEmpresa,
        idSucursal = idSucursal,
        nombrePrestador = nombrePrestador,
        urlFotoPrestador = urlFotoPrestador,
        estaVerificado = estaVerificado,
        estaSuscrito = estaSuscrito,
        tipo = if (tipo == TipoPromocion.HISTORIA) "STORY" else "PROMOTION",
        titulo = titulo,
        descripcion = descripcion,
        imageUrlsJson = gson.toJson(urlImagenes),
        tagsJson = "[]",
        idCategoriasJson = gson.toJson(idCategorias),
        idCategoria = idCategorias.firstOrNull(),
        geohash = null, 
        filtrosBusquedaJson = gson.toJson(filtrosBusqueda),
        codigoPostal = codigoPostal,
        porcentajeDescuento = porcentajeDescuento,
        fechaCreacion = fechaCreacion,
        fechaExpiracion = fechaExpiracion,
        estado = estado.name,
        tipoPromocion = if (tipoPromocion == TipoCategoriaPromo.PRODUCTO) "PRODUCTO" else "SERVICIO",
        etiquetaPromocion = etiquetaPromocion,
        conteoLikes = conteoLikes,
        conteoVistas = conteoVistas,
        conteoComentarios = conteoComentarios,
        reputacion = reputacion
    )
}
