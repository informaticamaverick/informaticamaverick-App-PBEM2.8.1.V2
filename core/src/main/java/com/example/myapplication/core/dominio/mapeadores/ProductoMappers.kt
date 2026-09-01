package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.ProductoFinalEntity
import com.example.myapplication.core.datos.local.entidades.TipoProductoFinal
import com.example.myapplication.core.dominio.modelos.ProductoDominio

/**
 * --- PRODUCTO MAPPER (v2026.ELITE) ---
 * [ELITE SSOT]: Centraliza la transformación de Productos Finales (Snapshots).
 */
object ProductoMappers {

    /**
     * Transforma una entidad de ítem final en un Modelo de Dominio.
     */
    fun deEntidadADominio(p: ProductoFinalEntity): ProductoDominio {
        return ProductoDominio(
            id = p.idOriginal ?: p.idLinea.toString(),
            codigo = "", 
            nombre = p.nombreCopiado,
            descripcion = p.descripcionCopiada,
            precio = p.precioSnapshot,
            moneda = "ARS",
            stockActual = 0,
            stockMinimo = 0,
            urlImagen = null,
            miniaturaBase64 = null,
            esServicio = p.tipoItem == TipoProductoFinal.SERVICIO,
            cantidadSeleccionada = p.cantidad,
            totalLinea = p.cantidad * p.precioSnapshot
        )
    }

    /**
     * Mapeo rápido para items de presupuesto (ArticuloPresupuesto).
     */
    fun deArticuloADominio(
        id: String?, 
        codigo: String, 
        descripcion: String, 
        cantidad: Int, 
        precio: Double
    ): ProductoDominio {
        return ProductoDominio(
            id = id,
            codigo = codigo,
            nombre = descripcion,
            descripcion = "",
            precio = precio,
            moneda = "ARS",
            esServicio = true,
            cantidadSeleccionada = cantidad,
            totalLinea = cantidad * precio
        )
    }
}



