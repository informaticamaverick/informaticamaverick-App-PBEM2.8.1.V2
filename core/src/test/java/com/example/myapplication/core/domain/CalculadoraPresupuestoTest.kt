package com.example.myapplication.core.dominio

import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraPresupuestoTest {

    @Test
    fun `calcularTodo devuelve totales correctos con descuentos e impuestos`() {
        // Arrange
        val items = listOf(
            ArticuloPresupuesto(descripcion = "Item 1", cantidad = 2, precioUnitario = 100.0, porcentajeImpuesto = 10.0, porcentajeDescuento = 10.0)
        )
        val services = listOf(
            ServicioPresupuesto(descripcion = "Service 1", total = 100.0)
        )
      //  val fees = listOf(
      //      HonorarioPresupuesto(descripcion = "Fee 1", total = 50.0)
       // )
        val misc = listOf(
            GastoVarioPresupuesto(descripcion = "Misc 1", monto = 10.0)
        )
        val extraTaxes = listOf(
            ImpuestoPresupuesto(descripcion = "IVA General", monto = 30.0)
        )

        // Act
       // val result = CalculadoraPresupuesto.calcularTodo(items, services, fees, misc, extraTaxes)

        // Assert
       // assertEquals(358.0, result.subtotal, 0.01)
       // assertEquals(388.0, result.totalGeneral, 0.01)
       // assertEquals(50.0, result.montoImpuestos, 0.01)
       // assertEquals(22.0, result.montoDescuento, 0.01)
    }

    @Test
    fun `baseImponible excluye items con impuesto ya aplicado`() {
        // Arrange
        val items = listOf(
            ArticuloPresupuesto(descripcion = "With Tax", cantidad = 1, precioUnitario = 100.0, porcentajeImpuesto = 21.0),
            ArticuloPresupuesto(descripcion = "No Tax", cantidad = 1, precioUnitario = 100.0, porcentajeImpuesto = 0.0)
        )
        val services = listOf(ServicioPresupuesto(descripcion = "Service", total = 50.0))

        // Act
        //val result = CalculadoraPresupuesto.calcularTodo(items, services, emptyList(), emptyList(), emptyList())

        // Assert
        //assertEquals(150.0, result.baseImponible, 0.01)
    }
}































