package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.EventoDao
import com.example.myapplication.core.datos.local.dao.RecursoDao
import com.example.myapplication.core.datos.local.entidades.RecursoEntity
import com.example.myapplication.core.dominio.mapeadores.RecursoMappers
import com.example.myapplication.core.dominio.modelos.RecursoDominio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Repositorio de Recursos
 * [PROPÓSITO]: Gestionar activos y su disponibilidad real para el ecosistema Maverick.
 * [FUNCIONAMIENTO INTERNO]: Utiliza 'RecursoMappers' para desacoplar Room del dominio.
 * [RELACIÓN]: Cruza datos con 'EventoDao' para calcular disponibilidad táctica.
 */
@Singleton
class RecursosRepositorio @Inject constructor(
    private val recursoDao: RecursoDao,
    private val eventoDao: EventoDao
) {
    fun obtenerPorSucursal(idSucursal: String): Flow<List<RecursoDominio>> =
        recursoDao.obtenerPorSucursal(idSucursal).map { lista ->
            lista.map { RecursoMappers.deEntidadAModelo(it) }
        }

    fun obtenerPorPropietario(idPropietario: String): Flow<List<RecursoDominio>> =
        recursoDao.obtenerPorPropietario(idPropietario).map { lista ->
            lista.map { RecursoMappers.deEntidadAModelo(it) }
        }

    suspend fun insertar(recurso: RecursoDominio, idPropietario: String) {
        recursoDao.insertar(RecursoMappers.deModeloAEntidad(recurso, idPropietario))
    }

    suspend fun eliminarPorId(idRecurso: String) {
        recursoDao.eliminarPorId(idRecurso)
    }

    /**
     * 🔥 [ELITE]: Consulta la disponibilidad real de un recurso.
     * Cruza la capacidad máxima con los eventos (citas) confirmados en un rango.
     */
    suspend fun consultarOcupacionReal(idRecurso: String, inicioUtc: Long, finUtc: Long): Int {
        val ocupados = eventoDao.verificarOcupacionRecurso(idRecurso, inicioUtc, finUtc)
        val recurso = recursoDao.obtenerPorIdSync(idRecurso)
        val capacidadMaxima = recurso?.capacidadMaxima ?: 1
        
        return (capacidadMaxima - ocupados).coerceAtLeast(0)
    }
}


