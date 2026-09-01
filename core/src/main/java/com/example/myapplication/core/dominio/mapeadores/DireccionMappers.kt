package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import com.example.myapplication.core.dominio.modelos.DireccionDominio

/**
 * --- DIRECCIÓN MAPPER (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Puente único entre DireccionEntity (Datos) y DireccionDominio (Lógica).
 */
object DireccionMappers {

    fun deEntidadAModelo(d: DireccionEntity): DireccionDominio {
        return DireccionDominio(
            id = d.id,
            calle = d.calle,
            numero = d.numero,
            piso = d.piso,
            departamento = d.departamento,
            localidad = d.localidad,
            provincia = d.provincia,
            pais = d.pais,
            codigoPostal = d.codigoPostal,
            latitud = d.latitud,
            longitud = d.longitud,
            geohash = d.geohash,
            etiqueta = d.etiqueta,
            estaVerificadaGps = d.estaVerificadaGps,
            precisionGps = d.precisionGps,
            tieneLocalFisico = d.tieneLocalFisico,
            tipo = d.tipo,
            idPropietario = d.idPropietario,
            idSucursal = d.idSucursal,
            idReferencia = d.idReferencia
        )
    }

    fun deDominioAEntidad(d: DireccionDominio): DireccionEntity {
        return DireccionEntity(
            id = d.id,
            calle = d.calle,
            numero = d.numero,
            piso = d.piso,
            departamento = d.departamento,
            localidad = d.localidad,
            provincia = d.provincia,
            pais = d.pais,
            codigoPostal = d.codigoPostal,
            latitud = d.latitud,
            longitud = d.longitud,
            geohash = d.geohash,
            etiqueta = d.etiqueta,
            estaVerificadaGps = d.estaVerificadaGps,
            precisionGps = d.precisionGps,
            tieneLocalFisico = d.tieneLocalFisico,
            tipo = d.tipo,
            idPropietario = d.idPropietario ?: "",
            idSucursal = d.idSucursal,
            idReferencia = d.idReferencia
        )
    }
}



