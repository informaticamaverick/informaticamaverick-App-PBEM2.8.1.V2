package com.example.myapplication.core.dominio.motores

import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.DireccionDominio

/**
 * --- MODELOS ANALÍTICOS DE PRESUPUESTOS (v2026.ELITE) ---
 */
data class ModeloPresupuestoAnalitico(
    val presupuesto: PresupuestoConItems,
    val nombrePrestador: String,
    val fotoPrestador: String?,
    val direccionPrestador: DireccionDominio?
)

data class PresupuestoClasificado(
    val presupuesto: PresupuestoConItems,
    val puntaje: Double,
    val reputacion: Float,
    val trabajosRealizados: Int,
    val reconocimientos: List<String>,
    val puntajeRelacionPrecioCalidad: Double,
    val nombrePrestadorAlternativo: String? = null,
    val fotoPrestadorAlternativo: String? = null,
    val direccionPrestadorAlternativo: DireccionDominio? = null
)

data class ElementoGraficoPresupuesto(
    val presupuesto: PresupuestoConItems,
    val total: Double,
    val materiales: Double,
    val manoObra: Double,
    val impuestos: Double,
    val descuentos: Double, // 🔥 [ANALYTICS]
    val esIrrisorio: Boolean,
    val esOptimo: Boolean,
    val nombrePrestadorAlternativo: String? = null,
    val fotoPrestadorAlternativo: String? = null,
    val direccionPrestadorAlternativo: DireccionDominio? = null
)

data class EstadoAnaliticaMercado(
    val elementos: List<ElementoGraficoPresupuesto> = emptyList(),
    val promedioTotal: Double = 0.0,
    val precioMinimo: Double = 0.0,
    val precioMaximo: Double = 0.0,
    val conteoValidos: Int = 0,
    val estaAnalizando: Boolean = true
)

/**
 * --- MOTOR DE INTELIGENCIA DE MERCADO MAVERICK ---
 */
object MotorAnaliticas {
    fun calcularInteligenciaMercado(
        presupuestos: List<PresupuestoConItems>,
        promedio: Double,
        min: Double,
        max: Double
    ): List<PresupuestoClasificado> {
        return presupuestos.map { p ->
            val cabecera = p.cabecera
            val factorPrecio = if (promedio > 0) (cabecera.totalGeneral / promedio) else 1.0
            // El puntaje es inversamente proporcional al precio vs promedio, ponderado por reputación
            val scoreBase = (10.0 / factorPrecio).coerceIn(1.0, 10.0)
            
            PresupuestoClasificado(
                presupuesto = p,
                puntaje = scoreBase,
                reputacion = cabecera.reputacionPrestador ?: 0f,
                trabajosRealizados = cabecera.trabajosPrestador ?: 0,
                reconocimientos = when {
                    cabecera.totalGeneral <= min * 1.05 -> listOf("Mejor Precio")
                    (cabecera.reputacionPrestador ?: 0f) >= 4.8f -> listOf("Top Rated")
                    else -> emptyList()
                },
                puntajeRelacionPrecioCalidad = scoreBase * ((cabecera.reputacionPrestador ?: 1f) / 5.0)
            )
        }.sortedByDescending { it.puntaje }
    }
}

