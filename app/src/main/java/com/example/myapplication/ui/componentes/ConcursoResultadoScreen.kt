package com.example.myapplication.ui.componentes

import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import androidx.compose.ui.platform.LocalConfiguration
import com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers
import com.example.myapplication.uishared.ui.components.TarjetaPresupuesto
import com.example.myapplication.ui.componentes.sistema.AppTacticalButton
import com.example.myapplication.ui.estilos.ClienteTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

private val appBlue = Color(0xFF2197F5)

@Composable
fun ResultadoConcursoOverlay(
    concursoSeleccionado: ConcursoPublicoEntity?,
    onClose: () -> Unit,
    beBrainActionEvent: SharedFlow<String>,
    getPresupuestosParaConcurso: (String) -> StateFlow<List<PresupuestoFinalEntity>>,
    onPresupuestoClick: (PresupuestoFinalEntity) -> Unit,
    onChatClick: (String, String?) -> Unit,
    onAvatarClick: (PresupuestoFinalEntity) -> Unit,
    estaMultiseleccionActiva: Boolean,
    idsSeleccionados: Set<String>,
    alAlternarSeleccionItem: (String) -> Unit,
    alAlternarMultiseleccion: () -> Unit,
    alHacerClickAnaliticas: (ConcursoPublicoEntity, List<PresupuestoFinalEntity>) -> Unit,
    alEliminarPresupuestos: (Set<String>) -> Unit,
    alMarcarComoLeidosMulti: (Set<String>) -> Unit = {},
    alEstablecerContexto: (ContextoHUD) -> Unit,
    mostrarDialogoConfirmarEliminar: (String, () -> Unit) -> Unit,
    categorias: List<CategoriaEntity> = emptyList()
) {
    var ultimoConcursoSeleccionadoAlSalir by remember { mutableStateOf<ConcursoPublicoEntity?>(null) }
    val estadoGrillaPresupuestos = rememberLazyListState()

    if (concursoSeleccionado != null) {
        ultimoConcursoSeleccionadoAlSalir = concursoSeleccionado
    }

    LaunchedEffect(concursoSeleccionado) {
        if (concursoSeleccionado != null) {
            alEstablecerContexto(ContextoHUD.CONCURSOS)
        }
    }

    ultimoConcursoSeleccionadoAlSalir?.let { concurso ->
        val flujoPresupuestosConcurso = remember(concurso.idConcurso) { getPresupuestosParaConcurso(concurso.idConcurso) }
        val presupuestos by flujoPresupuestosConcurso.collectAsStateWithLifecycle(emptyList())

        val localizacionActual = LocalConfiguration.current.locales[0]
        LaunchedEffect(concurso, presupuestos, idsSeleccionados, localizacionActual) {
            beBrainActionEvent.collect { idAccion: String ->
                when (idAccion) {
                    "compare_all" -> {
                        alHacerClickAnaliticas(concurso, presupuestos.sortedBy { it.nombrePrestador.lowercase(localizacionActual) })
                    }
                    "compare_selected" -> {
                        val presupuestosSeleccionados = presupuestos.filter { it.idPresupuesto in idsSeleccionados }
                        if (presupuestosSeleccionados.isNotEmpty()) {
                            alHacerClickAnaliticas(concurso, presupuestosSeleccionados.sortedBy { it.nombrePrestador.lowercase(localizacionActual) })
                        }
                    }
                    "delete_selected" -> {
                        mostrarDialogoConfirmarEliminar("¿Deseas eliminar las ofertas seleccionadas de este concurso?") {
                            alEliminarPresupuestos(idsSeleccionados)
                        }
                    }
                    "mark_as_read" -> {
                        if (idsSeleccionados.isNotEmpty()) {
                            alMarcarComoLeidosMulti(idsSeleccionados)
                        }
                    }
                }
            }
        }

        SheetEmergenteVertical(
            isVisible = concursoSeleccionado != null,
            onClose = onClose,
            title = concurso.titulo,
            helperText = "Licitación Táctica",
            emoji = "📩",
            topOffset = 150.dp,
            showActions = true,
            isScrollable = false,
            onAnimationFinished = {
                if (concursoSeleccionado == null) {
                    ultimoConcursoSeleccionadoAlSalir = null
                }
            },
            actions = {
                AppTacticalButton(
                    isActive = false,
                    accentColor = Color.Yellow,
                    onClick = { alHacerClickAnaliticas(concurso, presupuestos) }
                ) { Text("📊", fontSize = 16.sp) }
            }
        ) {
            val local = LocalConfiguration.current.locales[0]
            val sdf = remember(local) { SimpleDateFormat("dd/MM/yyyy", local) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    com.example.myapplication.ui.componentes.DateInfoRowEmoji("📅", "INICIO", sdf.format(Date(concurso.fechaInicio)))
                    com.example.myapplication.ui.componentes.DateInfoRowEmoji("🏁", "CIERRE", sdf.format(Date(concurso.fechaFin)))
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (concurso.exigeVisita) BadgeRequisito("Visita", Icons.Default.Build)
                    if (concurso.exigeGarantia) BadgeRequisito("Garantía", Icons.Default.Build)
                    if (concurso.exigeDocPrestador) BadgeRequisito("Docs", Icons.Default.Build)
                }
            }

            val estadosExpandidos = remember { mutableStateMapOf<String, Boolean>() }

            // 🔥 [ELITE v2026]: Implementación de Lista Pura (Sin Molde viejo)
            androidx.compose.foundation.lazy.LazyColumn(
                state = estadoGrillaPresupuestos,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                elementosGrillaPresupuestos(
                    concurso = concurso,
                    presupuestos = presupuestos,
                    categorias = categorias,
                    estaMultiseleccionActiva = estaMultiseleccionActiva,
                    idsSeleccionados = idsSeleccionados,
                    alAlternarSeleccionItem = alAlternarSeleccionItem,
                    onPresupuestoClick = onPresupuestoClick,
                    onChatClick = onChatClick,
                    alAlternarMultiseleccion = alAlternarMultiseleccion,
                    onAvatarClick = onAvatarClick,
                    estadosExpandidos = estadosExpandidos,
                    locale = local
                )
            }
        }
    }
}

