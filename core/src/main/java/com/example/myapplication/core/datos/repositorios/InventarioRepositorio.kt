package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.InventarioDao
import com.example.myapplication.core.dominio.mapeadores.InventarioMappers
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE INVENTARIO (v2026.ELITE) ---
 * [PROPÓSITO]: Proveer acceso unificado al inventario de sucursales.
 */
@Singleton
class InventarioRepositorio @Inject constructor(
    private val inventarioDao: InventarioDao
) {
    
    fun obtenerInventarioPorSucursal(idSucursal: String): Flow<List<InventarioActivoDominio>> {
        return inventarioDao.obtenerInventarioPorSucursal(idSucursal).map {
            InventarioMappers.deListaVistaAListaDominio(it)
        }
    }

    fun buscarEnInventario(idSucursal: String, query: String): Flow<List<InventarioActivoDominio>> {
        return inventarioDao.buscarEnInventario(idSucursal, query).map {
            InventarioMappers.deListaVistaAListaDominio(it)
        }
    }
}


