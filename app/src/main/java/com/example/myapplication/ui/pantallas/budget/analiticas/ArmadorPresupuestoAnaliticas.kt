package com.example.myapplication.ui.pantallas.budget.analiticas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.dominio.motores.EstadoAnaliticaMercado
import com.example.myapplication.core.dominio.motores.ModeloPresupuestoAnalitico
import com.example.myapplication.core.dominio.motores.PresupuestoClasificado
import com.example.myapplication.uishared.estilos.SharedPalette
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto

/**
 * --- 🏗️ ARMADOR DE ANALÍTICAS (ENSAMBLADOR - v2026.ELITE) ---
 * Título: Armador de Presupuesto Analiticas
 * Propósito: Ensamblar los organismos (Secciones) para construir el lienzo de la pantalla.
 * [LEY #10]: Screen Anatomy. Caja > Lienzo.
 * [LEY #9]: Estándar Mav en Español.
 */

@Composable
fun ArmadorCuerpoAnaliticas(
    estadoMercado: EstadoAnaliticaMercado,
    presupuestosAnaliticos: List<ModeloPresupuestoAnalitico>,
    presupuestosRankeados: List<PresupuestoClasificado>,
    alSeleccionarPrestador: (PresupuestoClasificado) -> Unit,
    alHacerClickGrafico: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. BLOQUE DE KPIs ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaKpiAnalitico(
                modifier = Modifier.weight(1f),
                etiqueta = "PROMEDIO MERCADO",
                valor = estadoMercado.promedioTotal,
                colorAcento = Color.White
            )
            TarjetaKpiAnalitico(
                modifier = Modifier.weight(1f),
                etiqueta = "MEJOR OFERTA",
                valor = estadoMercado.precioMinimo,
                colorAcento = SharedPalette.SuccessGreen
            )
        }

        // --- 2. CURVA DE PRECIOS ---
        SeccionCurvaPreciosMasiva(
            presupuestos = presupuestosAnaliticos.map { it.presupuesto },
            alHacerClickMaximizar = alHacerClickGrafico
        )

        // --- 3. RANKING ELITE ---
        if (presupuestosRankeados.isNotEmpty()) {
            SeccionRankingTopElite(
                presupuestosRankeados = presupuestosRankeados,
                alHacerClickPrestador = alSeleccionarPrestador
            )
        }

        // --- 3. MATRIZ TÉCNICA ---
        SeccionMatrizComparativaTecnica(
            presupuestosAnaliticos = presupuestosAnaliticos,
            presupuestosRankeados = presupuestosRankeados,
            alHacerClickPrestador = { model ->
                val ranked = presupuestosRankeados.find { it.presupuesto.cabecera.idPresupuesto == model.presupuesto.cabecera.idPresupuesto }
                if (ranked != null) alSeleccionarPrestador(ranked)
            }
        )

        // --- 4. INSIGHT IA ---
        TarjetaInsightIA(presupuestosRankeados = presupuestosRankeados)

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
private fun PreviewArmadorCuerpoAnaliticas() {
    val mockBudget = PresupuestoFinalEntity(
        idPresupuesto = "1",
        nombrePrestador = "Maverick Test",
        totalGeneral = 45000.0,
        estado = EstadoPresupuesto.ACEPTADO
    )
    val mockRanked = listOf(
        PresupuestoClasificado(
            presupuesto = PresupuestoConItems(
                cabecera = mockBudget,
                lineas = emptyList(),
                finanzas = emptyList()
            ),
            puntaje = 9.5,
            reputacion = 4.8f,
            trabajosRealizados = 120,
            reconocimientos = listOf("Top Rated", "Mejor Precio"),
            puntajeRelacionPrecioCalidad = 9.8
        )
    )
    
    ClienteTheme {
        ArmadorCuerpoAnaliticas(
            estadoMercado = EstadoAnaliticaMercado(estaAnalizando = false, promedioTotal = 50000.0, precioMinimo = 45000.0),
            presupuestosAnaliticos = emptyList(),
            presupuestosRankeados = mockRanked,
            alSeleccionarPrestador = {},
            alHacerClickGrafico = {}
        )
    }
}

