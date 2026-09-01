package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.SucursalEntity
import com.example.myapplication.core.datos.local.relaciones.SucursalCompletaRelacionesBD
import com.example.myapplication.core.dominio.modelos.SucursalDominio
import com.example.myapplication.core.dominio.modelos.SucursalDominioCompleto

object SucursalMappers {
    fun deEntidadADominio(s: SucursalEntity): SucursalDominio {
        return SucursalDominio(
            id = s.id,
            idEmpresaPadre = s.idEmpresaPadre,
            idPropietario = s.idPropietario,
            nombre = s.nombre,
            descripcion = s.descripcion,
            numeroTelefono = s.numeroTelefono,
            reputacion = s.reputacion,
            totalReseñas = s.totalReseñas,
            trabajosRealizados = s.trabajosRealizados,
            likes = s.likes,
            dislikes = s.dislikes,
            estaEnLinea = s.estaEnLinea,
            brindaServicio = s.brindaServicio,
            brindaProducto = s.brindaProducto,
            atiende24Horas = s.atiende24Horas,
            visitaADomicilio = s.visitaADomicilio,
            realizaEnvios = s.realizaEnvios,
            brindaTurnos = s.brindaTurnos,
            usaAgendaRecursos = s.usaAgendaRecursos,
            capacidadSimultanea = s.capacidadSimultanea
        )
    }

    fun deRelacionADominioCompleto(relacion: SucursalCompletaRelacionesBD): SucursalDominioCompleto {
        return SucursalDominioCompleto(
            sucursal = deEntidadADominio(relacion.sucursal),
            direccion = relacion.direccion?.let { DireccionMappers.deEntidadAModelo(it) },
            horario = relacion.horario?.let { HorarioMappers.deEntidadAModelo(it) },
            equipoTrabajo = relacion.equipoTrabajo.map { EquipoTrabajoMappers.deEntidadAModelo(it) },
            recursos = relacion.recursos.map { RecursoMappers.deEntidadAModelo(it) },
            reseñas = relacion.reseñas.map { ReviewMappers.deEntidadADominio(it) }
        )
    }

    fun deDominioAEntidad(s: SucursalDominio): SucursalEntity {
        return SucursalEntity(
            id = s.id,
            idEmpresaPadre = s.idEmpresaPadre,
            idPropietario = s.idPropietario,
            nombre = s.nombre,
            descripcion = s.descripcion,
            numeroTelefono = s.numeroTelefono,
            reputacion = s.reputacion,
            totalReseñas = s.totalReseñas,
            trabajosRealizados = s.trabajosRealizados,
            likes = s.likes,
            dislikes = s.dislikes,
            estaEnLinea = s.estaEnLinea,
            brindaServicio = s.brindaServicio,
            brindaProducto = s.brindaProducto,
            atiende24Horas = s.atiende24Horas,
            visitaADomicilio = s.visitaADomicilio,
            realizaEnvios = s.realizaEnvios,
            brindaTurnos = s.brindaTurnos,
            usaAgendaRecursos = s.usaAgendaRecursos,
            capacidadSimultanea = s.capacidadSimultanea,
            ultimaSincronizacion = System.currentTimeMillis()
        )
    }
}



