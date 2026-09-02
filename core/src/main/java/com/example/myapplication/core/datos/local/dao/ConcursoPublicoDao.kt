package com.example.myapplication.core.datos.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.ConcursoPublicoConPresupuestos
import com.example.myapplication.core.datos.local.entidades.vistas.ConcursoPublicoResumenSQLView
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE LICITACIONES / CONCURSOS PÚBLICOS (ELITE v2026) ---
 */
@Dao
interface ConcursoPublicoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(concurso: ConcursoPublicoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(concursos: List<ConcursoPublicoEntity>)

    @Query("SELECT * FROM concursos_publicos WHERE idConcurso = :id")
    fun obtenerPorId(id: String): Flow<ConcursoPublicoEntity?>

    @Query("SELECT * FROM concursos_publicos ORDER BY marcaTiempo DESC")
    fun obtenerTodos(): Flow<List<ConcursoPublicoEntity>>

    @Query("""
        SELECT cp.* FROM concursos_publicos cp
        JOIN concursos_publicos_fts fts ON cp.rowid = fts.rowid
        WHERE concursos_publicos_fts MATCH :consulta
        ORDER BY cp.marcaTiempo DESC
    """)
    fun buscarMatch(consulta: String): Flow<List<ConcursoPublicoEntity>>

    @Query("SELECT * FROM ConcursoPublicoResumenSQLView WHERE idCliente = :idCliente ORDER BY marcaTiempo DESC")
    fun obtenerResumenesPorCliente(idCliente: String): Flow<List<ConcursoPublicoResumenSQLView>>

    @Query("SELECT DISTINCT idCategoria FROM concursos_publicos WHERE idCliente = :idCliente")
    fun obtenerRubrosEnUso(idCliente: String): Flow<List<String>>

    @Query("SELECT * FROM concursos_publicos WHERE idConcurso = :id")
    suspend fun obtenerPorIdSync(id: String): ConcursoPublicoEntity?

    @Query("SELECT * FROM concursos_publicos")
    suspend fun obtenerTodosSync(): List<ConcursoPublicoEntity>

    @Query("SELECT * FROM concursos_publicos WHERE idCliente = :idCliente ORDER BY marcaTiempo DESC")
    suspend fun obtenerPorClienteSync(idCliente: String): List<ConcursoPublicoEntity>

    @Query("DELETE FROM concursos_publicos WHERE idConcurso = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM concursos_publicos")
    suspend fun eliminarTodo()

    @Transaction
    @Query("SELECT * FROM concursos_publicos WHERE idConcurso = :id")
    fun obtenerConcursoConPresupuestos(id: String): Flow<ConcursoPublicoConPresupuestos?>

    // 🐛 FIX (01/09): esta consulta no filtraba nada — devolvía TODOS los concursos
    // guardados localmente sin importar zona ni categoría del prestador. Aparte de que
    // la query remota (ver ConcursoRemoteMediator) ahora sí filtra al traer de Firestore,
    // esta capa local también tiene que filtrar: la tabla puede tener filas viejas
    // cacheadas de antes del fix (o de otra sesión) que la sincronización remota no borra.
    @Query("SELECT * FROM concursos_publicos WHERE direccionCodigoPostal = :cp AND idCategoria IN (:categorias) ORDER BY marcaTiempo DESC")
    fun obtenerMercadoPaginado(cp: String, categorias: List<String>): PagingSource<Int, ConcursoPublicoEntity>

    @Query("""
        SELECT cp.* FROM concursos_publicos cp
        LEFT JOIN concursos_publicos_fts fts ON cp.rowid = fts.rowid
        WHERE cp.idCliente = :idCliente
        AND (:consulta = '' OR concursos_publicos_fts MATCH :consulta)
        AND (:soloActivos = 0 OR cp.estaActivo = 1)
        AND (:soloAdjudicados = 0 OR cp.idPrestadorAdjudicado IS NOT NULL)
        AND (:idCategoria IS NULL OR cp.idCategoria = :idCategoria)
        ORDER BY 
            CASE WHEN :orden = 'reciente' THEN cp.marcaTiempo END DESC,
            CASE WHEN :orden = 'antiguo' THEN cp.marcaTiempo END ASC
    """)
    fun buscarPropiosFts(
        idCliente: String,
        consulta: String,
        soloActivos: Boolean,
        soloAdjudicados: Boolean,
        idCategoria: String?,
        orden: String
    ): Flow<List<ConcursoPublicoEntity>>

    @Query("""
        SELECT v.* FROM ConcursoPublicoResumenSQLView v
        JOIN concursos_publicos cp ON v.idConcurso = cp.idConcurso
        LEFT JOIN concursos_publicos_fts fts ON cp.rowid = fts.rowid
        WHERE cp.idCliente = :idCliente
        AND (:consulta = '' OR concursos_publicos_fts MATCH :consulta)
        AND (:soloActivos = 0 OR cp.estaActivo = 1)
        AND (:soloAdjudicados = 0 OR cp.idPrestadorAdjudicado IS NOT NULL)
        AND (:idCategoria IS NULL OR cp.idCategoria = :idCategoria)
        ORDER BY 
            CASE WHEN :orden = 'reciente' THEN cp.marcaTiempo END DESC,
            CASE WHEN :orden = 'antiguo' THEN cp.marcaTiempo END ASC
    """)
    fun buscarPropiosResumenFts(
        idCliente: String,
        consulta: String,
        soloActivos: Boolean,
        soloAdjudicados: Boolean,
        idCategoria: String?,
        orden: String
    ): Flow<List<ConcursoPublicoResumenSQLView>>
}
