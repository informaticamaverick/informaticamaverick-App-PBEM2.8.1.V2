package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA GESTIÓN DE EVENTOS DE CALENDARIO ---
 */
@Dao
interface EventoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evento: EventoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(eventos: List<EventoEntity>)

    @Query("SELECT * FROM eventos WHERE id = :id")
    fun obtenerPorId(id: String): Flow<EventoEntity?>

    @Query("SELECT * FROM eventos WHERE idChat = :idChat")
    fun obtenerPorChat(idChat: String): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE idCliente = :idCliente ORDER BY fechaInicioUtc ASC")
    fun obtenerPorCliente(idCliente: String): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE idPropietarioSucursal = :idPropietario ORDER BY fechaInicioUtc ASC")
    fun obtenerPorPropietarioSucursal(idPropietario: String): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE idPropietarioSucursal = :idPrestador ORDER BY fechaInicioUtc ASC")
    suspend fun obtenerPorPrestadorSync(idPrestador: String): List<EventoEntity>

    @Query("SELECT COUNT(*) FROM eventos WHERE idRecurso = :idRecurso AND estado NOT IN ('CANCELADO') AND ((fechaInicioUtc >= :inicioDia AND fechaInicioUtc < :finDia) OR (fechaFinUtc > :inicioDia AND fechaFinUtc <= :finDia) OR (fechaInicioUtc <= :inicioDia AND fechaFinUtc >= :finDia))")
    suspend fun verificarOcupacionRecurso(idRecurso: String, inicioDia: Long, finDia: Long): Int

    /**
     * [ELITE]: Obtiene eventos filtrados por sucursal y rango de tiempo.
     */
    @Query("SELECT * FROM eventos WHERE idSucursal = :idSucursal AND fechaInicioUtc >= :inicioDia AND fechaInicioUtc <= :finDia")
    fun obtenerEventosDiaSucursal(idSucursal: String, inicioDia: Long, finDia: Long): Flow<List<EventoEntity>>

    @Query("SELECT COUNT(*) FROM eventos")
    suspend fun obtenerConteoTotal(): Int

    @Query("SELECT * FROM eventos ORDER BY fechaInicioUtc ASC")
    fun obtenerTodos(): Flow<List<EventoEntity>>

    @Query("UPDATE eventos SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: String, nuevoEstado: com.example.myapplication.core.datos.local.entidades.EstadoEvento)

    /**
     * [LEY #14]: Búsqueda en la fuente.
     */
    @Query("""
        SELECT * FROM eventos 
        WHERE idCliente = :idCliente 
        AND (titulo LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%')
        ORDER BY fechaInicioUtc ASC
    """)
    fun buscarPorCliente(idCliente: String, query: String): Flow<List<EventoEntity>>

    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM eventos WHERE id IN (:ids)")
    suspend fun eliminarMasivo(ids: List<String>)
}
