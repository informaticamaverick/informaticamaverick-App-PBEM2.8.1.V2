package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.DireccionEntity

@Dao
interface DireccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDireccion(direccion: DireccionEntity)

    @Update
    suspend fun updateDireccion(direccion: DireccionEntity)

    @Delete
    suspend fun deleteDireccion(direccion: DireccionEntity)

    @Query("SELECT * FROM direcciones WHERE id = :id")
    suspend fun getDireccionById(id: String): DireccionEntity?

    @Query("SELECT * FROM direcciones WHERE referenciaId = :referenciaId AND referenciaTipo =:tipo")
    suspend fun getDireccionByReferencia(referenciaId: String, tipo: String): DireccionEntity?

    @Query("DELETE FROM direcciones WHERE referenciaId = :referenciaId")
    suspend fun deleteDireccionesByReferencia(referenciaId: String)

}