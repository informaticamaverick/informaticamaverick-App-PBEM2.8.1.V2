package com.example.myapplication.core.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.datos.local.entidades.CuentaEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE CUENTAS (SSOT) ---
 */
@Dao
interface CuentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cuenta: CuentaEntity)

    @Query("SELECT * FROM cuentas WHERE id = :uid")
    fun obtenerPorId(uid: String): Flow<CuentaEntity?>

    @Query("SELECT * FROM cuentas WHERE id = :uid")
    suspend fun obtenerPorIdSync(uid: String): CuentaEntity?

    @Query("DELETE FROM cuentas WHERE id = :uid")
    suspend fun eliminarCuenta(uid: String)

    @Query("DELETE FROM cuentas")
    suspend fun eliminarTodas()

    @Query("UPDATE cuentas SET estaSuscrito = :activa WHERE id = :uid")
    suspend fun actualizarEstadoSuscripcion(uid: String, activa: Boolean)

    @Query("UPDATE cuentas SET idPerfilActivo = :idPerfilActivo, priorizarEmpresa = :priorizarEmpresa WHERE id = :uid")
    suspend fun actualizarModoSoberania(uid: String, idPerfilActivo: String?, priorizarEmpresa: Boolean)
}
