package com.example.myapplication.core.dominio.mapeadores.shallow

import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import com.example.myapplication.core.dominio.modelos.shallow.PrestadorShallowDominio
import com.example.myapplication.core.utilidades.ImageUtils

/**
 * --- PRESTADOR SHALLOW MAPPER (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Transformar entre Entidad de Prestador y su versión ligera para búsqueda.
 * [LEY #17]: Protocolo de Bautizo.
 */
object PrestadorShallowMappers {

    fun deEntidadADominio(
        entidad: IdentidadPrestadorEntity, 
        direccion: DireccionEntity? = null,
        estaSuscrito: Boolean = false,
        tags: List<String> = emptyList()
    ): PrestadorShallowDominio {
        return PrestadorShallowDominio(
            id = entidad.id,
            idPropietario = entidad.id,
            nombreVisible = entidad.nombreVisible,
            urlFoto = entidad.urlFotoPerfil,
            miniaturaBase64 = entidad.miniaturaBase64,
            reputacion = entidad.reputacion,
            trabajosRealizados = entidad.trabajosRealizados,
            estaVerificado = entidad.estaVerificado,
            estaEnLinea = entidad.estaEnLinea,
            estaSuscrito = estaSuscrito,
            brindaServicio = entidad.brindaServicio,
            brindaProducto = entidad.brindaProducto,
            brindaTurnos = entidad.brindaTurnos,
            atiende24Horas = entidad.atiende24Horas,
            visitaADomicilio = entidad.visitaADomicilio,
            realizaEnvios = entidad.realizaEnvios,
            tieneLocalFisico = entidad.tieneLocalFisico,
            calle = direccion?.calle ?: "",
            numero = direccion?.numero ?: "",
            codigoPostal = direccion?.codigoPostal ?: "",
            latitud = direccion?.latitud ?: 0.0,
            longitud = direccion?.longitud ?: 0.0,
            idCategorias = entidad.idCategorias,
            filtrosBusqueda = tags
        )
    }
    
    fun deDominioAMapa(dominio: PrestadorShallowDominio): Map<String, Any?> {
        // [SUPREME.FIX]: Limpieza de punteros de memoria [B@... y conversión a Base64 real
        fun procesarCampoImagen(campo: Any?): String? {
            return when (campo) {
                is ByteArray -> ImageUtils.bytesToBase64(campo)
                else -> {
                    val s = campo?.toString()
                    // Si el string es un puntero corrupto, lo anulamos para que la app no intente cargarlo
                    if (s?.startsWith("[B@") == true) null else s
                }
            }
        }

        val miniLimpia = procesarCampoImagen(dominio.miniaturaBase64)
        val fotoLimpia = if (dominio.urlFoto?.toString()?.startsWith("http") == true) dominio.urlFoto.toString() else null

        return mapOf(
            "id" to dominio.id,
            "idPropietario" to dominio.idPropietario,
            "tipoIdentidad" to "PRESTADOR",
            "nombreVisible" to dominio.nombreVisible,
            "urlFoto" to fotoLimpia,
            "urlMiniatura" to miniLimpia,
            "miniaturaBase64" to miniLimpia,
            "reputacion" to dominio.reputacion,
            "trabajosRealizados" to dominio.trabajosRealizados,
            "estaVerificado" to dominio.estaVerificado,
            "estaEnLinea" to dominio.estaEnLinea,
            "brindaServicio" to dominio.brindaServicio,
            "brindaProducto" to dominio.brindaProducto,
            "brindaTurnos" to dominio.brindaTurnos,
            // [FIX]: la clave era "atiende24Horas" pero el lector (ResultadoIndiceBusquedaMappers)
            // espera "atiende24h" — nunca coincidían, el filtro de 24hs en el buscador del
            // cliente siempre leía false sin importar el valor real.
            "atiende24h" to dominio.atiende24Horas,
            "visitaADomicilio" to dominio.visitaADomicilio,
            "realizaEnvios" to dominio.realizaEnvios,
            "tieneLocalFisico" to dominio.tieneLocalFisico,
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



