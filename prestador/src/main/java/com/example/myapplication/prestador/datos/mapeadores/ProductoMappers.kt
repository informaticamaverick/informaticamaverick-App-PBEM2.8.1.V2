package com.example.myapplication.prestador.datos.mapeadores

import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.dominio.modelos.ProductoDominio

/**
 * --- PRODUCTO MAPPER PRESTADOR (v2026.ELITE) ---
 * [PROPÓSITO]: Mapear entidades del catálogo local (Cocina Privada) al Dominio.
 */
object ProductoMappers {

    fun deEntidadADominio(
        p: ProductoEntity,
        mapaCategorias: Map<String, CategoriaEntity> = emptyMap()
    ): ProductoDominio {
        val catInfo = mapaCategorias[p.idCategoria]
        return ProductoDominio(
            id = p.id,
            codigo = p.sku ?: p.codigoBarras ?: "",
            nombre = p.nombre,
            descripcion = p.descripcion,
            precio = p.precioVenta,
            precioCosto = p.precioCosto,
            impuestoDefault = p.impuestoDefault,
            descuentoDefault = p.descuentoDefault,
            moneda = p.moneda,
            stockActual = p.stockActual,
            stockMinimo = p.stockMinimo,
            urlImagen = p.urlImagen,
            miniaturaBase64 = p.miniaturaBase64,
            esServicio = p.tipo == TipoProducto.SERVICIO,
            idCategoria = p.idCategoria,
            nombreCategoria = catInfo?.nombre,
            iconoCategoria = catInfo?.icono,
            cantidadSeleccionada = p.cantidad,
            totalLinea = p.cantidad * p.precioVenta
        )
    }

    fun deDominioAEntidad(p: ProductoDominio, idPropietario: String): ProductoEntity {
        return ProductoEntity(
            id = p.id ?: java.util.UUID.randomUUID().toString(),
            idPropietario = idPropietario,
            nombre = p.nombre,
            descripcion = p.descripcion,
            precioCosto = p.precioCosto,
            precioVenta = p.precio,
            cantidad = p.cantidadSeleccionada,
            impuestoDefault = p.impuestoDefault,
            descuentoDefault = p.descuentoDefault,
            moneda = p.moneda,
            sku = p.codigo,
            idCategoria = p.idCategoria,
            stockActual = p.stockActual,
            stockMinimo = p.stockMinimo,
            tipo = if (p.esServicio) TipoProducto.SERVICIO else TipoProducto.PRODUCTO,
            urlImagen = p.urlImagen,
            miniaturaBase64 = p.miniaturaBase64
        )
    }
}




