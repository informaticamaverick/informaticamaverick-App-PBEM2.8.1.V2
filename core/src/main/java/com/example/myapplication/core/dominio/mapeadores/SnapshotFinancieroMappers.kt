package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import java.util.UUID

/**
 * --- SNAPSHOT FINANCIERO MAPPER (v2026.SUPREME) ---
 * [PROPÓSITO]: Congelar el estado económico (precios, costos, impuestos) al enviar un presupuesto.
 * [LEY #16]: Inmutabilidad Contable.
 */
object SnapshotFinancieroMappers {

    /**
     * Crea un Snapshot inmutable listo para ser enviado y guardado en la "Mesa Final".
     * [NOTA]: Toda labor profesional se mapea a SERVICIO según ley SUPREME.
     */
    fun crearSnapshotFinal(
        idPresupuesto: String = UUID.randomUUID().toString(),
        idCliente: String,
        idPrestador: String,
        idConcurso: String? = null,
        titulo: String,
        subtotal: Double,
        total: Double,
        idCategoria: String? = null,
        nombrePrestador: String = "",
        fotoPrestador: String? = null,
        articulos: List<ArticuloPresupuesto> = emptyList(),
        servicios: List<ServicioPresupuesto> = emptyList(),
        gastos: List<GastoVarioPresupuesto> = emptyList(),
        impuestos: List<ImpuestoPresupuesto> = emptyList(),
        tipo: TipoPresupuesto = TipoPresupuesto.NUEVO,
        etiquetaManoObra: String = "MANO DE OBRA"
    ): PresupuestoConItems {
        
        // 🔥 [SUPREME]: Delegamos el cálculo final al motor central para evitar discrepancias
        val calc = CalculadoraPresupuesto.calcularTodo(articulos, servicios, gastos, impuestos)

        val cabecera = PresupuestoFinalEntity(
            idPresupuesto = idPresupuesto,
            idCliente = idCliente,
            idPrestador = idPrestador,
            idConcurso = idConcurso,
            tituloTrabajo = titulo,
            idCategoria = idCategoria,
            nombrePrestador = nombrePrestador,
            urlFotoPrestador = fotoPrestador,
            subtotal = calc.subtotal,
            totalGeneral = calc.totalGeneral,
            subtotalArticulos = calc.totalMateriales,
            subtotalServicios = calc.totalManoObra,
            subtotalGastos = calc.totalGastos,
            totalImpuestos = calc.montoImpuestos,
            totalDescuentos = calc.montoDescuento,
            totalCostoGeral = calc.costoTotal,
            tipo = tipo,
            etiquetaManoObra = etiquetaManoObra
        )

        val lineas = mutableListOf<ProductoFinalEntity>()
        
        // 1. Mapeo de Materiales
        articulos.forEach {
            lineas.add(ProductoFinalEntity(
                idPresupuesto = idPresupuesto,
                idOriginal = it.idProducto,
                nombreCopiado = it.descripcion,
                cantidad = it.cantidad,
                precioSnapshot = it.precioUnitario,
                precioCostoSnapshot = it.precioCosto,
                porcentajeImpuesto = it.porcentajeImpuesto,
                porcentajeDescuento = it.porcentajeDescuento,
                tipoItem = TipoProductoFinal.PRODUCTO
            ))
        }

        // 2. Mapeo de Mano de Obra (Servicios)
        servicios.forEach {
            lineas.add(ProductoFinalEntity(
                idPresupuesto = idPresupuesto,
                idOriginal = it.idProducto,
                nombreCopiado = it.descripcion,
                cantidad = 1,
                precioSnapshot = it.precioUnitario,
                porcentajeDescuento = it.porcentajeDescuento,
                tipoItem = TipoProductoFinal.SERVICIO
            ))
        }

        // 3. Mapeo de Gastos
        gastos.forEach {
            lineas.add(ProductoFinalEntity(
                idPresupuesto = idPresupuesto,
                nombreCopiado = it.descripcion,
                cantidad = 1,
                precioSnapshot = it.precioUnitario,
                porcentajeDescuento = it.porcentajeDescuento,
                tipoItem = TipoProductoFinal.GASTO
            ))
        }

        val finanzas = impuestos.map {
            FinanzaFinalEntity(
                idPresupuesto = idPresupuesto,
                etiqueta = it.descripcion,
                monto = it.monto,
                tipo = TipoFinanzaFinal.IMPUESTO
            )
        }

        return PresupuestoConItems(
            cabecera = cabecera,
            lineas = lineas,
            finanzas = finanzas
        )
    }
}




