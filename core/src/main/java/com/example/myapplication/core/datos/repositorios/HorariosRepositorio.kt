package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.HorarioDao
import com.example.myapplication.core.datos.local.entidades.HorarioEntity
import com.example.myapplication.core.dominio.mapeadores.HorarioMappers
import com.example.myapplication.core.dominio.modelos.HorarioDominio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Repositorio de Horarios
 * [PROPÓSITO]: Centralizar la gestión de tiempos de atención y disponibilidad.
 * [FUNCIONAMIENTO INTERNO]: Mapeo entre 'HorarioEntity' y el modelo de dominio 'Horario'.
 * [RELACIÓN]: Sincroniza datos locales con la lógica de negocio temporal.
 */
@Singleton
class HorariosRepositorio @Inject constructor(
    private val horarioDao: HorarioDao,
    private val firestore: FirebaseFirestore
) {
    fun obtenerPorReferencia(idReferencia: String): Flow<HorarioDominio?> =
        horarioDao.obtenerPorReferencia(idReferencia).map { entidad ->
            entidad?.let { HorarioMappers.deEntidadAModelo(it) }
        }

    suspend fun insertar(horario: HorarioDominio, idReferencia: String, idReferenciaPadre: String? = null) {
        horarioDao.insertar(HorarioMappers.deModeloAEntidad(horario, idReferencia, idReferenciaPadre))
    }
}



































