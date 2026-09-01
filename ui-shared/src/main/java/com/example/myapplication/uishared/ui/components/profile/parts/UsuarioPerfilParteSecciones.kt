package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio

/**
 * --- SECCIONES DEL PERFIL DE USUARIO (Elite v2026) ---
 */

@Composable
fun SeccionDatosUsuarioMav(
    usuario: PrestadorDominio,
    esMiPropioPerfil: Boolean,
    alGuardar: (PrestadorDominio) -> Unit
) {
    var modoEdicion by remember { mutableStateOf(false) }
    var nombre by remember(usuario) { mutableStateOf(usuario.nombre) }
    var apellido by remember(usuario) { mutableStateOf(usuario.apellido) }
    var nombrePublico by remember(usuario) { mutableStateOf(usuario.titulo) }
    var correo by remember(usuario) { mutableStateOf(usuario.correo) }
    var telefono by remember(usuario) { mutableStateOf(usuario.numeroTelefono) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CabeceraSeccionMav("DATOS PERSONALES", "👤")
                
                if (esMiPropioPerfil) {
                    if (modoEdicion) {
                        Row {
                            IconButton(onClick = { 
                                alGuardar(usuario.copy(
                                    nombre = nombre,
                                    apellido = apellido,
                                    titulo = nombrePublico, 
                                    correo = correo, 
                                    numeroTelefono = telefono
                                ))
                                modoEdicion = false 
                            }) { Icon(Icons.Default.Check, null, tint = Color(0xFF4ADE80)) }
                            IconButton(onClick = { modoEdicion = false }) { Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.6f)) }
                        }
                    } else {
                        IconButton(onClick = { modoEdicion = true }) { Icon(Icons.Default.Edit, null, tint = Color(0xFF3B82F6)) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilaDatoPerfilMav("NOMBRE", nombre, Icons.Default.Person, modoEdicion, { nombre = it })
                FilaDatoPerfilMav("APELLIDO", apellido, Icons.Default.Person, modoEdicion, { apellido = it })
                FilaDatoPerfilMav("NOMBRE PÚBLICO", nombrePublico, Icons.Default.Face, modoEdicion, { nombrePublico = it })
                FilaDatoPerfilMav("CORREO", correo, Icons.Default.Email, modoEdicion, { correo = it })
                FilaDatoPerfilMav("TELÉFONO", telefono, Icons.Default.Phone, modoEdicion, { telefono = it })
            }
        }
    }
}


@Composable
fun SeccionDireccionesUsuarioMav(
    direcciones: List<DireccionDominio>,
    esMiPropioPerfil: Boolean,
    alAbrirEditor: (DireccionDominio) -> Unit,
    alEliminar: (DireccionDominio) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CabeceraSeccionMav("MIS DIRECCIONES DE SERVICIO", "📍", modifier = Modifier.padding(horizontal = 4.dp))
        Spacer(Modifier.height(12.dp))

        if (direcciones.isEmpty()) {
            Text(
                "No tienes direcciones guardadas.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            direcciones.forEach { dir ->
                TarjetaDireccionEliteMav(
                    direccion = dir,
                    esSoloLectura = !esMiPropioPerfil,
                    alEditar = { alAbrirEditor(dir) },
                    alBorrar = { alEliminar(dir) },
                    mostrarBotonBorrar = esMiPropioPerfil,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        
        if (esMiPropioPerfil) {
            Button(
                onClick = { alAbrirEditor(DireccionDominio()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("AÑADIR DIRECCIÓN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SeccionVinculoComercialMav(
    sucursalesAsociadas: List<DireccionDominio> // Representamos sucursales como direcciones con contexto
) {
    if (sucursalesAsociadas.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        CabeceraSeccionMav("PUNTOS DE VENTA VINCULADOS", "🏪", modifier = Modifier.padding(horizontal = 4.dp))
        Spacer(Modifier.height(12.dp))

        sucursalesAsociadas.forEach { suc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24).copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF3B82F6).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏪", fontSize = 20.sp)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            suc.etiqueta.uppercase().ifBlank { "SUCURSAL" },
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            suc.aTextoCompleto(),
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

































