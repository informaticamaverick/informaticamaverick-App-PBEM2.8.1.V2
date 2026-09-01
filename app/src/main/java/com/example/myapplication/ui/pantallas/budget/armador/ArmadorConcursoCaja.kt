package com.example.myapplication.ui.pantallas.budget.armador

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.ui.componentes.sistema.lista.MoldeSheetEmergenteV3
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.viewmodel.budget.BorradorConcursoViewModel

/**
 * --- CAJA DEL ARMADOR DE CONCURSO (v2026.ELITE) ---
 * [PROPÓSITO]: Definir el contenedor visual (BottomSheet) para el Wizard de licitaciones.
 * [LEY #10]: Screen Anatomy. Caja > Lienzo.
 * [LEY #9]: Estándar Mav en Español.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmadorConcursoCaja(
    estaVisible: Boolean,
    alCerrar: () -> Unit,
    alTenerExito: () -> Unit,
    modeloVista: BorradorConcursoViewModel = hiltViewModel()
) {
    // 🔥 [ELITE]: Reclamo de Soberanía mediante Pila (Ley #12)
    val beConfig = remember { 
        com.example.myapplication.ui.componentes.be.modelos.ContextoHUD.WIZARD_CONCURSO.crearConfiguracionBase()
    }

    androidx.compose.runtime.DisposableEffect(estaVisible) {
        if (estaVisible) {
            modeloVista.idSoberania = beConfig.id // 🔥 [FIX]: Sincronizar ID
            modeloVista.configurarHUD(true)
            modeloVista.coordinador.navCoordinador.registrarPantalla(beConfig)
        }
        onDispose {
            if (estaVisible) {
                modeloVista.configurarHUD(false)
                modeloVista.coordinador.navCoordinador.removerPantalla(beConfig.id)
            }
        }
    }

    // 🔥 [ELITE]: Escucha de Éxito Soberano
    androidx.compose.runtime.LaunchedEffect(Unit) {
        modeloVista.finalizarExitosamente.collect {
            alTenerExito()
        }
    }

    MoldeSheetEmergenteV3(
        estaVisible = estaVisible,
        alCerrar = {
            modeloVista.configurarHUD(false) // Devolver HUD antes de cerrar
            alCerrar()
        },
        tituloCabecera = "Licitación Pública",
        iconoCabecera = "📝",
        colorBordeAcento = SharedPalette.AcidGreen,
        alturaMaximaFraccion = 0.95f
    ) {
        ArmadorConcursoLienzo(
            modeloVista = modeloVista
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewArmadorConcurso() {
    PBEMTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            MoldeSheetEmergenteV3(
                estaVisible = true,
                alCerrar = {},
                tituloCabecera = "Licitación Pública",
                iconoCabecera = "📝",
                colorBordeAcento = SharedPalette.AcidGreen,
                alturaMaximaFraccion = 0.95f
            ) {
                ArmadorConcursoLienzoContent(
                    pasoActual = 0,
                    titulo = "Proyecto de Ejemplo",
                    descripcion = "Descripción del proyecto",
                    idCategoria = "1",
                    nombreCategoria = "Plomería",
                    iconoCategoria = "🪠",
                    descripcionCategoria = "Servicios de agua y gas",
                    queryCategoria = "",
                    menuCategoriasExpandido = false,
                    urisDeImagenes = emptyList(),
                    exigeVisita = false,
                    exigeGarantia = false,
                    exigeMetodoPago = true,
                    exigeDocPrestador = false,
                    duracionDias = 7,
                    estadoCuenta = null,
                    todasLasCategorias = listOf(
                        CategoriaEntity(id = "1", nombre = "Plomería", icono = "🪠", idSuperCategoria = "HOGAR")
                    ),
                    idPerfilSeleccionado = null,
                    direccionSeleccionada = DireccionDominio(calle = "Av. Siempre Viva", numero = "742", localidad = "Springfield", codigoPostal = "4000"),
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
                    alActivarDireccionManual = { _ -> },
                    alCambiarCalle = { _ -> },
                    alCambiarNumero = { _ -> },
                    alCambiarCiudad = { _ -> },
                    alCambiarCp = { _ -> },
                    alAgregarImagen = { _ -> },
                    alEliminarImagen = { _ -> },
                    alCambiarExigeVisita = { _ -> },
                    alCambiarExigeGarantia = { _ -> },
                    alCambiarExigeMetodoPago = { _ -> },
                    alCambiarExigeDocPrestador = { _ -> },
                    alCambiarDuracion = { _ -> },
                    alPublicar = {}
                )
            }
        }
    }
}
