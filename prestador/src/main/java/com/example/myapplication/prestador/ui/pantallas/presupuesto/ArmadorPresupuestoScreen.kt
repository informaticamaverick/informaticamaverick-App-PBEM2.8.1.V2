package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.viewmodel.presupuesto.BorradorPresupuestoViewModel

/**
 * --- PUNTO DE ENTRADA: ARMADOR DE PRESUPUESTOS (ELITE) ---
 * Conecta el ViewModel con el Layout orquestador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmadorPresupuestoScreen(
    idCliente: String,
    idPrestador: String,
    idConcurso: String? = null,
    onVolver: () -> Unit,
    onVerCatalogo: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    viewModel: BorradorPresupuestoViewModel = hiltViewModel()
) {
    val borrador by viewModel.estadoBorrador.collectAsStateWithLifecycle()
    val articulos by viewModel.articulos.collectAsStateWithLifecycle()
    val servicios by viewModel.servicios.collectAsStateWithLifecycle()
    val gastosVarios by viewModel.gastosVarios.collectAsStateWithLifecycle()
    val calculos by viewModel.calculos.collectAsStateWithLifecycle()
    val sugerencias by viewModel.sugerenciasProductos.collectAsStateWithLifecycle()
    val perfil by viewModel.perfilPrestador.collectAsStateWithLifecycle()
    val categoriasVigentes by viewModel.categoriasVigentes.collectAsStateWithLifecycle()
    val seccionActual by viewModel.seccionActual.collectAsStateWithLifecycle()
    val datosCliente by viewModel.datosCliente.collectAsStateWithLifecycle()
    val direccionesCliente by viewModel.direccionesCliente.collectAsStateWithLifecycle()
    val misIdentidades by viewModel.misIdentidades.collectAsStateWithLifecycle()
    val infoCategoria by viewModel.infoCategoriaActual.collectAsStateWithLifecycle()
    val validacionCatalogo by viewModel.resultadoValidacion.collectAsStateWithLifecycle()
    val mostrarAlertaDuplicado by viewModel.mostrarAlertaDuplicado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navegarAPaywall.collect {
            onNavigateToPaywall()
        }
    }

    LaunchedEffect(idCliente, idPrestador, idConcurso) {
        viewModel.inicializarBorrador(idCliente, idPrestador, idConcurso)
    }

    // 🔥 Delegamos toda la UI al Contenedor orquestador
    ArmadorPresupuestoMobileLayout(
        borrador = borrador,
        articulos = articulos,
        servicios = servicios,
        gastosVarios = gastosVarios,
        calculos = calculos,
        sugerencias = sugerencias,
        perfil = perfil,
        misIdentidades = misIdentidades,
        categoriasVigentes = categoriasVigentes,
        infoCategoria = infoCategoria,
        seccionActual = seccionActual,
        datosCliente = datosCliente,
        direccionesCliente = direccionesCliente,
        onCambiarSeccion = viewModel::cambiarSeccion,
        onVolver = onVolver,
        onVerCatalogo = onVerCatalogo,
        onActualizarBusqueda = viewModel::actualizarBusqueda,
        onAgregarArticulo = viewModel::intentarAgregarArticulo,
        onAgregarServicio = viewModel::intentarAgregarServicio,
        onAgregarGasto = viewModel::intentarAgregarGasto,
        onEliminarArticulo = viewModel::eliminarArticulo,
        onEliminarServicio = viewModel::eliminarServicio,
        onEliminarGasto = viewModel::eliminarGastoVario,
        onSeleccionarDireccion = viewModel::seleccionarDireccionCliente,
        onActualizarDireccionManual = viewModel::actualizarDireccionManual,
        onActualizarMetodosPago = viewModel::actualizarMetodosPago,
        onActualizarValidez = viewModel::actualizarValidez,
        onActualizarNotas = viewModel::actualizarNotas,
        onActualizarCategoria = viewModel::actualizarCategoria,
        onSeleccionarIdentidadEmisora = viewModel::seleccionarIdentidadEmisora,
        validacionCatalogo = validacionCatalogo,
        onValidarCatalogo = viewModel::validarSincronizacionCatalogo,
        onSincronizarCatalogo = viewModel::sincronizarCatalogo,
        onLimpiarValidacion = viewModel::limpiarValidacion,
        mostrarAlertaDuplicado = mostrarAlertaDuplicado,
        onOcultarAlertaDuplicado = viewModel::ocultarAlertaDuplicado,
        onEnviar = viewModel::enviarPresupuesto
    )
}
