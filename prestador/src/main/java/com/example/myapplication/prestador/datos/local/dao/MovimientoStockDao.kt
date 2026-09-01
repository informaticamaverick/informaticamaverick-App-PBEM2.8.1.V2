package com.example.myapplication.prestador.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.prestador.datos.local.entidades.MovimientoStockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoStockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarMovimiento(movimiento: MovimientoStockEntity)

    @Query("SELECT * FROM movimientos_stock WHERE idProducto = :idProducto ORDER BY marcaTiempo DESC")
    fun obtenerHistorialProducto(idProducto: String): Flow<List<MovimientoStockEntity>>

    @Query("SELECT SUM(cantidad) FROM movimientos_stock WHERE idProducto = :idProducto")
    suspend fun calcularStockReal(idProducto: String): Int
}
