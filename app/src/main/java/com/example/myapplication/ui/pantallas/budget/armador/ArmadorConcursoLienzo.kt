package com.example.myapplication.ui.pantallas.budget.armador

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.viewmodel.budget.BorradorConcursoViewModel

/**
 * --- LIENZO DEL ARMADOR DE CONCURSO (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar el Wizard de creación de licitaciones.
 * [LEY #12]: Soberanía del Contenido (Pilot Mode).
 * [LEY #9]: Estándar Mav en Español.
 */
@Composable
fun ArmadorConcursoLienzo(
    modeloVista: BorradorConcursoViewModel
) {
    val pasoActual by modeloVista.pasoActual.collectAsStateWithLifecycle()
    val titulo by modeloVista.titulo.collectAsStateWithLifecycle()
    val descripcion by modeloVista.descripcion.collectAsStateWithLifecycle()
    val idCategoria by modeloVista.idCategoria.collectAsStateWithLifecycle()
    val nombreCategoria by modeloVista.nombreCategoria.collectAsStateWithLifecycle()
    val iconoCategoria by modeloVista.iconoCategoria.collectAsStateWithLifecycle()
    val descripcionCategoria by modeloVista.descripcionCategoria.collectAsStateWithLifecycle()
    
    val queryCategoria by modeloVista.queryCategoria.collectAsStateWithLifecycle()
    val menuCategoriasExpandido by modeloVista.menuCategoriasExpandido.collectAsStateWithLifecycle()

    val urisDeImagenes by modeloVista.urisDeImagenes.collectAsStateWithLifecycle()
    val exigeVisita by modeloVista.exigeVisita.collectAsStateWithLifecycle()
    val exigeGarantia by modeloVista.exigeGarantia.collectAsStateWithLifecycle()
    val exigeMetodoPago by modeloVista.exigeMetodoPago.collectAsStateWithLifecycle()
    val exigeDocPrestador by modeloVista.exigeDocPrestador.collectAsStateWithLifecycle()
    val duracionDias by modeloVista.duracionDias.collectAsStateWithLifecycle()
    
    val estadoCuenta by modeloVista.estadoCuenta.collectAsStateWithLifecycle()
    val todasLasCategorias by modeloVista.todasLasCategorias.collectAsStateWithLifecycle()
    
    val idPerfilSeleccionado by modeloVista.idPerfilSeleccionado.collectAsStateWithLifecycle()
    val direccionSeleccionada by modeloVista.direccionSeleccionada.collectAsStateWithLifecycle()
    val esDireccionManual by modeloVista.esDireccionManual.collectAsStateWithLifecycle()
    
    val calleManual by modeloVista.calleManual.collectAsStateWithLifecycle()
    val numeroManual by modeloVista.numeroManual.collectAsStateWithLifecycle()
    val ciudadManual by modeloVista.ciudadManual.collectAsStateWithLifecycle()
    val cpManual by modeloVista.cpManual.collectAsStateWithLifecycle()

    val mostrarMenuPerfil by modeloVista.mostrarMenuPerfil.collectAsStateWithLifecycle()
    val mostrarMenuUbicacion by modeloVista.mostrarMenuUbicacion.collectAsStateWithLifecycle()
    val estaGpsActivo by modeloVista.estaGpsActivo.collectAsStateWithLifecycle()

    ArmadorConcursoLienzoContent(
        pasoActual = pasoActual,
        titulo = titulo,
        descripcion = descripcion,
        idCategoria = idCategoria,
        nombreCategoria = nombreCategoria,
        iconoCategoria = iconoCategoria,
        descripcionCategoria = descripcionCategoria,
        queryCategoria = queryCategoria,
        menuCategoriasExpandido = menuCategoriasExpandido,
        urisDeImagenes = urisDeImagenes,
        exigeVisita = exigeVisita,
        exigeGarantia = exigeGarantia,
        exigeMetodoPago = exigeMetodoPago,
        exigeDocPrestador = exigeDocPrestador,
        duracionDias = duracionDias,
        estadoCuenta = estadoCuenta,
        todasLasCategorias = todasLasCategorias,
        idPerfilSeleccionado = idPerfilSeleccionado,
        direccionSeleccionada = direccionSeleccionada,
        esDireccionManual = esDireccionManual,
        calleManual = calleManual,
        numeroManual = numeroManual,
        ciudadManual = ciudadManual,
        cpManual = cpManual,
        mostrarMenuPerfil = mostrarMenuPerfil,
        mostrarMenuUbicacion = mostrarMenuUbicacion,
        estaGpsActivo = estaGpsActivo,
        alCambiarPaso = { modeloVista.actualizarPaso(it) },
        alCambiarTitulo = { modeloVista.actualizarTitulo(it) },
        alCambiarDescripcion = { modeloVista.actualizarDescripcion(it) },
        alSeleccionarCategoria = { modeloVista.actualizarCategoria(it) },
        alCambiarQueryCategoria = { modeloVista.actualizarQueryCategoria(it) },
        alAlternarMenuCategorias = { modeloVista.alternarMenuCategorias(it) },
        alCambiarPerfil = { idEmp, idSuc -> modeloVista.seleccionarPerfil(idEmp, idSuc) },
        alAlternarMenuPerfil = { modeloVista.alternarMenuPerfil(it) },
        alSeleccionarDireccion = { modeloVista.seleccionarDireccion(it) },
        alAlternarMenuUbicacion = { modeloVista.alternarMenuUbicacion(it) },
        alAlternarGps = { /* Implementar GPS si es necesario */ },
        alActivarDireccionManual = { modeloVista.activarDireccionManual(it) },
        alCambiarCalle = { modeloVista.actualizarCalleManual(it) },
        alCambiarNumero = { modeloVista.actualizarNumeroManual(it) },
        alCambiarCiudad = { modeloVista.actualizarCiudadManual(it) },
        alCambiarCp = { modeloVista.actualizarCpManual(it) },
        alAgregarImagen = { modeloVista.agregarImagen(it) },
        alEliminarImagen = { modeloVista.eliminarImagen(it) },
        alCambiarExigeVisita = { modeloVista.actualizarExigeVisita(it) },
        alCambiarExigeGarantia = { modeloVista.actualizarExigeGarantia(it) },
        alCambiarExigeMetodoPago = { modeloVista.actualizarExigeMetodoPago(it) },
        alCambiarExigeDocPrestador = { modeloVista.actualizarExigeDocPrestador(it) },
        alCambiarDuracion = { modeloVista.actualizarDuracion(it) },
        alPublicar = { modeloVista.publicarLicitacionConPublicidad() }
    )
}

