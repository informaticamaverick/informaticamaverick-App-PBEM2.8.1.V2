package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.EquipoTrabajoDao
import com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity
import com.example.myapplication.core.dominio.mapeadores.EquipoTrabajoMappers
import com.example.myapplication.core.dominio.modelos.EquipoTrabajoDominio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Repositorio de Equipo de Trabajo
 * [PROPÓSITO]: Centralizar las operaciones de staff para todas las aplicaciones del ecosistema.
 * [FUNCIONAMIENTO INTERNO]: Actúa como puente entre el DAO y los modelos de dominio usando mappers externos.
 * [RELACIÓN]: Implementa la Ley #9 (Estándar Maverick) y sustituye al antiguo 'EquipoRepositorio'.
 */
@Singleton
class EquipoTrabajoRepositorio @Inject constructor(
    private val equipoTrabajoDao: EquipoTrabajoDao
) {
    fun obtenerPorSucursal(idSucursal: String): Flow<List<EquipoTrabajoDominio>> =
        equipoTrabajoDao.obtenerPorSucursal(idSucursal).map { lista ->
            lista.map { EquipoTrabajoMappers.deEntidadAModelo(it) }
        }

    fun obtenerPorPropietario(idPropietario: String): Flow<List<EquipoTrabajoDominio>> =
        equipoTrabajoDao.obtenerPorPropietario(idPropietario).map { lista ->
            lista.map { EquipoTrabajoMappers.deEntidadAModelo(it) }
        }

    suspend fun insertar(equipoTrabajo: EquipoTrabajoDominio, idPropietario: String) {
        equipoTrabajoDao.insertar(EquipoTrabajoMappers.deModeloAEntidad(equipoTrabajo, idPropietario))
    }

    suspend fun eliminarPorId(idEmpleado: String) {
        equipoTrabajoDao.eliminarPorId(idEmpleado)
    }
}


