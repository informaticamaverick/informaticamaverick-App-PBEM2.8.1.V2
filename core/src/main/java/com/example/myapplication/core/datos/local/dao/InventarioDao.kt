package com.example.myapplication.core.datos.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.myapplication.core.datos.local.entidades.vistas.InventarioSucursalSQLView
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE INVENTARIO (v2026.ELITE) ---
 * [PROPÓSITO]: Acceso a la vista unificada de recursos y equipo.
 */
@Dao
interface InventarioDao {
    
    @Query("SELECT * FROM v_inventario_sucursal WHERE idSucursal = :idSucursal")
    fun obtenerInventarioPorSucursal(idSucursal: String): Flow<List<InventarioSucursalSQLView>>

    @Query("""
        SELECT * FROM v_inventario_sucursal 
        WHERE idSucursal = :idSucursal 
        AND (nombre LIKE '%' || :query || '%' OR categoria LIKE '%' || :query || '%')
    """)
    fun buscarEnInventario(idSucursal: String, query: String): Flow<List<InventarioSucursalSQLView>>
}