@Composable
fun ArmadorConcursoLienzoContent(
    pasoActual: Int,
    titulo: String,
    descripcion: String,
    idCategoria: String,
    nombreCategoria: String,
    iconoCategoria: String,
    descripcionCategoria: String,
    queryCategoria: String,
    menuCategoriasExpandido: Boolean,
    urisDeImagenes: List<String>,
    exigeVisita: Boolean,
    exigeGarantia: Boolean,
    exigeMetodoPago: Boolean,
    exigeDocPrestador: Boolean,
    duracionDias: Int,
    estadoCuenta: CuentaMaestroUsuario?,
    todasLasCategorias: List<CategoriaEntity>,
    idPerfilSeleccionado: String?,
    direccionSeleccionada: DireccionDominio?,
    esDireccionManual: Boolean,
    calleManual: String,
    numeroManual: String,
    ciudadManual: String,
    cpManual: String,
    mostrarMenuPerfil: Boolean,
    mostrarMenuUbicacion: Boolean,
    estaGpsActivo: Boolean,
    alCambiarPaso: (Int) -> Unit,
    alCambiarTitulo: (String) -> Unit,
    alCambiarDescripcion: (String) -> Unit,
    alSeleccionarCategoria: (CategoriaEntity) -> Unit,
    alCambiarQueryCategoria: (String) -> Unit,
    alAlternarMenuCategorias: (Boolean) -> Unit,
    alCambiarPerfil: (String?, String?) -> Unit,
    alAlternarMenuPerfil: (Boolean) -> Unit,
    alSeleccionarDireccion: (DireccionDominio) -> Unit,
    alAlternarMenuUbicacion: (Boolean) -> Unit,
    alAlternarGps: () -> Unit,
    alActivarDireccionManual: (Boolean) -> Unit,
    alCambiarCalle: (String) -> Unit,
    alCambiarNumero: (String) -> Unit,
    alCambiarCiudad: (String) -> Unit,
    alCambiarCp: (String) -> Unit,
    alAgregarImagen: (String) -> Unit,
    alEliminarImagen: (String) -> Unit,
    alCambiarExigeVisita: (Boolean) -> Unit,
    alCambiarExigeGarantia: (Boolean) -> Unit,
    alCambiarExigeMetodoPago: (Boolean) -> Unit,
    alCambiarExigeDocPrestador: (Boolean) -> Unit,
    alCambiarDuracion: (Int) -> Unit,
    alPublicar: () -> Unit
) {
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { alAgregarImagen(it.toString()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Encabezado de Pasos ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ItemIndicadorPaso(titulo = "1. Identidad", activo = pasoActual == 0, completado = pasoActual > 0)
                    ItemIndicadorPaso(titulo = "2. Detalle", activo = pasoActual == 1, completado = pasoActual > 1)
                    ItemIndicadorPaso(titulo = "3. Cláusulas", activo = pasoActual == 2, completado = pasoActual > 2)
                }

                LinearProgressIndicator(
                    progress = { (pasoActual + 1) / 3f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = SharedPalette.AcidGreen,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // --- Contenido Animado ---
            AnimatedContent(
                targetState = pasoActual,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "AnimacionPasos"
            ) { paso ->
                when (paso) {
                    0 -> SeccionSolicitante(
                        estadoCuenta = estadoCuenta,
                        todasLasCategorias = todasLasCategorias,
                        idCategoriaSeleccionada = idCategoria,
                        nombreCategoria = nombreCategoria,
                        iconoCategoria = iconoCategoria,
                        descripcionCategoria = descripcionCategoria,
                        alSeleccionarCategoria = alSeleccionarCategoria,
                        queryCategoria = queryCategoria,
                        alCambiarQueryCategoria = alCambiarQueryCategoria,
                        menuCategoriasExpandido = menuCategoriasExpandido,
                        alAlternarMenuCategorias = alAlternarMenuCategorias,
                        idPerfilSeleccionado = idPerfilSeleccionado,
                        alCambiarPerfil = alCambiarPerfil,
                        mostrarMenuPerfil = mostrarMenuPerfil,
                        alAlternarMenuPerfil = alAlternarMenuPerfil,
                        direccionSeleccionada = direccionSeleccionada,
                        alSeleccionarDireccion = alSeleccionarDireccion,
                        mostrarMenuUbicacion = mostrarMenuUbicacion,
                        alAlternarMenuUbicacion = alAlternarMenuUbicacion,
                        estaGpsActivo = estaGpsActivo,
                        alAlternarGps = alAlternarGps,
                        esDireccionManual = esDireccionManual,
                        alActivarDireccionManual = alActivarDireccionManual,
                        calleManual = calleManual,
                        alCambiarCalle = alCambiarCalle,
                        numeroManual = numeroManual,
                        alCambiarNumero = alCambiarNumero,
                        ciudadManual = ciudadManual,
                        alCambiarCiudad = alCambiarCiudad,
                        cpManual = cpManual,
                        alCambiarCp = alCambiarCp
                    )
                    1 -> SeccionDetalleLicitacion(
                        titulo = titulo,
                        alCambiarTitulo = alCambiarTitulo,
                        descripcion = descripcion,
                        alCambiarDescripcion = alCambiarDescripcion,
                        urisDeImagenes = urisDeImagenes,
                        alAgregarImagen = { lanzador.launch("image/*") },
                        alEliminarImagen = alEliminarImagen
                    )
                    2 -> SeccionRequisitosYPlazos(
                        exigeVisita = exigeVisita,
                        alCambiarExigeVisita = alCambiarExigeVisita,
                        exigeGarantia = exigeGarantia,
                        alCambiarExigeGarantia = alCambiarExigeGarantia,
                        exigeMetodoPago = exigeMetodoPago,
                        alCambiarExigeMetodoPago = alCambiarExigeMetodoPago,
                        exigeDocPrestador = exigeDocPrestador,
                        alCambiarExigeDocPrestador = alCambiarExigeDocPrestador,
                        duracionDias = duracionDias,
                        alCambiarDuracion = alCambiarDuracion
                    )
                }
            }
        }

        // --- SOBERANÍA TÁCTICA: HUD (BE PILOTO) ---
        // Se ha eliminado la botonera de la pantalla.
        // Be orquesta la navegación mediante el contrato HUD.
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewArmadorConcursoLienzo() {
    PBEMTheme {
        ArmadorConcursoLienzoContent(
            pasoActual = 0,
            titulo = "Proyecto de Ejemplo",
            descripcion = "Descripción del proyecto",
            idCategoria = "",
            nombreCategoria = "",
            iconoCategoria = "📋",
            descripcionCategoria = "",
            queryCategoria = "",
            menuCategoriasExpandido = false,
            urisDeImagenes = emptyList(),
            exigeVisita = false,
            exigeGarantia = false,
            exigeMetodoPago = true,
            exigeDocPrestador = false,
            duracionDias = 7,
            estadoCuenta = null,
            todasLasCategorias = emptyList(),
            idPerfilSeleccionado = null,
            direccionSeleccionada = null,
            esDireccionManual = false,
            calleManual = "",
            numeroManual = "",
            ciudadManual = "",
            cpManual = "",
            mostrarMenuPerfil = false,
            mostrarMenuUbicacion = false,
            estaGpsActivo = false,
            alCambiarPaso = {},
            alCambiarTitulo = {},
            alCambiarDescripcion = {},
            alSeleccionarCategoria = { },
            alCambiarQueryCategoria = {},
            alAlternarMenuCategorias = {},
            alCambiarPerfil = { _, _ -> },
            alAlternarMenuPerfil = {},
            alSeleccionarDireccion = {},
            alAlternarMenuUbicacion = {},
            alAlternarGps = {},
            alActivarDireccionManual = {},
            alCambiarCalle = {},
            alCambiarNumero = {},
            alCambiarCiudad = {},
            alCambiarCp = {},
            alAgregarImagen = {},
            alEliminarImagen = {},
            alCambiarExigeVisita = {},
            alCambiarExigeGarantia = {},
            alCambiarExigeMetodoPago = {},
            alCambiarExigeDocPrestador = {},
            alCambiarDuracion = {},
            alPublicar = {}
        )
    }
}
