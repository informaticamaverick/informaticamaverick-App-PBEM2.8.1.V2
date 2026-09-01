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

    @Query("SELECT * FROM concursos_publicos ORDER BY marcaTiempo DESC")
    fun obtenerMercadoPaginado(): PagingSource<Int, ConcursoPublicoEntity>

    @Query("""
        SELECT cp.* FROM concursos_publicos cp
        WHERE cp.idCliente = :idCliente
        AND (:consulta = '' OR cp.rowid IN (SELECT rowid FROM concursos_publicos_fts WHERE concursos_publicos_fts MATCH :consulta))
        AND (:soloActivos = 0 OR cp.estado = 'ABIERTA')
        AND (:soloCerrados = 0 OR cp.estado = 'CERRADA')
        AND (:soloAdjudicados = 0 OR cp.idPrestadorAdjudicado IS NOT NULL)
        AND (:soloNoLeidos = 0 OR (SELECT COUNT(*) FROM presupuestos_finales p WHERE p.idConcurso = cp.idConcurso AND p.leido = 0) > 0)
        AND (:idCategoria IS NULL OR cp.idCategoria = :idCategoria)
        ORDER BY 
            CASE WHEN :orden = 'sort_date' THEN cp.marcaTiempo END DESC,
            CASE WHEN :orden = 'sort_alpha' THEN cp.titulo END ASC,
            CASE WHEN :orden = 'sort_concursos_conteo' THEN cp.conteoPresupuestos END DESC,
            CASE WHEN :orden = 'reciente' THEN cp.marcaTiempo END DESC
    """)
    fun buscarPropiosFts(
        idCliente: String,
        consulta: String,
        soloActivos: Boolean,
        soloCerrados: Boolean,
        soloAdjudicados: Boolean,
        soloNoLeidos: Boolean,
        idCategoria: String?,
        orden: String
    ): Flow<List<ConcursoPublicoEntity>>

    @Query("""
        SELECT v.* FROM ConcursoPublicoResumenSQLView v
        JOIN concursos_publicos cp ON v.idConcurso = cp.idConcurso
        WHERE cp.idCliente = :idCliente
        AND (:consulta = '' OR cp.rowid IN (SELECT rowid FROM concursos_publicos_fts WHERE concursos_publicos_fts MATCH :consulta))
        AND (:soloActivos = 0 OR cp.estado = 'ABIERTA')
        AND (:soloCerrados = 0 OR cp.estado = 'CERRADA')
        AND (:soloAdjudicados = 0 OR cp.idPrestadorAdjudicado IS NOT NULL)
        AND (:soloNoLeidos = 0 OR v.ofertasNuevas > 0)
        AND (:idCategoria IS NULL OR cp.idCategoria = :idCategoria)
        ORDER BY 
            CASE WHEN :orden = 'sort_date' THEN cp.marcaTiempo END DESC,
            CASE WHEN :orden = 'sort_alpha' THEN cp.titulo END ASC,
            CASE WHEN :orden = 'sort_concursos_conteo' THEN v.totalOfertas END DESC,
            CASE WHEN :orden = 'reciente' THEN cp.marcaTiempo END DESC
    """)
    fun buscarPropiosResumenFts(
        idCliente: String,
        consulta: String,
        soloActivos: Boolean,
        soloCerrados: Boolean,
        soloAdjudicados: Boolean,
        soloNoLeidos: Boolean,
        idCategoria: String?,
        orden: String
    ): Flow<List<ConcursoPublicoResumenSQLView>>
}
