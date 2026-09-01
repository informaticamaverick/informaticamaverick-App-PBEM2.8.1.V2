package com.example.myapplication.uishared.ui.components.profile.parts

import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.utilidades.ImageUtils

/**
 * --- BLOQUES REUTILIZABLES DEL PERFIL (Ley #10) ---
 * [PROPÓSITO]: Moléculas de UI que agrupan piezas atómicas para formar filas o tarjetas.
 */

private val ColorAcentoMav = Color(0xFF3B82F6)

@Composable
fun FilaDatoPerfilMav(
    etiqueta: String,
    valor: String,
    icono: ImageVector,
    enModoEdicion: Boolean = false,
    alCambiarValor: (String) -> Unit = {},
    esGoogle: Boolean = false,
    iconoFinal: @Composable (() -> Unit)? = null
) {
    val contexto = LocalContext.current
    val esEmail = remember(valor) { Patterns.EMAIL_ADDRESS.matcher(valor).matches() }
    val alfa = if (enModoEdicion || valor.isNotBlank()) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(alfa),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = ColorAcentoMav.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (esGoogle) {
                    Icon(painterResource(id = com.example.myapplication.core.R.drawable.icons8_logo_de_google_48), null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                } else {
                    Icon(icono, null, tint = ColorAcentoMav, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            if (enModoEdicion && !esGoogle && iconoFinal == null) {
                BasicTextField(
                    value = valor,
                    onValueChange = alCambiarValor,
                    textStyle = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(ColorAcentoMav),
                    decorationBox = { innerTextField ->
                        Box {
                            if (valor.isEmpty()) Text("Completar...", color = Color.Gray, fontSize = 14.sp)
                            innerTextField()
                        }
                    }
                )
            } else {
                Text(
                    text = valor.ifBlank { "No especificado" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (esEmail && !enModoEdicion) TextDecoration.Underline else TextDecoration.None
                    ),
                    color = if (esEmail && !enModoEdicion) ColorAcentoMav else if (enModoEdicion && esGoogle) Color.Gray else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = esEmail && !enModoEdicion) {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$valor")
                                putExtra(Intent.EXTRA_SUBJECT, "Contacto desde Maverick")
                            }
                            contexto.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(contexto, "No hay apps de correo instaladas", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
        if (iconoFinal != null) iconoFinal()
    }
}

@Composable
fun EtiquetaFlagMav(
    titulo: String,
    emoji: String,
    habilitado: Boolean,
    enModoEdicion: Boolean = false,
    alCambiar: (Boolean) -> Unit = {}
) {
    val colorAcento = Color(0xFF3B82F6)
    val colorFondo = if (habilitado) colorAcento.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f)
    val colorBorde = if (habilitado) colorAcento.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
    val colorTexto = if (habilitado) Color.White else Color.Gray

    Surface(
        onClick = { if (enModoEdicion) alCambiar(!habilitado) },
        color = colorFondo,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colorBorde),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = titulo.uppercase(),
                    color = colorTexto,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
            
            if (enModoEdicion) {
                Checkbox(
                    checked = habilitado,
                    onCheckedChange = alCambiar,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorAcento,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
            } else if (habilitado) {
                Icon(
                    Icons.Default.CheckCircle, 
                    null, 
                    tint = colorAcento, 
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FilaSwitchMav(
    etiqueta: String,
    activado: Boolean,
    habilitado: Boolean = true,
    alCambiar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().alpha(if (habilitado) 1f else 0.5f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, color = Color.White, fontSize = 14.sp)
        Switch(
            checked = activado,
            onCheckedChange = alCambiar,
            enabled = habilitado,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ColorAcentoMav,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun TarjetaDireccionEliteMav(
    direccion: DireccionDominio, 
    alEditar: (DireccionDominio) -> Unit = {},
    alBorrar: () -> Unit = {},
    esPuntoDeVenta: Boolean = false,
    contenidoExtra: (@Composable () -> Unit)? = null,
    mostrarBotonBorrar: Boolean = false,
    esSoloLectura: Boolean = false,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val tieneLocal = direccion.tieneLocalFisico
    val estaVerificada = direccion.estaVerificadaGps

    Surface(
        onClick = { if (!esSoloLectura) alEditar(direccion) },
        color = Color(0xFF1A1A24).copy(alpha = 0.6f), 
        shape = RoundedCornerShape(16.dp), 
        border = BorderStroke(
            width = if (estaVerificada) 1.dp else 0.5.dp, 
            color = if (estaVerificada) ColorAcentoMav.copy(0.6f) else Color.White.copy(0.1f)
        ),
        modifier = modifier
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(0.05f)
                ) {
                    Box(contentAlignment = Alignment.Center) { 
                        if (tieneLocal) {
                            Text("🏬", fontSize = 26.sp)
                        } else {
                            Icon(
                                imageVector = if (esPuntoDeVenta) Icons.Default.BusinessCenter else Icons.Default.HomeWork, 
                                contentDescription = null, 
                                tint = if (esPuntoDeVenta) ColorAcentoMav else Color.Gray,
                                modifier = Modifier.size(26.dp)
                            ) 
                        }
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    val textoEtiqueta = when {
                        estaVerificada -> "UBICACIÓN VERIFICADA ✅"
                        direccion.etiqueta.isNotBlank() -> direccion.etiqueta.uppercase()
                        esPuntoDeVenta -> "SUCURSAL"
                        else -> "CASA CENTRAL"
                    }
                    
                    Text(
                        text = textoEtiqueta, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = if (estaVerificada) Color(0xFF4ADE80) else ColorAcentoMav, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp
                    )
                    
                    Text(
                        text = direccion.aTextoCorto(),
                        style = MaterialTheme.typography.titleMedium, 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "${direccion.localidad}, ${direccion.provincia}", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(direccion.aTextoCompleto())}")
                            try {
                                contexto.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                            } catch (e: Exception) {
                                try {
                                    contexto.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (e2: Exception) {
                                    android.widget.Toast.makeText(contexto, "No se pudo abrir el mapa", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Map, null, tint = ColorAcentoMav, modifier = Modifier.size(20.dp))
                    }
                    
                    if (mostrarBotonBorrar && !esSoloLectura) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = alBorrar, modifier = Modifier.size(32.dp)) { 
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(20.dp)) 
                        }
                    }
                }
            }
            
            if (contenidoExtra != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                    contenidoExtra()
                }
            }
        }
    }
}

@Composable
fun TarjetaVinculoGoogleMav(
    emailGoogle: String,
    enModoEdicion: Boolean,
    alVincular: () -> Unit,
    alDesvincular: () -> Unit,
    forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    var mostrarConfirmacionDesvincular by remember { mutableStateOf(false) }
    val estaVinculado = emailGoogle.isNotBlank()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (estaVinculado) Modifier 
                else Modifier.border(1.dp, Color(0xFF4285F4).copy(alpha = 0.2f), forma)
            ),
        shape = forma,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (estaVinculado) Color(0xFF16161D) else Color(0xFF4285F4).copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = com.example.myapplication.core.R.drawable.icons8_logo_de_google_48),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (estaVinculado) "CUENTA DE GOOGLE VINCULADA" else "VINCULAR CUENTA DE GOOGLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (estaVinculado) Color.Gray else Color(0xFF4285F4),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (estaVinculado) emailGoogle else "Asegura tu perfil y sincroniza tus datos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (enModoEdicion && estaVinculado) {
                    IconButton(onClick = { mostrarConfirmacionDesvincular = true }) {
                        Icon(Icons.Default.LinkOff, "Desvincular", tint = Color.Red.copy(alpha = 0.7f))
                    }
                } else if (!estaVinculado) {
                    Button(
                        onClick = alVincular,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("VINCULAR", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (mostrarConfirmacionDesvincular) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionDesvincular = false },
            title = { Text("⚠️ Desvincular Google", color = Color.White) },
            text = { 
                Text(
                    "Al desvincular tu cuenta de Google, perderás la capacidad de iniciar sesión con este método.",
                    color = Color.White.copy(alpha = 0.7f)
                ) 
            },
            confirmButton = {
                TextButton(onClick = { 
                    mostrarConfirmacionDesvincular = false
                    alDesvincular()
                }) {
                    Text("DESVINCULAR", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionDesvincular = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A24)
        )
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun PreviewBloquesMav() {
    val m = PrestadorPerfilMocks.elenaRodriguez
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FilaDatoPerfilMav("Nombre", m.titulo, Icons.Default.Person, enModoEdicion = true)
        FilaSwitchMav("Urgencias 24h", activado = m.atiende24h, alCambiar = {})
        TarjetaDireccionEliteMav(
            direccion = DireccionDominio(calle = "Av. Aconquija", numero = "2000", localidad = "Yerba Buena"),
            esPuntoDeVenta = true
        )
        TarjetaVinculoGoogleMav(emailGoogle = m.correo, enModoEdicion = true, alVincular = {}, alDesvincular = {})
    }
}

































