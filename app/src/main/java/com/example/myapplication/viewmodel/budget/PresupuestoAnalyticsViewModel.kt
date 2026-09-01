package com.example.myapplication.viewmodel.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.datos.local.entidades.TipoProductoFinal
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.repositorios.PresupuestoRepositorio
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.dominio.motores.ModeloPresupuestoAnalitico
import com.example.myapplication.core.dominio.motores.EstadoAnaliticaMercado
import com.example.myapplication.core.dominio.motores.ElementoGraficoPresupuesto
import com.example.myapplication.core.dominio.motores.PresupuestoClasificado
import com.example.myapplication.core.dominio.motores.MotorAnaliticas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE ANALÍTICAS DE PRESUPUESTOS (v2026.ELITE) ---
 * [PROPÓSITO]: Realizar cálculos comerciales y comparativas sin sobrecargar otros módulos.
 * [LEY #1]: Pantallas tontas. Toda la lógica de inteligencia reside aquí.
 */
@HiltViewModel
class PresupuestoAnalyticsViewModel @Inject constructor(
    private val presupuestoRepositorio: PresupuestoRepositorio,
    private val prestadorDao: IdentidadPrestadorDao
) : ViewModel() {

    private val _presupuestosAnaliticos = MutableStateFlow<List<ModeloPresupuestoAnalitico>>(emptyList())
    val presupuestosAnaliticos = _presupuestosAnaliticos.asStateFlow()

    private val _concursoVirtual = MutableStateFlow<ConcursoPublicoEntity?>(null)
    val concursoVirtual = _concursoVirtual.asStateFlow()

    private val _estadoMercado = MutableStateFlow(EstadoAnaliticaMercado())
    val estadoMercado = _estadoMercado.asStateFlow()

    private val _presupuestosRankeados = MutableStateFlow<List<PresupuestoClasificado>>(emptyList())
    val presupuestosRankeados = _presupuestosRankeados.asStateFlow()

    fun inicializarConPresupuestos(ids: List<String>, concursoReal: ConcursoPublicoEntity? = null) {
        val virtual = concursoReal ?: ConcursoPublicoEntity(
            idConcurso = "VIRTUAL_COMPARE",
            titulo = "Comparativa de Archivo",
            descripcion = "Análisis de presupuestos seleccionados desde el archivero."
        )
        _concursoVirtual.value = virtual

        viewModelScope.launch {
            val presupuestos = ids.mapNotNull { presupuestoRepositorio.obtenerPresupuestoPorId(it).firstOrNull() }

            // Transformamos al modelo analítico enriquecido
            val analiticos = presupuestos.map { p ->
                val cabecera = p.cabecera
                val pInfo = prestadorDao.obtenerPorId(cabecera.idPrestador).firstOrNull()
                ModeloPresupuestoAnalitico(
                    presupuesto = p,
                    nombrePrestador = pInfo?.nombreVisible ?: cabecera.nombrePrestador,
                    fotoPrestador = pInfo?.miniaturaBase64 ?: pInfo?.urlFotoPerfil ?: cabecera.urlFotoPrestador,
                    direccionPrestador = null 
                )
            }
            _presupuestosAnaliticos.value = analiticos

            // Realizamos cálculos de mercado (Ley #1)
            calcularAnaliticas(analiticos)
        }
    }

    private fun calcularAnaliticas(budgets: List<ModeloPresupuestoAnalitico>) {
        if (budgets.isEmpty()) {
            _estadoMercado.value = EstadoAnaliticaMercado(estaAnalizando = false)
            return
        }

        val listaPresupuestos = budgets.map { it.presupuesto }
        val avg = listaPresupuestos.map { it.cabecera.totalGeneral }.average()
        val minVal = listaPresupuestos.minOf { it.cabecera.totalGeneral }
        val maxVal = listaPresupuestos.maxOf { it.cabecera.totalGeneral }

        val itemsGraficos = budgets.map { model ->
            val b = model.presupuesto
            val cabecera = b.cabecera
            val total = cabecera.totalGeneral
            val esIrrisorio = total > avg * 1.8 
            val esOptimo = total <= avg && total >= minVal * 0.9

            ElementoGraficoPresupuesto(
                presupuesto = b,
                total = total,
                materiales = cabecera.subtotalArticulos,
                manoObra = cabecera.subtotalServicios,
                impuestos = cabecera.totalImpuestos,
                descuentos = cabecera.totalDescuentos, // 🔥 [ANALYTICS]
                esIrrisorio = esIrrisorio,
                esOptimo = esOptimo,
                nombrePrestadorAlternativo = model.nombrePrestador,
                fotoPrestadorAlternativo = model.fotoPrestador,
                direccionPrestadorAlternativo = model.direccionPrestador
            )
        }

        val estado = EstadoAnaliticaMercado(itemsGraficos, avg, minVal, maxVal, itemsGraficos.count { !it.esIrrisorio }, false)
        _estadoMercado.value = estado

        // Clasificación de Inteligencia (app SCORE)
        val baseRanked = MotorAnaliticas.calcularInteligenciaMercado(listaPresupuestos, avg, minVal, maxVal).take(10)
        _presupuestosRankeados.value = baseRanked.map { ranked ->
            val model = budgets.find { it.presupuesto.cabecera.idPresupuesto == ranked.presupuesto.cabecera.idPresupuesto }
            ranked.copy(
                nombrePrestadorAlternativo = model?.nombrePrestador, 
                fotoPrestadorAlternativo = model?.fotoPrestador,
                direccionPrestadorAlternativo = model?.direccionPrestador
            )
        }
    }
}



