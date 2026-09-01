package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE PRESUPUESTOS (ELITE v2026) ---
 */
@Dao
interface PresupuestoFinalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(presupuesto: PresupuestoFinalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(presupuestos: List<PresupuestoFinalEntity>)

    @Query("SELECT * FROM presupuestos_finales ORDER BY marcaTiempo DESC")
    fun obtenerTodos(): Flow<List<PresupuestoFinalEntity>>

    @Transaction
    @Query("SELECT * FROM presupuestos_finales WHERE idPresupuesto = :id")
    fun obtenerPorId(id: String): Flow<PresupuestoConItems?>

    @Query("UPDATE presupuestos_finales SET estado = :nuevoEstado WHERE idPresupuesto = :id")
    suspend fun actualizarEstado(id: String, nuevoEstado: com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto)

    @Query("SELECT * FROM presupuestos_finales WHERE idCliente = :idCliente ORDER BY marcaTiempo DESC")
    fun obtenerPorCliente(idCliente: String): Flow<List<PresupuestoFinalEntity>>

    @Query("SELECT * FROM presupuestos_finales WHERE idPrestador = :idPrestador ORDER BY marcaTiempo DESC")
    fun obtenerPorPrestador(idPrestador: String): Flow<List<PresupuestoFinalEntity>>

    @Query("SELECT * FROM presupuestos_finales WHERE idCliente = :id OR idPrestador = :id ORDER BY marcaTiempo DESC")
    fun obtenerTodosParaPerfil(id: String): Flow<List<PresupuestoFinalEntity>>

    @Query("SELECT * FROM presupuestos_finales WHERE (idCliente = :id1 AND idPrestador = :id2) OR (idCliente = :id2 AND idPrestador = :id1) ORDER BY marcaTiempo DESC")
    fun obtenerPresupuestosEntre(id1: String, id2: String): Flow<List<PresupuestoFinalEntity>>

    @Query("""
        SELECT v.* FROM PresupuestoResumenSQLView v
        WHERE (v.idCliente = :idLocal OR v.idPrestador = :idLocal)
        AND (:idConcurso IS NULL OR v.idConcurso = :idConcurso)
        AND (:idRemoto IS NULL OR :idRemoto = 'global' OR (v.idCliente = :idRemoto OR v.idPrestador = :idRemoto))
        AND (:consulta = '' OR v.tituloTrabajo LIKE '%' || :consulta || '%' OR v.numeroPresupuesto LIKE '%' || :consulta || '%' OR v.nombreSoberano LIKE '%' || :consulta || '%')
        AND (
            :idCategoria IS NULL 
            OR (:idCategoria = 'SIN_RUBRO' AND v.idCategoria IS NULL)
            OR v.idCategoria = :idCategoria
        )
        AND (:soloPendientes = 0 OR v.estado = 'PENDIENTE')
        AND (:soloAceptados = 0 OR v.estado = 'ACEPTADO')
        AND (:soloNoLeidos = 0 OR v.leido = 0)
        ORDER BY 
            CASE WHEN :orden = 'sort_amount_asc' THEN v.totalGeneral END ASC,
            CASE WHEN :orden = 'sort_amount_desc' THEN v.totalGeneral END DESC,
            v.marcaTiempo DESC
    """)
    fun buscarPresupuestosSoberanosPaginados(
        idLocal: String,
        idRemoto: String?,
        idConcurso: String?,
        consulta: String,
        idCategoria: String?,
        soloPendientes: Boolean,
        soloAceptados: Boolean,
        soloNoLeidos: Boolean,
        orden: String
    ): androidx.paging.PagingSource<Int, com.example.myapplication.core.datos.local.entidades.vistas.PresupuestoResumenSQLView>

    @Query("""
        SELECT v.idPresupuesto FROM PresupuestoResumenSQLView v
        WHERE (v.idCliente = :idLocal OR v.idPrestador = :idLocal)
        AND (:idConcurso IS NULL OR v.idConcurso = :idConcurso)
        AND (:idRemoto IS NULL OR :idRemoto = 'global' OR (v.idCliente = :idRemoto OR v.idPrestador = :idRemoto))
        AND (:consulta = '' OR v.tituloTrabajo LIKE '%' || :consulta || '%' OR v.numeroPresupuesto LIKE '%' || :consulta || '%' OR v.nombreSoberano LIKE '%' || :consulta || '%')
        AND (
            :idCategoria IS NULL 
            OR (:idCategoria = 'SIN_RUBRO' AND v.idCategoria IS NULL)
            OR v.idCategoria = :idCategoria
        )
        AND (:soloPendientes = 0 OR v.estado = 'PENDIENTE')
        AND (:soloAceptados = 0 OR v.estado = 'ACEPTADO')
        AND (:soloNoLeidos = 0 OR v.leido = 0)
    """)
    fun buscarPresupuestosSoberanosIds(
        idLocal: String,
        idRemoto: String?,
        idConcurso: String?,
        consulta: String,
        idCategoria: String?,
        soloPendientes: Boolean,
        soloAceptados: Boolean,
        soloNoLeidos: Boolean
    ): Flow<List<String>>

    @Query("SELECT DISTINCT IFNULL(idCategoria, 'SIN_RUBRO') FROM presupuestos_finales WHERE (idCliente = :id OR idPrestador = :id)")
    fun obtenerRubrosEnUso(id: String): Flow<List<String>>

    @Query("SELECT * FROM presupuestos_finales WHERE idConcurso = :idConcurso ORDER BY marcaTiempo DESC")
    fun obtenerPorConcurso(idConcurso: String): Flow<List<PresupuestoFinalEntity>>

    @Query("DELETE FROM presupuestos_finales WHERE idPresupuesto = :id")
    suspend fun eliminarPorId(id: String)

    @Transaction
    suspend fun guardarPresupuestoCompleto(
        cabecera: PresupuestoFinalEntity,
        lineas: List<ProductoFinalEntity>,
        finanzas: List<FinanzaFinalEntity>
    ) {
        insertar(cabecera)
        insertarLineas(lineas)
        insertarFinanzas(finanzas)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLineas(lineas: List<ProductoFinalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFinanzas(finanzas: List<FinanzaFinalEntity>)
}
