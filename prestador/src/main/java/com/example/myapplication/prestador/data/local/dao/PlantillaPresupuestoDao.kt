package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.PlantillaPresupuestoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantillaPresupuestoDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plantilla: PlantillaPresupuestoEntity)

    @Query("SELECT * FROM plantillas_presupuesto ORDER BY createdAT DESC")
    fun getAll(): Flow<List<PlantillaPresupuestoEntity>>

    @Query("SELECT * FROM plantillas_presupuesto ORDER BY createdAT DESC")
    suspend fun getAllSync(): List<PlantillaPresupuestoEntity>

    @Query("DELETE FROM plantillas_presupuesto WHERE id = :id")
    suspend fun deleteById(id: String)

}