@Composable
fun BadgeRequisito(etiqueta: String, icono: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, null, tint = Color.Gray, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(etiqueta, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun LazyListScope.elementosGrillaPresupuestos(
    concurso: ConcursoPublicoEntity,
    presupuestos: List<PresupuestoFinalEntity>,
    categorias: List<CategoriaEntity> = emptyList(),
    estaMultiseleccionActiva: Boolean = false,
    idsSeleccionados: Set<String> = emptySet(),
    alAlternarSeleccionItem: (String) -> Unit = {},
    onPresupuestoClick: (PresupuestoFinalEntity) -> Unit,
    onChatClick: (String, String?) -> Unit,
    alAlternarMultiseleccion: () -> Unit,
    onAvatarClick: (PresupuestoFinalEntity) -> Unit,
    estadosExpandidos: MutableMap<String, Boolean>,
    locale: Locale
) {
    if (presupuestos.isEmpty()) {
        item {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("Sin ofertas registradas", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val formateadorFecha = SimpleDateFormat("dd MMMM yyyy", locale)
    
    val presupuestosAgrupados = presupuestos.groupBy {
        formateadorFecha.format(Date(it.marcaTiempo))
    }.mapValues { entry ->
        entry.value.sortedWith(compareBy<PresupuestoFinalEntity> { it.leido }.thenByDescending { it.marcaTiempo })
    }.toList().sortedByDescending { it.second.first().marcaTiempo }

    presupuestosAgrupados.forEach { (textoFecha, presupuestosEnFecha) ->
        val estaExpandido = estadosExpandidos[textoFecha] ?: true
        
        item(key = "header_$textoFecha") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { 
                        estadosExpandidos[textoFecha] = !estaExpandido 
                    }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DivisorPremium(modifier = Modifier.weight(1f))
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = textoFecha.uppercase(),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = if (estaExpandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                DivisorPremium(modifier = Modifier.weight(1f))
            }
        }

        if (estaExpandido) {
            val presupuestosTroceados = presupuestosEnFecha.chunked(2) // 🔥 [ELITE]: 2 columnas para impacto A4
            items(presupuestosTroceados.size, key = { "row_${textoFecha}_$it" }) { indiceFila ->
                val presupuestosFila = presupuestosTroceados[indiceFila]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    presupuestosFila.forEach { p ->
                        val metaCat = categorias.find { it.id == p.idCategoria }
                        val iconoCat = metaCat?.icono ?: "📋"
                        val nombreCat = metaCat?.nombre ?: "Servicio"
                        
                        val resumen = PresupuestoMappers.aResumenDominio(
                            entidad = p,
                            nombreCat = nombreCat,
                            iconoCat = iconoCat
                        )
                        
                        TarjetaPresupuesto(
                            modifier = Modifier.weight(1f),
                            presupuesto = resumen,
                            estaSeleccionado = idsSeleccionados.contains(p.idPresupuesto),
                            esMultiseleccionActiva = estaMultiseleccionActiva,
                            alHacerClick = { onPresupuestoClick(p) },
                            alHacerClickChat = { onChatClick(p.idPrestador, p.idCategoria ?: concurso.idCategoria) },
                            alHacerLongClick = {
                                if (!estaMultiseleccionActiva) {
                                    alAlternarMultiseleccion()
                                }
                                alAlternarSeleccionItem(p.idPresupuesto)
                            }
                        )
                    }
                    repeat(2 - presupuestosFila.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DivisorPremium (modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PrevisualizacionResultadoConcursoOverlay() {
    val concursoPrueba = ConcursoPublicoEntity(
        idConcurso = "concurso_123",
        titulo = "Instalación de Aire Acondicionado",
        idCliente = "cliente_abc",
        descripcion = "Se requiere instalar un aire acondicionado split de 3000 frigorías.",
        idCategoria = "CLIMA",
        marcaTiempo = System.currentTimeMillis()
    )

    val presupuestosPrueba = listOf(
        PresupuestoFinalEntity(
            idPresupuesto = "b1",
            idCliente = "cliente_abc",
            idPrestador = "p1",
            idConcurso = "concurso_123",
            nombrePrestador = "Juan Clima",
            totalGeneral = 4500.0,
            leido = false,
            marcaTiempo = System.currentTimeMillis()
        ),
        PresupuestoFinalEntity(
            idPresupuesto = "b2",
            idCliente = "cliente_abc",
            idPrestador = "p2",
            idConcurso = "concurso_123",
            nombrePrestador = "Marta Frío",
            totalGeneral = 4200.0,
            leido = true,
            marcaTiempo = System.currentTimeMillis() - 86400000
        )
    )

    ClienteTheme(darkTheme = true) {
        ResultadoConcursoOverlay(
            concursoSeleccionado = concursoPrueba,
            onClose = {},
            beBrainActionEvent = MutableSharedFlow(),
            getPresupuestosParaConcurso = { _ -> MutableStateFlow(presupuestosPrueba) },
            onPresupuestoClick = {},
            onChatClick = { _, _ -> },
            onAvatarClick = {},
            estaMultiseleccionActiva = false,
            idsSeleccionados = emptySet(),
            alAlternarSeleccionItem = {},
            alAlternarMultiseleccion = {},
            alHacerClickAnaliticas = { _, _ -> },
            alEliminarPresupuestos = {},
            alMarcarComoLeidosMulti = {},
            alEstablecerContexto = {},
            mostrarDialogoConfirmarEliminar = { _, _ -> }
        )
    }
}
