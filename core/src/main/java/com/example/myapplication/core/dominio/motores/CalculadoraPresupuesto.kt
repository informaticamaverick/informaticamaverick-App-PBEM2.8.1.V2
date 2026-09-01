package com.example.myapplication.core.dominio.motores

import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems

/**
 * --- MOTOR DE CÁLCULO DE PRESUPUESTOS (V2026.SUPREME) ---
 * Centraliza la lógica de negocio para asegurar paridad entre apps.
 * [LEY #9]: Nombres en español y unificación SUPREME.
 */
object CalculadoraPresupuesto {

    data class ResultadoCalculo(
        val subtotal: Double = 0.0,      // Base imponible (antes de IVA y descuentos)
        val montoImpuestos: Double = 0.0, // Total de carga impositiva
        val montoDescuento: Double = 0.0, // Total de bonificaciones
        val totalGeneral: Double = 0.0,   // Número final (Subtotal + Impuestos - Descuentos)
        val totalMateriales: Double = 0.0,
        val totalManoObra: Double = 0.0,
        val totalGastos: Double = 0.0,
        val intereses: Double = 0.0,
        val costoTotal: Double = 0.0,
        val gananciaEstimada: Double = 0.0
    )

    /**
     * Realiza un cálculo profundo del presupuesto.
     */
    fun calcularTodo(
        articulos: List<ArticuloPresupuesto>,
        servicios: List<ServicioPresupuesto>,
        gastos: List<GastoVarioPresupuesto>,
        impuestos: List<ImpuestoPresupuesto>
    ): ResultadoCalculo {
        
        // 1. Cálculos de Base (Subtotales puros)
        val baseArticulos = articulos.sumOf { it.precioUnitario * it.cantidad }
        val baseServicios = servicios.sumOf { it.precioUnitario }
        val baseGastos = gastos.sumOf { it.precioUnitario }
        
        val subtotalGlobal = baseArticulos + baseServicios + baseGastos

        // 2. Cálculo de Impuestos
        val impuestoArticulos = articulos.sumOf { (it.precioUnitario * it.cantidad) * (it.porcentajeImpuesto / 100.0) }
        val impuestosExtra = impuestos.sumOf { it.monto }
        val totalImpuestos = impuestoArticulos + impuestosExtra

        // 3. Cálculo de Descuentos e Intereses (Monto fijo + Porcentaje)
        val descArticulos = articulos.sumOf { 
            val subItem = calcularSubtotalItem(
                precioUnitario = it.precioUnitario,
                cantidad = it.cantidad,
                montoDescuento = it.montoDescuento,
                porcentajeDescuento = it.porcentajeDescuento,
                montoInteres = it.montoInteres,
                porcentajeInteres = it.porcentajeInteres
            )
            (it.precioUnitario * it.cantidad) - subItem
        }
        
        val descServicios = servicios.sumOf {
            val subItem = calcularSubtotalItem(
                precioUnitario = it.precioUnitario,
                cantidad = 1,
                montoDescuento = it.montoDescuento,
                porcentajeDescuento = it.porcentajeDescuento,
                montoInteres = it.montoInteres,
                porcentajeInteres = it.porcentajeInteres
            )
            it.precioUnitario - subItem
        }

        val descGastos = gastos.sumOf {
            val subItem = calcularSubtotalItem(
                precioUnitario = it.precioUnitario,
                cantidad = 1,
                montoDescuento = it.montoDescuento,
                porcentajeDescuento = it.porcentajeDescuento,
                montoInteres = it.montoInteres,
                porcentajeInteres = it.porcentajeInteres
            )
            it.precioUnitario - subItem
        }
        
        val totalDescuentosNegativos = descArticulos + descServicios + descGastos
        // NOTA: Si el resultado es negativo significa que hay más intereses que descuentos.
        // Pero para mantener la compatibilidad con el DTO ResultadoCalculo:
        
        val totalCosto = articulos.sumOf { it.precioCosto * it.cantidad }

        return ResultadoCalculo(
            subtotal = subtotalGlobal,
            montoImpuestos = totalImpuestos,
            montoDescuento = if (totalDescuentosNegativos > 0) totalDescuentosNegativos else 0.0,
            intereses = if (totalDescuentosNegativos < 0) -totalDescuentosNegativos else 0.0,
            totalGeneral = subtotalGlobal + totalImpuestos - totalDescuentosNegativos,
            totalMateriales = baseArticulos,
            totalManoObra = baseServicios,
            totalGastos = baseGastos,
            costoTotal = totalCosto,
            gananciaEstimada = (subtotalGlobal - totalDescuentosNegativos) - totalCosto
        )
    }

    /**
     * Calcula el subtotal de un ítem individual aplicando descuentos e intereses.
     * Orden: (Precio * Cantidad) - DescuentoFijo -> Aplicar Descuento% -> + InteresFijo -> Aplicar Interes%
     */
    fun calcularSubtotalItem(
        precioUnitario: Double,
        cantidad: Int,
        montoDescuento: Double = 0.0,
        porcentajeDescuento: Double = 0.0,
        montoInteres: Double = 0.0,
        porcentajeInteres: Double = 0.0
    ): Double {
        val base = precioUnitario * cantidad
        
        // 1. Descuento Fijo (por unidad si se desea, o total. Aquí se asume total según LEY #9)
        // Pero el usuario dijo "si el articulo cuesta 1000 y hago un descunto fijo de 100 , debe aparecer 900".
        // Asumimos que el montoDescuento es el total del renglón para que sea consistente con la UI.
        val trasDescuentoFijo = base - montoDescuento
        
        // 2. Descuento Porcentual
        val trasDescuentoPorc = trasDescuentoFijo * (1 - (porcentajeDescuento / 100.0))
        
        // 3. Interés Fijo
        val trasInteresFijo = trasDescuentoPorc + montoInteres
        
        // 4. Interés Porcentual
        return trasInteresFijo * (1 + (porcentajeInteres / 100.0))
    }

    /**
     * Calcula el porcentaje que representa un monto sobre una base.
     */
    fun calcularPorcentajeDesdeMonto(base: Double, monto: Double): Double {
        if (base <= 0) return 0.0
        return (monto * 100.0) / base
    }

    /**
     * Calcula el monto que representa un porcentaje sobre una base.
     */
    fun calcularMontoDesdePorcentaje(base: Double, porcentaje: Double): Double {
        return base * (porcentaje / 100.0)
    }

    /**
     * Cálculo rápido para resúmenes de presupuestos finales (Mesa).
     */
    fun calcularResumen(relacion: PresupuestoConItems): ResultadoCalculo {
        val h = relacion.cabecera
        return ResultadoCalculo(
            subtotal = h.subtotal,
            montoImpuestos = h.totalImpuestos,
            montoDescuento = h.totalDescuentos,
            totalGeneral = h.totalGeneral,
            totalMateriales = h.subtotalArticulos,
            totalManoObra = h.subtotalServicios,
            totalGastos = h.subtotalGastos
        )
    }
}

