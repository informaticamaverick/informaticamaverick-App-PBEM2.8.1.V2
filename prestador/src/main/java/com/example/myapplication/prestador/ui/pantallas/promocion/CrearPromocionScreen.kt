package com.example.myapplication.prestador.ui.pantallas.promocion

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.core.dominio.modelos.TipoCategoriaPromo
import com.example.myapplication.core.dominio.modelos.TipoPromocion
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.promocion.PrePromocionViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.ExperimentalStdlibApi

private const val TITULO_MAX = 60
private const val DESCRIPCION_MAX  = 300

/**
 * --- PANTALLA DE CREACIÓN DE PROMOCIONES (ELITE v2026.FINAL) ---
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun CrearPromocionScreen(
    promocionId: String? = null,
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onPublish: (Promocion) -> Unit = {},
    viewModel: PrePromocionViewModel = hiltViewModel(),
    identidadViewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
) {
    val tag = "CrearPromocionScreen"

    val stateDeep by identidadViewModel.state.collectAsStateWithLifecycle()
    val ecosistema = stateDeep.ecosistema
    
    val perfilesDisponibles = remember(ecosistema) {
        if (ecosistema == null) emptyList<PrestadorDominio>()
        else {
            val personal = ecosistema.prestador.perfil.copy(
                codigoPostal = ecosistema.prestador.direcciones.firstOrNull()?.codigoPostal
            )
            
            val empresas = ecosistema.empresas.flatMap { emp ->
                emp.sucursales.map { suc ->
                    PrestadorDominio(
                        id = suc.sucursal.id,
                        idPropietario = ecosistema.cuenta.id,
                        idEmpresa = emp.empresa.id,
                        tipo = com.example.myapplication.core.dominio.modelos.TipoPrestador.SUCURSAL,
                        titulo = emp.empresa.nombre,
                        nombreSucursal = suc.sucursal.nombre,
                        urlMiniatura = emp.empresa.urlMiniatura ?: emp.empresa.urlFoto,
                        codigoPostal = suc.direccion?.codigoPostal,
                        idCategorias = emp.empresa.idCategorias,
                        estaVerificado = emp.empresa.estaVerificada
                    )
                }
            }
            listOf(personal) + empresas
        }
    }

    LaunchedEffect(promocionId) {
        Log.d(tag, "🔍 [LOAD_PROMO] Buscando: $promocionId")
        promocionId?.let { viewModel.loadPromotion(it) }
    }

    var perfilSeleccionado by remember { mutableStateOf<PrestadorDominio?>(null) }

    LaunchedEffect(perfilesDisponibles) {
        if (perfilSeleccionado == null && perfilesDisponibles.isNotEmpty()) {
            perfilSeleccionado = perfilesDisponibles.first()
        }
    }

    val idPrestador = ecosistema?.cuenta?.id.orEmpty()
    val perfilActivo = perfilSeleccionado
    
    val nombrePrestador = perfilActivo?.titulo ?: "Profesional"
    val urlFotoPrestador = perfilActivo?.urlMiniatura as? String
    val cpFinal = perfilActivo?.codigoPostal.orEmpty()
    val estaVerificado = perfilActivo?.estaVerificado ?: false

    var tipoSeleccionado       by remember { mutableStateOf(TipoPromocion.PROMOCION) }
    var titulo                 by remember { mutableStateOf("") }
    var descripcion            by remember { mutableStateOf("") }
    var imagenesSeleccionadas   by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (imagenesSeleccionadas.size + uris.size <= 5) {
            imagenesSeleccionadas = imagenesSeleccionadas + uris
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (promocionId == null) "CREAR PUBLICACIÓN" else "EDITAR PUBLICACIÓN", fontWeight = FontWeight.Black, fontSize = 16.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF16161D), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF0F0F15)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            
            if (perfilesDisponibles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PUBLICAR COMO:", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        perfilesDisponibles.forEach { perf ->
                            ProfileSelectorItem(
                                nombre = if (perf.idEmpresa == null) "Personal" else perf.nombreSucursal ?: perf.titulo,
                                foto = perf.urlMiniatura as? String,
                                isSelected = perfilSeleccionado?.id == perf.id,
                                onClick = { perfilSeleccionado = perf }
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeButton(
                    modifier = Modifier.weight(1f),
                    label = "PROMOCIÓN",
                    desc = "7 días de visibilidad",
                    isSelected = tipoSeleccionado == TipoPromocion.PROMOCION,
                    color = Color(0xFF3B82F6),
                    onClick = { tipoSeleccionado = TipoPromocion.PROMOCION }
                )
                TypeButton(
                    modifier = Modifier.weight(1f),
                    label = "HISTORIA",
                    desc = "24 horas de impacto",
                    isSelected = tipoSeleccionado == TipoPromocion.HISTORIA,
                    color = Color(0xFFF43F5E),
                    onClick = { tipoSeleccionado = TipoPromocion.HISTORIA }
                )
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { if (it.length <= TITULO_MAX) titulo = it },
                label = { Text("Título de la oferta") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${titulo.length}/$TITULO_MAX") }
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { if (it.length <= DESCRIPCION_MAX) descripcion = it },
                label = { Text("Descripción detallada") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                supportingText = { Text("${descripcion.length}/$DESCRIPCION_MAX") }
            )

            Button(
                onClick = {
                    viewModel.createPromotion(
                        idPrestador = idPrestador,
                        nombrePrestador = nombrePrestador,
                        urlFotoPrestador = urlFotoPrestador,
                        tipo = tipoSeleccionado,
                        tipoCategoria = TipoCategoriaPromo.SERVICIO,
                        titulo = titulo,
                        descripcion = descripcion,
                        urisImagenes = imagenesSeleccionadas,
                        descuento = "",
                        etiquetaPromocion = null,
                        etiquetas = emptyList(),
                        idCategorias = (perfilActivo?.idCategorias ?: emptyList()).toSet(),
                        codigoPostal = cpFinal,
                        estaVerificado = estaVerificado,
                        idEmpresa = perfilActivo?.idEmpresa,
                        idSucursal = if (perfilActivo?.idEmpresa != null) perfilActivo.id else null,
                        onSuccess = { onBack() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
            ) {
                Text("PUBLICAR AHORA", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ProfileSelectorItem(nombre: String, foto: String?, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.width(80.dp)) {
        Box(modifier = Modifier.size(56.dp).border(2.dp, if (isSelected) Color(0xFF3B82F6) else Color.Transparent, CircleShape).padding(4.dp)) {
            AsyncImage(model = foto, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
        }
        Text(nombre, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontSize = 10.sp, color = if (isSelected) Color.White else Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun TypeButton(modifier: Modifier, label: String, desc: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.2.dp, if (isSelected) color else Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isSelected) color else Color.White)
            Text(desc, fontSize = 9.sp, color = Color.Gray)
        }
    }
}
