package com.example.myapplication.prestador.datos.local.dao

import androidx.room.*
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE PRODUCTOS - COCINA PRIVADA (v2026.ELITE) ---
 */
@Dao
interface ProductoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProducto(producto: ProductoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductos(productos: List<ProductoEntity>)

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerProductoPorId(id: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE idPresupuesto IS NULL AND idBorrador IS NULL")
    fun obtenerCatalogoMaestro(): Flow<List<ProductoEntity>>

    /**
     * 🔥 [ELITE]: Búsqueda Táctica en el catálogo privado (FTS4).
     */
    @Query("""
        SELECT p.* FROM productos p
        JOIN productos_fts fts ON p.rowid = fts.rowid
        WHERE p.idPresupuesto IS NULL AND p.idBorrador IS NULL
        AND productos_fts MATCH :consulta
    """)
    fun buscarEnCatalogo(consulta: String): Flow<List<ProductoEntity>>

    @Delete
    suspend fun eliminarProducto(producto: ProductoEntity)

    @Query("UPDATE productos SET stockActual = stockActual - :cantidad WHERE id = :idProducto")
    suspend fun descontarStock(idProducto: String, cantidad: Int)

    @Query("SELECT * FROM productos WHERE idPresupuesto = :idPresupuesto")
    suspend fun obtenerProductosPorPresupuesto(idPresupuesto: String): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE idBorrador = :idBorrador")
    suspend fun obtenerProductosPorBorrador(idBorrador: String): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE idPropietario = :idPropietario")
    fun obtenerProductosPorPropietario(idPropietario: String): Flow<List<ProductoEntity>>
}

