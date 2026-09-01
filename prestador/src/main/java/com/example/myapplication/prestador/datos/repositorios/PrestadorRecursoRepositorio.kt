package com.example.myapplication.prestador.datos.repositorios

import com.example.myapplication.core.datos.local.dao.RecursoDao
import com.example.myapplication.core.datos.local.entidades.RecursoEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE RECURSOS PRESTADOR (ELITE v2026.FINAL) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Singleton
class PrestadorRecursoRepositorio @Inject constructor(
    private val recursoDao: RecursoDao,
    private val firestore: FirebaseFirestore
) {
    fun obtenerPorSucursal(idSucursal: String): Flow<List<RecursoEntity>> =
        recursoDao.obtenerPorSucursal(idSucursal)

    fun obtenerPorPropietario(idPropietario: String): Flow<List<RecursoEntity>> =
        recursoDao.obtenerPorPropietario(idPropietario)

    suspend fun guardarRecurso(recurso: RecursoEntity) {
        recursoDao.insertar(recurso)
        // [Fase 2]: Sync remota
    }

    suspend fun eliminarRecurso(recurso: RecursoEntity) {
        recursoDao.eliminarPorId(recurso.id)
        // [Fase 2]: Sync remota
    }
}















































