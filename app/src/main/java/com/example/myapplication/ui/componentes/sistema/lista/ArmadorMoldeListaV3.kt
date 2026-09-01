package com.example.myapplication.ui.componentes.sistema.lista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.ui.componentes.be.modelos.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/**
 * --- ARMADOR MAESTRO DE MOLDES DE LISTA V3 (v2026.ELITE) ---
 * 
 * [PROPÓSITO]: Orquestar el ensamblaje de listas en diferentes contextos del ecosistema.
 * [FUNCIONAMIENTO]: Especializa la anatomía de la lista según sea Pantalla Completa o Modo Búsqueda.
 * [LEY #10]: Garantiza la simetría visual y la física del colapso en todo el proyecto.
 */

// ==================================================================================
// --- MODALIDAD 1: PANTALLA COMPLETA (Home, Notificaciones) ---
// ==================================================================================

/**
 * ArmadorListaPantallaCompleta: Ensamblador para pantallas raíz.
 * Maneja el colapso de cabecera y el soporte multi-identidad (Pager).
 */
@Composable
fun ArmadorListaPantallaCompleta(
    modifier: Modifier = Modifier,
    titulo: String,
    subtitulo: String? = null,
    icono: String? = null,
    cantidadItems: Int? = null,
    perfiles: List<PerfilIdentidadV3> = emptyList(),
    idPerfilInicial: String? = null,
    alSeleccionarPerfil: (PerfilIdentidadV3) -> Unit = {},
    colorAcento: Color = SharedPalette.ElectricCyan,
    alturaCabeceraManual: Dp? = null,
    estadoLista: LazyListState = rememberLazyListState(),
    colorContenedor: Color = SharedPalette.EliteSurface,
    slotCentral: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null, // 🔥 [NEW]
    menuPerfil: @Composable (ColumnScope.(idPerfil: String) -> Unit)? = null, // 🔥 [NEW]
    acciones: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null,
    filtros: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null,
    contenido: LazyListScope.(perfil: PerfilIdentidadV3?) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Configuración del Pager si hay perfiles
    val estadoPager = if (perfiles.isNotEmpty()) {
        val paginaInicial = remember(idPerfilInicial) {
            val indice = perfiles.indexOfFirst { it.id == idPerfilInicial }
            if (indice != -1) indice else 0
        }
        rememberPagerState(initialPage = paginaInicial, pageCount = { perfiles.size })
    } else null

    // Sincronización Pager -> Callback
    if (estadoPager != null) {
        LaunchedEffect(estadoPager.currentPage) {
            alSeleccionarPerfil(perfiles[estadoPager.currentPage])
        }
    }

    val alturaFinalCabecera = alturaCabeceraManual ?: if (filtros != null) 74.dp else 52.dp

    ContenedorBaseAppV3(
        modifier = modifier,
        colorFondo = colorContenedor
    ) {
        // --- 1. CAPA DE CONTENIDO (UDF - LEY #1) ---
        if (estadoPager != null) {
            HorizontalPager(
                state = estadoPager,
                modifier = Modifier.fillMaxSize().zIndex(0f),
                contentPadding = PaddingValues(top = alturaFinalCabecera + 8.dp, bottom = 80.dp),
                pageSpacing = 16.dp,
                verticalAlignment = Alignment.Top
            ) { pagina ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contenido(perfiles[pagina])
                }
            }
        } else {
            LazyColumn(
                state = estadoLista,
                modifier = Modifier.fillMaxSize().zIndex(0f),
                contentPadding = PaddingValues(top = alturaFinalCabecera + 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                contenido(null)
            }
        }

        // --- 2. CAPA DE SOMBRA (ESTÉTICA) ---
        SombraProyectadaV3(desplazamientoY = alturaFinalCabecera)

        // --- 3. CAPA DE CABECERA (FÍSICA DEL COLAPSO) ---
        CabeceraAppV3(
            modifier = Modifier.zIndex(2f),
            titulo = titulo,
            subtitulo = subtitulo,
            icono = icono,
            cantidadItems = cantidadItems,
            perfiles = perfiles,
            idPerfilSeleccionado = estadoPager?.let { perfiles[it.currentPage].id },
            alSeleccionarPerfil = { perfil ->
                scope.launch { estadoPager?.animateScrollToPage(perfiles.indexOf(perfil)) }
            },
            alturaCabecera = alturaFinalCabecera,
            colorAcento = colorAcento,
            slotCentral = slotCentral,
            menuPerfil = menuPerfil,
            accionesDerecha = acciones,
            filtros = filtros
        )
    }
}

