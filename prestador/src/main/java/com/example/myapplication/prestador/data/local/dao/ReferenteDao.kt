package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity

@Dao
interface ReferenteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferente(referente: ReferenteEntity)

    @Update
    suspend fun updateReferente(referente: ReferenteEntity)

    @Delete
    suspend fun deleteReferente(referente: ReferenteEntity)

    @Query("SELECT * FROM referentes WHERE id = :id")
    suspend fun getReferenteById(id: String): ReferenteEntity?

    @Query("SELECT * FROM referentes WHERE providerId = :providerId AND activo = 1")
    suspend fun getReferentesByProvider(providerId: String): List<ReferenteEntity>

    @Query("SELECT * FROM referentes WHERE empresaId = :empresaId AND activo = 1")
    suspend fun getReferentesByEmpresa(empresaId: String): List<ReferenteEntity>

    @Query("SELECT * FROM referentes WHERE sucursalId = :sucursalId AND activo = 1")
    suspend fun getReferentesBySucursal(sucursalId: String): List<ReferenteEntity>

    @Query("UPDATE referentes SET activo = 0 WHERE id = :id")
    suspend fun desactivarReferente(id: String)
}