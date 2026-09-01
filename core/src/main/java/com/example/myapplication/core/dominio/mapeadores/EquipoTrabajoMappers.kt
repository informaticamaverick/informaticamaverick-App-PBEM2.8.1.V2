package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity
import com.example.myapplication.core.dominio.modelos.EquipoTrabajoDominio

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Mapeador de Equipo de Trabajo (Unificado)
 * [PROPÓSITO]: Conversión entre 'EquipoTrabajoEntity' and 'EquipoTrabajoDominio'.
 * [FUNCIONAMIENTO INTERNO]: Transformación de datos del staff con campos formateados.
 * [RELACIÓN]: Reemplaza la lógica de mapeo antigua de 'EquipoMapper'.
 */
object EquipoTrabajoMappers {

    fun deEntidadAModelo(entidad: EquipoTrabajoEntity): EquipoTrabajoDominio {
        return EquipoTrabajoDominio(
            id = entidad.id,
            nombre = entidad.nombre,
            apellido = entidad.apellido,
            nombreCompleto = "${entidad.nombre} ${entidad.apellido}".trim(),
            cargo = entidad.cargo,
            detalle = entidad.detalle,
            iniciales = (entidad.nombre.take(1) + entidad.apellido.take(1)).uppercase(),
            avatarEmoji = entidad.avatarEmoji,
            idSucursal = entidad.idSucursal,
            estaHabilitado = entidad.estaHabilitado,
            idRecursoVinculado = entidad.idRecursoVinculado
        )
    }

    fun deModeloAEntidad(modelo: EquipoTrabajoDominio, idPropietario: String): EquipoTrabajoEntity {
        return EquipoTrabajoEntity(
            id = modelo.id,
            idPropietario = idPropietario,
            idSucursal = modelo.idSucursal ?: "",
            nombre = modelo.nombre,
            apellido = modelo.apellido,
            cargo = modelo.cargo,
            detalle = modelo.detalle,
            avatarEmoji = modelo.avatarEmoji,
            estaHabilitado = modelo.estaHabilitado,
            idRecursoVinculado = modelo.idRecursoVinculado
        )
    }
    
    // Mapeo para Firestore (DTO limpio)
    fun aMapaFirestore(entidad: EquipoTrabajoEntity): Map<String, Any?> {
        return mapOf(
            "id" to entidad.id,
            "nombre" to entidad.nombre,
            "apellido" to entidad.apellido,
            "cargo" to entidad.cargo,
            "detalle" to entidad.detalle,
            "avatarEmoji" to entidad.avatarEmoji,
            "idSucursal" to entidad.idSucursal,
            "estaHabilitado" to entidad.estaHabilitado,
            "idRecursoVinculado" to entidad.idRecursoVinculado
        )
    }
}