/**
 * ArmadorGridPantallaCompleta: Ensamblador para pantallas raíz en formato cuadrícula.
 */
@Composable
fun ArmadorGridPantallaCompleta(
    modifier: Modifier = Modifier,
    titulo: String,
    subtitulo: String? = null,
    icono: String? = null,
    cantidadItems: Int? = null,
    perfiles: List<PerfilIdentidadV3> = emptyList(),
    idPerfilSeleccionado: String? = null,
    alSeleccionarPerfil: (PerfilIdentidadV3) -> Unit = {},
    colorAcento: Color = SharedPalette.ElectricCyan,
    columnas: GridCells = GridCells.Fixed(2),
    alturaCabeceraManual: Dp? = null,
    estadoGrid: LazyGridState = rememberLazyGridState(),
    colorContenedor: Color = SharedPalette.EliteSurface,
    slotCentral: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null, // 🔥 [NEW]
    menuPerfil: @Composable (ColumnScope.(idPerfil: String) -> Unit)? = null, // 🔥 [NEW]
    acciones: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null,
    accionesIzquierda: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null, // 🔥 [NEW]
    filtros: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null,
    contenido: LazyGridScope.() -> Unit
) {
    val alturaFinalCabecera = alturaCabeceraManual ?: if (filtros != null) 74.dp else 52.dp

    ContenedorBaseAppV3(
        modifier = modifier,
        colorFondo = colorContenedor
    ) {
        LazyVerticalGrid(
            columns = columnas,
            state = estadoGrid,
            modifier = Modifier.fillMaxSize().zIndex(0f),
            contentPadding = PaddingValues(
                top = alturaFinalCabecera + 12.dp,
                bottom = 80.dp,
                start = 12.dp,
                end = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            contenido()
        }

        SombraProyectadaV3(desplazamientoY = alturaFinalCabecera)

        CabeceraAppV3(
            modifier = Modifier.zIndex(2f),
            titulo = titulo,
            subtitulo = subtitulo,
            icono = icono,
            cantidadItems = cantidadItems,
            perfiles = perfiles,
            idPerfilSeleccionado = idPerfilSeleccionado,
            alSeleccionarPerfil = alSeleccionarPerfil,
            alturaCabecera = alturaFinalCabecera,
            colorAcento = colorAcento,
            slotCentral = slotCentral,
            menuPerfil = menuPerfil,
            accionesDerecha = acciones,
            accionesIzquierda = accionesIzquierda, // 🔥 [NEW]
            filtros = filtros
        )
    }
}

// ==================================================================================
// --- MODALIDAD 2: MODO BÚSQUEDA ASISTENTE (Presupuestos, Mercado) ---
// ==================================================================================

/**
 * ArmadorListaModoBusqueda: Ensamblador especializado para integración con el HUD de Be.
 * Utiliza MoldeSheetEmergenteV3 para medir el espacio perfecto debajo de la cabecera de Be.
 */
@Composable
fun ArmadorListaModoBusqueda(
    estaVisible: Boolean,
    alCerrar: () -> Unit,
    titulo: String,
    subtitulo: String? = null,
    icono: String? = null,
    cantidadItems: Int? = null,
    pistaBusqueda: String? = null, // 🔥 [NEW]
    colorAcento: Color = SharedPalette.ElectricCyan,
    menuPerfil: @Composable (ColumnScope.(idPerfil: String) -> Unit)? = null, // 🔥 [NEW]
    acciones: @Composable (RowScope.() -> Unit)? = null,
    filtros: @Composable (() -> Unit)? = null,
    usaGrid: Boolean = false,
    columnasGrid: Int = 3,
    manejarHUD: Boolean = true, // 🔥 [NEW]: Indica si este componente debe gestionar su propio HUD Stack
    configuracionSoberana: ConfiguracionContextoBe? = null, // 🔥 [NEW v2026.SUPREME]
    beCerebroVm: BeCerebroViewModel = hiltViewModel(),
    contenido: LazyListScope.() -> Unit = {},
    contenidoGrid: LazyGridScope.() -> Unit = {}
) {
    // 🔥 [v2026.ELITE]: Soberanía HUD del Modo Búsqueda mediante Pila (Stack)
    val beConfig = remember(titulo, pistaBusqueda, configuracionSoberana) { 
        configuracionSoberana ?: ConfiguracionContextoBe(
            id = "busqueda_${titulo.lowercase().replace(" ", "_")}",
            primarias = emptyList(),
            sistema = listOf("teclado", "cerrar_todo"),
            pistaBusqueda = pistaBusqueda ?: "BUSCAR EN ${titulo.uppercase()}...",
            mostrarHerramientas = true,
            abrirTecladoEnBusqueda = false
        )
    }

    if (manejarHUD) {
        DisposableEffect(estaVisible) {
            if (estaVisible) {
                beCerebroVm.navCoordinador.registrarPantalla(beConfig)
                beCerebroVm.beBusquedaMotor.establecerEstaBusquedaActiva(true)
            }
            onDispose {
                if (estaVisible) {
                    beCerebroVm.navCoordinador.removerPantalla(beConfig.id)
                    beCerebroVm.beBusquedaMotor.establecerEstaBusquedaActiva(false) // 🔥 [FIX]: Limpiar modo búsqueda al cerrar
                    beCerebroVm.coordinador.limpiarModoTactico() // 🔥 [v2026.ELITE]: Garantizar higiene HUD al cerrar hoja
                }
            }
        }
    }

    MoldeSheetEmergenteV3(
        estaVisible = estaVisible,
        alCerrar = alCerrar,
        alturaMaximaFraccion = 1f, 
        colorBordeAcento = colorAcento,
        paddingSuperiorHUD = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 70.dp, 
        cabeceraSoberana = {
            CabeceraAppV3(
                titulo = titulo,
                subtitulo = subtitulo,
                icono = icono,
                cantidadItems = cantidadItems,
                alturaCabecera = if (filtros != null) 82.dp else 58.dp, // 🔥 [v2026.SUPREME]: Paridad casi exacta con Pantalla Completa
                colorAcento = colorAcento,
                menuPerfil = menuPerfil, 
                accionesDerecha = { 
                    if (acciones != null) acciones() 
                    else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            com.example.myapplication.ui.componentes.BotonAccionCircularElite(
                                estaAbierto = true,
                                alHacerClick = alCerrar,
                                tamanoBase = 26.dp 
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            TextCompacto(
                                text = "CERRAR",
                                color = SharedPalette.RogCrimson,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp)
                            )
                        }
                    }
                },
                filtros = filtros?.let { { it() } }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (usaGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnasGrid),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    contenidoGrid()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contenido()
                }
            }
        }
    }
}

// ==================================================================================
// --- PREVIEWS (LEY #10: MODO LECTURA) ---
// ==================================================================================

@Preview(name = "Armador Lista - Pantalla Completa", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewArmadorCompleto() {
    PBEMTheme {
        ArmadorListaPantallaCompleta(
            titulo = "BANDEJA DE ENTRADA",
            subtitulo = "Mensajería Elite",
            cantidadItems = 12
        ) {
            items(10) { 
                Box(Modifier.fillMaxWidth().height(80.dp).padding(8.dp).background(Color.White.copy(alpha = 0.05f))) 
            }
        }
    }
}
