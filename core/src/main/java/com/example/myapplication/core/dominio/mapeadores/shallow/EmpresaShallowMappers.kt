package com.example.myapplication.core.dominio.mapeadores.shallow

import com.example.myapplication.core.datos.local.entidades.EmpresaEntity
import com.example.myapplication.core.dominio.modelos.shallow.EmpresaShallowDominio

/**
 * --- EMPRESA SHALLOW MAPPER (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Transformar entre Entidad de Empresa y su versión ligera.
 * [LEY #17]: Protocolo de Bautizo.
 */
object EmpresaShallowMappers {

    fun deEntidadADominio(
        entidad: EmpresaEntity,
        estaSuscrito: Boolean = false
    ): EmpresaShallowDominio {
        return EmpresaShallowDominio(
            id = entidad.id,
            idPropietario = entidad.idPropietario,
            nombre = entidad.nombre,
            urlMiniatura = entidad.miniaturaBase64 ?: entidad.urlFoto,
            reputacion = entidad.reputacion,
            estaVerificada = entidad.estaVerificada,
            estaSuscrito = estaSuscrito,
            idCategorias = entidad.idCategorias
        )
    }

    fun deDominioAMapa(dominio: EmpresaShallowDominio): Map<String, Any?> {
        return mapOf(
            "id" to dominio.id,
            "idPropietario" to dominio.idPropietario,
            "tipoIdentidad" to "EMPRESA",
            "nombreVisible" to dominio.nombre,
            "urlMiniatura" to dominio.urlMiniatura,
            "reputacion" to dominio.reputacion,
            "estaVerificada" to dominio.estaVerificada,
            "estaSuscrito" to dominio.estaSuscrito,
            "idCategorias" to dominio.idCategorias
        )
    }
}



