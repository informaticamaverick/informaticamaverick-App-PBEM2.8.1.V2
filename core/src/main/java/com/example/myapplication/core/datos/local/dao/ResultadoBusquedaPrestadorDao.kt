package com.example.myapplication.core.datos.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.myapplication.core.datos.local.entidades.RelacionBusquedaEntity
import com.example.myapplication.core.datos.local.entidades.vistas.ResultadoBusquedaPrestadorSQLView

/**
 * --- DAO: RESULTADO BÚSQUEDA PRESTADOR (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Gestionar la persistencia local de los resultados de búsqueda.
 * [LEY #6]: Soberanía del Cliente.
 */
@Dao
interface ResultadoBusquedaPrestadorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRelaciones(lista: List<RelacionBusquedaEntity>)

    /**
     * Obtiene los resultados de una consulta usando la Vista SQL unificada.
     * [LEY #4]: Carga Instantánea.
     * [LEY #14]: Filtrado en la Fuente (SQL-First).
     */
    @Query("""
        SELECT v.* FROM ResultadoBusquedaPrestadorSQLView v
        INNER JOIN relaciones_busqueda r ON v.id = r.idPrestador
        WHERE r.idConsulta = :idConsulta
        AND (:query = '' OR v.titulo LIKE '%' || :query || '%')
        AND (:solo24h = 0 OR v.atiende24h = 1)
        AND (:soloVerificados = 0 OR v.estaVerificado = 1)
        AND (:conEnvio = 0 OR v.realizaEnvios = 1)
        AND (:estaOnline = 0 OR v.estaOnline = 1)
        ORDER BY 
            CASE WHEN :orden = 'reputacion' THEN v.reputacion END DESC,
            r.ordenRanking ASC
    """)
    fun obtenerResultadosPaginados(
        idConsulta: String,
        query: String = "",
        solo24h: Boolean = false,
        soloVerificados: Boolean = false,
        conEnvio: Boolean = false,
        estaOnline: Boolean = false,
        orden: String = "reciente"
    ): PagingSource<Int, ResultadoBusquedaPrestadorSQLView>

    /**
     * Obtiene una lista estática de resultados (Para Radar).
     */
    @Query("""
        SELECT v.* FROM ResultadoBusquedaPrestadorSQLView v
        INNER JOIN relaciones_busqueda r ON v.id = r.idPrestador
        WHERE r.idConsulta = :idConsulta
        AND (:solo24h = 0 OR v.atiende24h = 1)
        AND v.estaOnline = 1
        ORDER BY r.ordenRanking ASC
        LIMIT :limite
    """)
    fun obtenerListaEstatica(
        idConsulta: String,
        solo24h: Boolean = false,
        limite: Int = 10
    ): Flow<List<ResultadoBusquedaPrestadorSQLView>>

    @Query("DELETE FROM relaciones_busqueda WHERE idConsulta = :idConsulta")
    suspend fun limpiarResultadosDeConsulta(idConsulta: String)

    /**
     * 🔥 [ELITE]: Obtiene los datos completos de una lista de IDs desde la Vista SQL.
     * Útil para recuperar Favoritos o selecciones específicas.
     */
    @Query("SELECT * FROM ResultadoBusquedaPrestadorSQLView WHERE id IN (:ids)")
    fun obtenerPorIds(ids: List<String>): Flow<List<ResultadoBusquedaPrestadorSQLView>>
}
