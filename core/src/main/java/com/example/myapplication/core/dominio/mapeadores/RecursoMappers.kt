package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.RecursoEntity
import com.example.myapplication.core.dominio.modelos.RecursoDominio

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Mapeador de Recurso (Unificado)
 * [PROPÓSITO]: Transformación entre 'RecursoEntity' (Room) y 'RecursoDominio'.
 * [FUNCIONAMIENTO INTERNO]: Mapea campos uno a uno y genera datos masticados (precios, capacidad).
 * [RELACIÓN]: Desacopla la base de datos de los casos de uso y la UI.
 */
object RecursoMappers {

    fun deEntidadAModelo(entidad: RecursoEntity): RecursoDominio {
        return RecursoDominio(
            id = entidad.id,
            nombre = entidad.nombre,
            descripcion = entidad.descripcion,
            precioBase = entidad.precioBase,
            precioFormateado = "$${"%.2f".format(entidad.precioBase)}",
            tipoRecurso = entidad.tipoRecurso,
            estaActivo = entidad.estaHabilitado,
            capacidadMaxima = entidad.capacidadMaxima,
            capacidadFormateada = "${entidad.capacidadMaxima} pers.",
            idSucursal = entidad.idSucursal
        )
    }

    fun deModeloAEntidad(modelo: RecursoDominio, idPropietario: String): RecursoEntity {
        return RecursoEntity(
            id = modelo.id,
            idPropietario = idPropietario,
            idSucursal = modelo.idSucursal ?: "",
            nombre = modelo.nombre,
            descripcion = modelo.descripcion,
            precioBase = modelo.precioBase,
            tipoRecurso = modelo.tipoRecurso,
            estaHabilitado = modelo.estaActivo,
            capacidadMaxima = modelo.capacidadMaxima
        )
    }
    
    // Mapeo para Firestore (DTO limpio)
    fun aMapaFirestore(entidad: RecursoEntity): Map<String, Any?> {
        return mapOf(
            "id" to entidad.id,
            "nombre" to entidad.nombre,
            "descripcion" to entidad.descripcion,
            "precioBase" to entidad.precioBase,
            "tipoRecurso" to entidad.tipoRecurso,
            "estaHabilitado" to entidad.estaHabilitado,
            "capacidadMaxima" to entidad.capacidadMaxima,
            "idSucursal" to entidad.idSucursal
        )
    }
}



