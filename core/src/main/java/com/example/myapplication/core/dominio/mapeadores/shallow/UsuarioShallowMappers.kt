package com.example.myapplication.core.dominio.mapeadores.shallow

import com.example.myapplication.core.datos.local.entidades.IdentidadUsuarioEntity
import com.example.myapplication.core.dominio.modelos.shallow.UsuarioShallowDominio
import com.example.myapplication.core.utilidades.ImageUtils

/**
 * --- USUARIO SHALLOW MAPPER (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Transformar entre Entidad de Usuario y su versión ligera.
 * [LEY #17]: Protocolo de Bautizo.
 */
object UsuarioShallowMappers {

    fun deEntidadADominio(
        entidad: IdentidadUsuarioEntity,
        estaSuscrito: Boolean = false
    ): UsuarioShallowDominio {
        // 🔥 [ELITE]: Priorizamos la miniatura local de Room sobre la URL de red
        val miniaturaFinal = entidad.miniaturaBase64 ?: entidad.urlFotoPerfil

        return UsuarioShallowDominio(
            id = entidad.id,
            nombreVisible = entidad.nombreVisible,
            urlMiniatura = miniaturaFinal,
            reputacion = entidad.reputacion,
            estaEnLinea = entidad.estaEnLinea,
            estaSuscrito = estaSuscrito
        )
    }
    
    fun deDominioAMapa(dominio: UsuarioShallowDominio): Map<String, Any?> {
        // [SUPREME.FIX]: Limpieza de punteros de memoria [B@... y conversión a Base64 real (App Azul)
        fun procesarCampoImagen(campo: Any?): String? {
            return when (campo) {
                is ByteArray -> ImageUtils.bytesToBase64(campo)
                else -> {
                    val s = campo?.toString()
                    if (s?.startsWith("[B@") == true) null else s
                }
            }
        }

        val miniLimpia = procesarCampoImagen(dominio.urlMiniatura)

        return mapOf(
            "id" to dominio.id,
            "nombreVisible" to dominio.nombreVisible,
            "urlMiniatura" to miniLimpia,
            "miniaturaBase64" to miniLimpia, // Estandarización Maverick
            "reputacion" to dominio.reputacion,
            "estaEnLinea" to dominio.estaEnLinea
            // [SUPREME]: Se elimina 'estaSuscrito' por ser un flag de control local del prestador
        )
    }
}




