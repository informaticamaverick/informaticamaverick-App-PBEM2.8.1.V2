package com.example.myapplication.prestador.datos.local.dao

import androidx.room.*
import com.example.myapplication.prestador.datos.local.entidades.PresupuestoEntity
import com.example.myapplication.prestador.datos.local.entidades.BorradorPresupuestoEntity
import com.example.myapplication.prestador.datos.local.entidades.PlantillaPresupuestoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.prestador.datos.local.entidades.relaciones.PresupuestoCocinaConItems
import com.example.myapplication.prestador.datos.local.entidades.relaciones.BorradorCocinaConItems
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE PRESUPUESTOS - COCINA PRIVADA (v2026.ELITE) ---
 */
@Dao
interface PresupuestoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPresupuesto(presupuesto: PresupuestoEntity)

    @Query("SELECT * FROM presupuestos ORDER BY marcaTiempo DESC")
    fun obtenerTodos(): Flow<List<PresupuestoEntity>>

    @Transaction
    @Query("SELECT * FROM presupuestos WHERE idPresupuesto = :id")
    fun obtenerConItems(id: String): Flow<PresupuestoCocinaConItems?>

    @Query("UPDATE presupuestos SET estado = :estado WHERE idPresupuesto = :id")
    suspend fun actualizarEstado(id: String, estado: EstadoPresupuesto)

    @Delete
    suspend fun eliminarPresupuesto(presupuesto: PresupuestoEntity)

    // --- SECTOR: BORRADORES ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarBorrador(borrador: BorradorPresupuestoEntity)

    @Query("SELECT * FROM borradores_presupuesto WHERE idBorrador = :id")
    suspend fun obtenerBorrador(id: String): BorradorPresupuestoEntity?

    @Transaction
    @Query("SELECT * FROM borradores_presupuesto WHERE idBorrador = :id")
    fun obtenerBorradorConItems(id: String): Flow<BorradorCocinaConItems?>

    @Query("DELETE FROM borradores_presupuesto WHERE idBorrador = :id")
    suspend fun eliminarBorrador(id: String)
}

