package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.CategoriaDao
import com.example.myapplication.core.datos.local.dao.SuperCategoriaShallow
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.vistas.CategoriaResumenSQLView
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CATEGORÍAS (Atómico - v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Singleton
class CategoriaRepositorio @Inject constructor(
    private val categoriaDao: CategoriaDao
) {
    val todasLasCategorias: Flow<List<CategoriaEntity>> = categoriaDao.obtenerTodas()

    fun obtenerMetadatosSuperCategorias(): Flow<List<SuperCategoriaShallow>> = 
        categoriaDao.obtenerMetadatosSuperCategorias()

    fun obtenerPorSuperCategoria(idSuperCategoria: String): Flow<List<CategoriaEntity>> = 
        categoriaDao.obtenerPorSuperCategoria(idSuperCategoria)

    fun obtenerPorNombre(nombre: String): Flow<CategoriaEntity?> =
        categoriaDao.obtenerPorNombreFlow(nombre)

    fun obtenerPorId(id: String): Flow<CategoriaEntity?> =
        categoriaDao.obtenerPorIdFlow(id)

    suspend fun obtenerPorIdSync(id: String): CategoriaEntity? =
        categoriaDao.obtenerPorId(id)

    fun buscarCategorias(consulta: String): Flow<List<CategoriaEntity>> {
        val consultaFts = if (consulta.isNotBlank()) "$consulta*" else ""
        return categoriaDao.buscarMatch(consultaFts)
    }

    // --- VISTAS RESUMEN (ELITE v2026) ---

    fun obtenerResumenTodas(): Flow<List<CategoriaResumenSQLView>> = 
        categoriaDao.obtenerResumenTodas()

    fun obtenerResumenPorSuperCategoria(id: String): Flow<List<CategoriaResumenSQLView>> = 
        categoriaDao.obtenerResumenPorSuperCategoria(id)

    fun buscarResumenCategorias(consulta: String): Flow<List<CategoriaResumenSQLView>> {
        val consultaFts = if (consulta.isNotBlank()) "$consulta*" else ""
        return categoriaDao.buscarResumenMatch(consultaFts)
    }

    suspend fun insertarOActualizar(categoria: CategoriaEntity) {
        categoriaDao.insertarOActualizar(categoria)
    }

    /**
     * Sincronización del catálogo (Seed).
     */
    suspend fun sincronizarCatalogoSiEsNecesario(versionActual: Int) {
        android.util.Log.d("CategoriaRepo", "🛡️ [SYNC_CATALOG] Usando Seed Local v$versionActual.")
    }
}


































