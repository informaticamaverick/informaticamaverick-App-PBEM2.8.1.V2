package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.PresupuestoDominio
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PRESUPUESTO MAPPER (ELITE v2026.FINAL) ---
 * [ELITE SSOT]: Centraliza la transformación de Transacciones Comerciales.
 */
object PresupuestoMappers {

    /**
     * Mapea a modelo de Detalle Completo desde la relación soberana.
     */
    fun aDetalleUi(relacion: PresupuestoConItems, esMio: Boolean = false): PresupuestoDominio {
        val entidad = relacion.cabecera
        val lineas = relacion.lineas
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        // Clasificamos las líneas según su tipo
        val articulos = lineas.filter { it.tipoItem == TipoProductoFinal.PRODUCTO }.map {
            ArticuloPresupuesto(
                id = it.idLinea,
                idProducto = it.idOriginal,
                descripcion = it.nombreCopiado,
                cantidad = it.cantidad,
                precioUnitario = it.precioSnapshot,
                porcentajeImpuesto = it.porcentajeImpuesto,
                porcentajeDescuento = it.porcentajeDescuento
            )
        }
        
        val servicios = lineas.filter { it.tipoItem == TipoProductoFinal.SERVICIO }.map {
            ServicioPresupuesto(
                id = it.idLinea,
                idProducto = it.idOriginal,
                descripcion = it.nombreCopiado,
                precioUnitario = it.precioSnapshot,
                porcentajeDescuento = it.porcentajeDescuento,
                total = it.cantidad * it.precioSnapshot
            )
        }

        return PresupuestoDominio(
            id = entidad.idPresupuesto,
            numero = entidad.numeroPresupuesto ?: "S/N",
            titulo = entidad.tituloTrabajo ?: "Presupuesto de Servicio",
            prestadorNombre = entidad.nombrePrestador,
            empresaNombre = null, // En FinalEntity no guardamos empresa por ahora
            idCategoria = entidad.idCategoria,
            estado = entidad.estado,
            articulos = articulos,
            servicios = servicios,
            subtotalTexto = "$ ${String.format(Locale.getDefault(), "%,.2f", entidad.totalGeneral)}", // Simplificado
            totalTexto = "$ ${String.format(Locale.getDefault(), "%,.2f", entidad.totalGeneral)}",
            fechaTexto = sdf.format(Date(entidad.marcaTiempo)),
            notaLegal = null,
            tipo = entidad.tipo,
            esMio = esMio
        )
    }

    fun aResumenDominio(
        entidad: PresupuestoFinalEntity, 
        foto: Any? = null, 
        miniatura: String? = null, // 🔥 [NEW]
        nombreCat: String? = null, 
        iconoCat: String? = null,
        suscrito: Boolean = false
    ): PresupuestoResumenDominio {
        return PresupuestoResumenDominio(
            idPresupuesto = entidad.idPresupuesto,
            numeroPresupuesto = entidad.numeroPresupuesto,
            tituloTrabajo = entidad.tituloTrabajo,
            totalGeneral = entidad.totalGeneral,
            estado = entidad.estado,
            fechaTimestamp = entidad.marcaTiempo,
            esLeido = entidad.leido,
            idPrestador = entidad.idPrestador,
            nombrePrestador = entidad.nombrePrestador,
            fotoPrestador = foto ?: entidad.urlFotoPrestador,
            urlMiniatura = miniatura ?: entidad.urlMiniatura, // 🔥 [ELITE SSOT]
            idConcurso = entidad.idConcurso,
            idCategoria = entidad.idCategoria,
            nombreCategoria = nombreCat,
            iconoCategoria = iconoCat,
            estaSuscrito = suscrito
        )
    }
}



