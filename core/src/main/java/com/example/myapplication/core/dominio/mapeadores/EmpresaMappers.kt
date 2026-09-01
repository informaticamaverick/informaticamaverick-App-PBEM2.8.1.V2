package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.EmpresaEntity
import com.example.myapplication.core.datos.local.relaciones.EmpresaCompletaRelacionesBD
import com.example.myapplication.core.dominio.modelos.EmpresaDominio
import com.example.myapplication.core.dominio.modelos.EmpresaDominioCompleto

object EmpresaMappers {
    fun deEntidadADominio(e: EmpresaEntity): EmpresaDominio {
        return EmpresaDominio(
            id = e.id,
            idPropietario = e.idPropietario,
            nombre = e.nombre,
            razonSocial = e.razonSocial,
            descripcion = e.descripcion,
            cuit = e.cuit,
            correoContacto = e.correoContacto,
            urlFoto = e.urlFoto,
            urlMiniatura = e.miniaturaBase64,
            idCategorias = e.idCategorias,
            reputacion = e.reputacion,
            totalReseñas = e.totalReseñas,
            trabajosRealizados = e.trabajosRealizados,
            nivelElite = e.nivelElite,
            estaVerificada = e.estaVerificada
        )
    }

    fun deRelacionADominioCompleto(relacion: EmpresaCompletaRelacionesBD): EmpresaDominioCompleto {
        return EmpresaDominioCompleto(
            empresa = deEntidadADominio(relacion.empresa),
            sucursales = relacion.sucursales.map { SucursalMappers.deRelacionADominioCompleto(it) }
        )
    }

    fun deDominioAEntidad(e: EmpresaDominio): EmpresaEntity {
        return EmpresaEntity(
            id = e.id,
            idPropietario = e.idPropietario,
            nombre = e.nombre,
            razonSocial = e.razonSocial,
            descripcion = e.descripcion,
            cuit = e.cuit,
            correoContacto = e.correoContacto,
            urlFoto = com.example.myapplication.core.utilidades.ImageUtils.prepareForStorage(e.urlFoto),
            miniaturaBase64 = com.example.myapplication.core.utilidades.ImageUtils.prepareForStorage(e.urlMiniatura),
            idCategorias = e.idCategorias,
            reputacion = e.reputacion,
            totalReseñas = e.totalReseñas,
            trabajosRealizados = e.trabajosRealizados,
            nivelElite = e.nivelElite,
            estaVerificada = e.estaVerificada,
            ultimaSincronizacion = System.currentTimeMillis()
        )
    }
}



