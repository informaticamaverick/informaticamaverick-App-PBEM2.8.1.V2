package com.example.myapplication.prestador.viewmodel.presupuesto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.prestador.datos.local.entidades.PresupuestoEntity
import com.example.myapplication.prestador.datos.repositorios.PrestadorPresupuestoRepositorio
import com.example.myapplication.prestador.datos.repositorios.ConsultasPrestadorRepositorio
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogWrapper(
    val itemsJson: String = "",
    val serviciosJson: String = ""
)

/**
 * --- VIEWMODEL DE PRESUPUESTOS (ELITE v2026.FINAL) ---
 */
@HiltViewModel
class PrestadorPresupuestoViewModel @Inject constructor(
    private val repositorio: PrestadorPresupuestoRepositorio,
    private val consultasRepo: ConsultasPrestadorRepositorio
) : ViewModel() {

    // --- SECTOR: CATÁLOGOS (Sugerencias) ---
    val articleCatalog = repositorio.obtenerCatalogoArticulos()
        .map { it?.valor ?: "" }
        .map { CatalogWrapper(itemsJson = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogWrapper())

    val serviceCatalog = repositorio.obtenerCatalogoServicios()
        .map { it?.valor ?: "" }
        .map { CatalogWrapper(serviciosJson = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogWrapper())

    val presupuestos: StateFlow<List<PresupuestoEntity>> = repositorio.todosLosPresupuestos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [ELITE]: Bandeja de Documentos Enviados (Mesa).
     */
    val presupuestosEnviados: StateFlow<List<PresupuestoFinalEntity>> = repositorio.todosLosPresupuestosFinales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val concursos: StateFlow<List<ConcursoPublicoEntity>> = repositorio.todasLasLicitaciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🔥 [SUPREME]: Estadísticas de Rentabilidad (Be-Profit)
    val estadisticas: StateFlow<CalculadoraPresupuesto.ResultadoCalculo> = presupuestosEnviados.map { lista ->
        val aceptados = lista.filter { it.estado == EstadoPresupuesto.ACEPTADO }
        CalculadoraPresupuesto.ResultadoCalculo(
            totalGeneral = aceptados.sumOf { it.totalGeneral },
            costoTotal = aceptados.sumOf { it.totalCostoGeral },
            gananciaEstimada = aceptados.sumOf { it.totalGeneral - it.totalCostoGeral }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculadoraPresupuesto.ResultadoCalculo())

    private val _selectedBudgetId = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedBudgetFull: StateFlow<PresupuestoConItems?> = _selectedBudgetId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repositorio.obtenerPresupuestoCocinaConItems(id).map { cocina ->
            cocina?.let { c ->
                PresupuestoConItems(
                    cabecera = PresupuestoFinalEntity(
                        idPresupuesto = c.cabecera.idPresupuesto,
                        idCliente = c.cabecera.idCliente,
                        idPrestador = c.cabecera.idPrestador,
                        idConcurso = c.cabecera.idConcurso,
                        idCategoria = c.cabecera.idCategoria,
                        nombrePrestador = c.cabecera.nombrePrestador,
                        nombreEmpresaPrestador = c.cabecera.nombreEmpresaPrestador,
                        urlFotoPrestador = c.cabecera.urlFotoPrestador,
                        urlMiniatura = c.cabecera.urlMiniatura,
                        numeroPresupuesto = c.cabecera.numeroPresupuesto,
                        tituloTrabajo = c.cabecera.tituloTrabajo,
                        subtotal = c.cabecera.subtotal,
                        totalImpuestos = c.cabecera.montoImpuestos,
                        totalDescuentos = c.cabecera.montoDescuento,
                        totalGeneral = c.cabecera.totalGeneral,
                        diasValidez = c.cabecera.diasValidez,
                        notas = c.cabecera.notas,
                        metodosPago = c.cabecera.metodosPago,
                        tipo = c.cabecera.tipo,
                        estado = EstadoPresupuesto.valueOf(c.cabecera.estado.name),
                        marcaTiempo = c.cabecera.marcaTiempo
                    ),
                    lineas = c.items.map { p ->
                        ProductoFinalEntity(
                            idPresupuesto = c.cabecera.idPresupuesto,
                            idOriginal = p.id,
                            nombreCopiado = p.nombre,
                            descripcionCopiada = p.descripcion,
                            cantidad = p.cantidad,
                            precioSnapshot = p.precioVenta,
                            porcentajeImpuesto = p.impuestoDefault,
                            porcentajeDescuento = p.descuentoDefault,
                            tipoItem = when(p.tipo) {
                                TipoProducto.PRODUCTO -> TipoProductoFinal.PRODUCTO
                                TipoProducto.SERVICIO -> TipoProductoFinal.SERVICIO
                                TipoProducto.GASTO -> TipoProductoFinal.GASTO
                            }
                        )
                    },
                    finanzas = emptyList()
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun seleccionarPresupuestoParaDetalle(id: String?) {
        _selectedBudgetId.value = id
    }

    private val _idsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val idsSeleccionados: StateFlow<Set<String>> = _idsSeleccionados.asStateFlow()

    val esModoSeleccion: StateFlow<Boolean> = _idsSeleccionados.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _detallesConcurso = MutableStateFlow<ConcursoPublicoEntity?>(null)
    val detallesConcurso: StateFlow<ConcursoPublicoEntity?> = _detallesConcurso.asStateFlow()

    val clientes: StateFlow<List<IdentidadUsuarioEntity>> = flowOf(emptyList<IdentidadUsuarioEntity>())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun alternarSeleccion(id: String) {
        _idsSeleccionados.update { actual ->
            if (actual.contains(id)) actual - id else actual + id
        }
    }

    fun eliminarSeleccionados() {
        _idsSeleccionados.value = emptySet()
    }

    fun limpiarSeleccion() {
        _idsSeleccionados.value = emptySet()
    }

    fun cargarDetallesConcurso(id: String) {
        viewModelScope.launch {
            repositorio.todasLasLicitaciones.collect { lista ->
                _detallesConcurso.value = lista.find { it.idConcurso == id }
            }
        }
    }

    fun insertarPresupuesto(presupuesto: PresupuestoEntity) {
        // Reservado para futura persistencia directa si es necesaria
    }

    fun enviarPresupuestoReal(presupuesto: PresupuestoEntity) {
        android.util.Log.d("MavElite", "[ENVIO_PRESUPUESTO_INICIADO]")
        viewModelScope.launch {
            try {
                repositorio.enviarPresupuesto(presupuesto)
            } catch (e: Exception) {
                _error.emit(e.message ?: "Error desconocido")
            }
        }
    }

    // --- SECTOR: GESTIÓN DE CATÁLOGO ---

    fun saveArticleToSuggestions(item: ArticuloPresupuesto) {
        viewModelScope.launch {
            val actual = articleCatalog.value.itemsJson
            val itemStr = "${item.codigo};${item.descripcion};${item.cantidad};${item.precioUnitario};${item.porcentajeImpuesto};${item.porcentajeDescuento}"
            val nuevo = if (actual.isBlank()) itemStr else "$actual|$itemStr"
            repositorio.guardarMetadata(AppMetadataEntity("article_catalog", nuevo))
        }
    }

    fun updateArticleInCatalog(oldDesc: String, item: ArticuloPresupuesto) {
        viewModelScope.launch {
            val lista = articleCatalog.value.itemsJson.split("|").toMutableList()
            val index = lista.indexOfFirst { it.split(";").getOrNull(1) == oldDesc }
            if (index != -1) {
                lista[index] = "${item.codigo};${item.descripcion};${item.cantidad};${item.precioUnitario};${item.porcentajeImpuesto};${item.porcentajeDescuento}"
                repositorio.guardarMetadata(AppMetadataEntity("article_catalog", lista.joinToString("|")))
            }
        }
    }

    fun deleteArticleFromCatalog(desc: String) {
        viewModelScope.launch {
            val lista = articleCatalog.value.itemsJson.split("|").filter { it.split(";").getOrNull(1) != desc }
            repositorio.guardarMetadata(AppMetadataEntity("article_catalog", lista.joinToString("|")))
        }
    }

    fun saveServiceToSuggestions(svc: ServicioPresupuesto) {
        viewModelScope.launch {
            val actual = serviceCatalog.value.serviciosJson
            val itemStr = "${svc.codigo};${svc.descripcion};${svc.precioUnitario}"
            val nuevo = if (actual.isBlank()) itemStr else "$actual|$itemStr"
            repositorio.guardarMetadata(AppMetadataEntity("service_catalog", nuevo))
        }
    }

    fun updateServiceInCatalog(oldDesc: String, svc: ServicioPresupuesto) {
        viewModelScope.launch {
            val lista = serviceCatalog.value.serviciosJson.split("|").toMutableList()
            val index = lista.indexOfFirst { it.split(";").getOrNull(1) == oldDesc }
            if (index != -1) {
                lista[index] = "${svc.codigo};${svc.descripcion};${svc.precioUnitario}"
                repositorio.guardarMetadata(AppMetadataEntity("service_catalog", lista.joinToString("|")))
            }
        }
    }

    fun deleteServiceFromCatalog(desc: String) {
        viewModelScope.launch {
            val lista = serviceCatalog.value.serviciosJson.split("|").filter { it.split(";").getOrNull(1) != desc }
            repositorio.guardarMetadata(AppMetadataEntity("service_catalog", lista.joinToString("|")))
        }
    }
}
