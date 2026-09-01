package com.example.myapplication.core.dominio.mapeadores.shallow

import com.example.myapplication.core.datos.local.entidades.SucursalEntity
import com.example.myapplication.core.datos.local.entidades.EmpresaEntity
import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import com.example.myapplication.core.dominio.modelos.shallow.SucursalShallowDominio
import com.example.myapplication.core.utilidades.ImageUtils

/**
 * --- SUCURSAL SHALLOW MAPPER (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Transformar entre Entidad de Sucursal y su versión ligera para búsqueda.
 * [LEY #17]: Protocolo de Bautizo.
 */
object SucursalShallowMappers {

    fun deEntidadADominio(
        sucursal: SucursalEntity,
        empresa: EmpresaEntity,
        direccion: DireccionEntity? = null,
        estaSuscrito: Boolean = false,
        tags: List<String> = emptyList()
    ): SucursalShallowDominio {
        return SucursalShallowDominio(
            id = sucursal.id,
            idPropietario = sucursal.idPropietario,
            idEmpresaPadre = sucursal.idEmpresaPadre,
            nombreSucursal = sucursal.nombre,
            nombreEmpresa = empresa.nombre,
            urlFoto = empresa.urlFoto,
            miniaturaBase64 = empresa.miniaturaBase64,
            reputacion = sucursal.reputacion,
            estaEnLinea = sucursal.estaEnLinea,
            estaSuscrito = estaSuscrito,
            brindaServicio = sucursal.brindaServicio,
            brindaProducto = sucursal.brindaProducto,
            atiende24Horas = sucursal.atiende24Horas,
            visitaADomicilio = sucursal.visitaADomicilio,
            realizaEnvios = sucursal.realizaEnvios,
            calle = direccion?.calle ?: "",
            numero = direccion?.numero ?: "",
            codigoPostal = direccion?.codigoPostal ?: "",
            latitud = direccion?.latitud ?: 0.0,
            longitud = direccion?.longitud ?: 0.0,
            idCategorias = empresa.idCategorias,
            filtrosBusqueda = tags
        )
    }

    fun deDominioAMapa(dominio: SucursalShallowDominio): Map<String, Any?> {
        // [SUPREME.FIX]: Limpieza de punteros de memoria [B@... y conversión a Base64 real
        fun procesarCampoImagen(campo: Any?): String? {
            return when (campo) {
                is ByteArray -> ImageUtils.bytesToBase64(campo)
                else -> {
                    val s = campo?.toString()
                    if (s?.startsWith("[B@") == true) null else s
                }
            }
        }

        val miniLimpia = procesarCampoImagen(dominio.miniaturaBase64)
        val fotoLimpia = if (dominio.urlFoto?.toString()?.startsWith("http") == true) dominio.urlFoto.toString() else null

        return mapOf(
            "id" to dominio.id,
            "idPropietario" to dominio.idPropietario,
            "idPadre" to dominio.idEmpresaPadre,
            "tipoIdentidad" to "SUCURSAL",
            "nombreVisible" to dominio.nombreSucursal,
            "nombreEmpresa" to dominio.nombreEmpresa,
            "urlFoto" to fotoLimpia,
            "urlMiniatura" to miniLimpia,
            "miniaturaBase64" to miniLimpia,
            "reputacion" to dominio.reputacion,
            "estaEnLinea" to dominio.estaEnLinea,
            "brindaServicio" to dominio.brindaServicio,
            "brindaProducto" to dominio.brindaProducto,
            "atiende24Horas" to dominio.atiende24Horas,
            "visitaADomicilio" to dominio.visitaADomicilio,
            "realizaEnvios" to dominio.realizaEnvios,
            "calle" to dominio.calle,
            "numero" to dominio.numero,
            "codigoPostal" to dominio.codigoPostal,
            "latitud" to dominio.latitud,
            "longitud" to dominio.longitud,
            "idCategorias" to dominio.idCategorias,
            "filtrosBusqueda" to dominio.filtrosBusqueda
        )
    }
}



