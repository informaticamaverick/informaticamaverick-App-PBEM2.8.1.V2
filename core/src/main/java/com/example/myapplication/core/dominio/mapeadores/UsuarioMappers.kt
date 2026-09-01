package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.IdentidadUsuarioEntity
import com.example.myapplication.core.datos.local.relaciones.UsuarioConDireccionesRelacionesBD
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.utilidades.ImageUtils

/**
 * --- USUARIO MAPPER (v2026.FINAL) ---
 * [PROPÓSITO]: Transformar identidades de clientes en modelos de dominio.
 */
object UsuarioMappers {

    /**
     * Mapeo desde Relación de Room (Usuario + Direcciones).
     */
    fun deRelacionADominio(
        relacion: UsuarioConDireccionesRelacionesBD,
        cuenta: com.example.myapplication.core.datos.local.entidades.CuentaEntity? = null
    ): UsuarioDominio {
        val u = relacion.usuario
        return UsuarioDominio(
            id = u.id,
            nombre = u.nombre,
            apellido = u.apellido,
            nombreVisible = u.nombreVisible,
            urlFoto = u.urlFotoPerfil,
            urlMiniatura = u.miniaturaBase64,
            estaOnline = u.estaEnLinea,
            correo = cuenta?.correoGoogle ?: u.correoElectronico,
            telefono = u.numeroTelefono,
            cuitCuil = u.cuitCuil,
            biografia = u.biografia,
            reputacion = u.reputacion,
            totalReseñas = u.totalReseñas,
            trabajosContratados = u.trabajosContratados,
            esCargaCompleta = true
        )
    }

    /**
     * Mapeo desde Identidad Deep (Room).
     */
    fun deEntidadAModeloUi(
        u: IdentidadUsuarioEntity,
        cuenta: com.example.myapplication.core.datos.local.entidades.CuentaEntity? = null
    ): UsuarioDominio {
        return UsuarioDominio(
            id = u.id,
            nombre = u.nombre,
            apellido = u.apellido,
            nombreVisible = u.nombreVisible,
            urlFoto = u.urlFotoPerfil,
            urlMiniatura = u.miniaturaBase64,
            estaOnline = u.estaEnLinea,
            correo = cuenta?.correoGoogle ?: u.correoElectronico,
            telefono = u.numeroTelefono,
            cuitCuil = u.cuitCuil,
            biografia = u.biografia,
            reputacion = u.reputacion,
            totalReseñas = u.totalReseñas,
            trabajosContratados = u.trabajosContratados,
            esCargaCompleta = true
        )
    }

    fun deDominioAEntidad(u: UsuarioDominio): IdentidadUsuarioEntity {
        return IdentidadUsuarioEntity(
            id = u.id,
            nombre = u.nombre,
            apellido = u.apellido,
            nombreVisible = u.nombreVisible,
            urlFotoPerfil = ImageUtils.prepareForStorage(u.urlFoto),
            miniaturaBase64 = ImageUtils.prepareForStorage(u.urlMiniatura),
            correoElectronico = u.correo,
            numeroTelefono = u.telefono,
            cuitCuil = u.cuitCuil,
            biografia = u.biografia,
            reputacion = u.reputacion,
            totalReseñas = u.totalReseñas,
            trabajosContratados = u.trabajosContratados,
            estaEnLinea = u.estaOnline,
            ultimaSincronizacion = System.currentTimeMillis()
        )
    }

    /**
     * [ELITE]: Convierte un Usuario en un Modelo de Prestador táctico para el carrusel.
     * [LEY #10]: Reutilización de componentes jerárquicos.
     */
    fun deEntidadAPrestadorUi(
        u: IdentidadUsuarioEntity,
        cuenta: com.example.myapplication.core.datos.local.entidades.CuentaEntity? = null
    ): PrestadorDominio {
        return PrestadorDominio(
            id = u.id,
            idPropietario = u.id,
            nombre = u.nombre,
            apellido = u.apellido,
            titulo = u.nombreVisible.ifBlank { u.nombre },
            subtitulo = "Mi Perfil Personal",
            biografia = u.biografia,
            cuitCuil = u.cuitCuil,
            correo = cuenta?.correoGoogle ?: u.correoElectronico,
            esGoogle = cuenta?.correoGoogle?.isNotBlank() == true,
            numeroTelefono = u.numeroTelefono,
            urlFoto = ImageUtils.processImageSource(u.urlFotoPerfil),
            urlMiniatura = ImageUtils.processImageSource(u.miniaturaBase64 ?: u.urlFotoPerfil),
            estaOnline = u.estaEnLinea,
            estaSuscrito = cuenta?.estaSuscrito ?: false,
            reputacion = u.reputacion,
            totalReseñas = u.totalReseñas,
            tipo = TipoPrestador.INDIVIDUAL,
            esPerfilComercial = false // [BLOQUEO COMERCIAL]
        )
    }

    /**
     * [ELITE]: Convierte un Usuario Dominio (limpio) en un Modelo de Prestador.
     */
    fun deDominioAPrestadorUi(
        u: UsuarioDominio,
        estaSuscrito: Boolean = false
    ): PrestadorDominio {
        return PrestadorDominio(
            id = u.id,
            idPropietario = u.id,
            nombre = u.nombre,
            apellido = u.apellido,
            titulo = u.nombreVisible,
            subtitulo = "Mi Perfil Personal",
            biografia = u.biografia,
            cuitCuil = u.cuitCuil,
            correo = u.correo,
            numeroTelefono = u.telefono,
            urlFoto = u.urlFoto,
            urlMiniatura = u.urlMiniatura,
            estaOnline = u.estaOnline,
            estaSuscrito = estaSuscrito,
            reputacion = u.reputacion,
            totalReseñas = u.totalReseñas,
            tipo = TipoPrestador.INDIVIDUAL,
            esPerfilComercial = false
        )
    }

    /**
     * Mapeo desde Identidad Shallow (Documento ligero de Firestore).
     */

}




