package com.example.myapplication.prestador.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

private data class FaqItem(val pregunta: String, val respuesta: String)
private data class FaqCategoria(val nombre: String, val icono: androidx.compose.ui.graphics.vector.ImageVector, val items: List<FaqItem>)

private val faqCategorias = listOf(
    FaqCategoria(
        nombre = "Presupuestos",
        icono = Icons.Default.Description,
        items = listOf(
            FaqItem(
                "¿Cómo creo un presupuesto?",
                "Andá a Accesos rápidos → Crear Presupuesto. Completá los datos del cliente, agregá los ítems y tocá Compartir para enviarlo como PDF."
            ),
            FaqItem(
                "¿Puedo guardar ítems para reutilizarlos?",
                "Sí. Usá Mi Catálogo (en Accesos rápidos) para guardar artículos, servicios y honorarios que usás frecuentemente."
            ),
            FaqItem(
                "¿El cliente recibe el presupuesto directamente?",
                "Por ahora el presupuesto se genera como PDF y lo compartís vos por WhatsApp, email u otra app. En próximas versiones podrás enviarlo desde la app."
            )
        )
    ),
    FaqCategoria(
        nombre = "Turnos",
        icono = Icons.Default.CalendarToday,
        items = listOf(
            FaqItem(
                "¿Cómo creo un turno?",
                "Tocá Crear Turno en Accesos rápidos, completá el nombre del cliente, fecha, hora y servicio, y confirmá."
            ),
            FaqItem(
                "¿Puedo configurar mi disponibilidad horaria?",
                "Sí, si sos profesional aparece el botón Disponibilidad en Accesos rápidos. Desde ahí configurás tus días y horarios habilitados."
            ),
            FaqItem(
                "¿Los clientes pueden sacar turnos solos?",
                "Esta función estará disponible con la suscripción Agenda Pro. Por ahora los turnos los cargás vos manualmente."
            )
        )
    ),
    FaqCategoria(
        nombre = "Mi cuenta",
        icono = Icons.Default.Person,
        items = listOf(
            FaqItem(
                "¿Cómo edito mi perfil?",
                "Desde el menú superior (tres puntos) elegí Editar perfil. Podés cambiar tu nombre, foto, descripción y tipo de servicio."
            ),
            FaqItem(
                "¿Cómo cambio mi contraseña?",
                "Ve a Editar perfil → Seguridad. Ingresá tu contraseña actual y la nueva para confirmar el cambio."
            ),
            FaqItem(
                "¿Cómo cierro sesión?",
                "Desde el menú superior (tres puntos) tocá Cerrar sesión. Te pedirá confirmación antes de desconectarte."
            )
        )
    ),
    FaqCategoria(
        nombre = "General",
        icono = Icons.Default.HelpOutline,
        items = listOf(
            FaqItem(
                "¿La app funciona sin internet?",
                "Algunas funciones como ver tu calendario y catálogo funcionan offline. Para sincronizar datos necesitás conexión."
            ),
            FaqItem(
                "¿Mis datos están seguros?",
                "Sí. Toda la información se transmite cifrada y nuestros servidores cumplen con las normas de protección de datos."
            ),
            FaqItem(
                "¿Cómo reporto un error?",
                "Andá a Ayuda y soporte → Reportar un problema. Describí lo que pasó y si podés adjuntá una captura de pantalla."
            )
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroAyudaSheet(onDismiss: () -> Unit) {
    val colors = getPrestadorColors()
    val accent = colors.primaryOrange
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var busqueda by remember { mutableStateOf("") }
    val categoriasFiltradas = remember(busqueda) {
        if (busqueda.isBlank()) faqCategorias
        else faqCategorias.mapNotNull { cat ->
            val itemsFiltrados = cat.items.filter {
                it.pregunta.contains(busqueda, ignoreCase = true) ||
                it.respuesta.contains(busqueda, ignoreCase = true)
            }
            if (itemsFiltrados.isEmpty()) null else cat.copy(items = itemsFiltrados)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Handle + Header fijo
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.textSecondary.copy(alpha = 0.3f))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MenuBook, null, tint = accent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Centro de ayuda", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Text("Encontrá respuestas rápidas", fontSize = 12.sp, color = colors.textSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Buscador
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar en ayuda...", color = colors.textSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.textSecondary) },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Default.Close, null, tint = colors.textSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.3f),
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = accent,
                        focusedContainerColor = colors.backgroundColor.copy(alpha = 0.4f),
                        unfocusedContainerColor = colors.backgroundColor.copy(alpha = 0.4f)
                    ),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
            }

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))

            // Lista de FAQs scrolleable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                if (categoriasFiltradas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.SearchOff, null, tint = colors.textSecondary, modifier = Modifier.size(40.dp))
                            Text("Sin resultados para \"$busqueda\"", fontSize = 14.sp, color = colors.textSecondary)
                        }
                    }
                } else {
                    categoriasFiltradas.forEach { categoria ->
                        FaqCategoriaSection(
                            categoria = categoria,
                            accentColor = accent,
                            expandedByDefault = busqueda.isNotBlank()
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqCategoriaSection(
    categoria: FaqCategoria,
    accentColor: Color,
    expandedByDefault: Boolean = false
) {
    val colors = getPrestadorColors()
    var expandida by remember(expandedByDefault) { mutableStateOf(expandedByDefault) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.backgroundColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header de categoría
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandida = !expandida }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoria.icono, null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Text(
                    categoria.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${categoria.items.size} preguntas",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Icon(
                    if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Items expandibles
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))
                    categoria.items.forEachIndexed { idx, item ->
                        FaqItemRow(item = item, accentColor = accentColor)
                        if (idx < categoria.items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = colors.textSecondary.copy(alpha = 0.07f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqItemRow(item: FaqItem, accentColor: Color) {
    val colors = getPrestadorColors()
    var expandido by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.HelpOutline,
                null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                item.pregunta,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = colors.textSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Text(
                item.respuesta,
                fontSize = 13.sp,
                color = colors.textSecondary,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 8.dp, start = 26.dp)
            )
        }
    }
}
