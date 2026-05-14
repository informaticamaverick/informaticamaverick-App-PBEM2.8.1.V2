package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.ui.theme.PrestadorColors


@Composable
fun ModernCard(
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            // Header con ícono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            
            // Contenido
            content()
        }
    }
}

@Composable
fun SwitchOption(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primaryOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun VerificadoBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Verificado",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Verificado",
            fontSize = 12.sp,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Medium
        )
    }
}

// ── BOTTOM SHEET CREAR EMPRESA ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EmpresaBottomSheet(
    colors: PrestadorColors,
    onDismiss: () -> Unit,
    onAceptar: (CompanyProvider) -> Unit
) {
    var nombreComercial by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var emailCorporativo by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceColor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Datos de Empresa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFFFF5252)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryOrange,
                unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.4f),
                focusedLabelColor = colors.primaryOrange,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.primaryOrange
            )

            OutlinedTextField(
                value = nombreComercial,
                onValueChange = { nombreComercial = it },
                label = { Text("Nombre Comercial") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = razonSocial,
                onValueChange = { razonSocial = it },
                label = { Text("Razón Social") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = cuit,
                onValueChange = { cuit = it.filter { c -> c.isDigit() || c == '-' } },
                label = { Text("CUIT") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = emailCorporativo,
                onValueChange = { emailCorporativo = it },
                label = { Text("Email Corporativo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onAceptar(
                        CompanyProvider(
                            name = nombreComercial.trim(),
                            razonSocial = razonSocial.trim(),
                            cuit = cuit.trim(),
                            email = emailCorporativo.trim()
                        )
                    )
                },
                enabled = nombreComercial.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange,
                    disabledContainerColor = colors.primaryOrange.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Aceptar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── CARD SECCIÓN GENÉRICA ────────────────────────────────────────────────────
@Composable
internal fun ProfileSectionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    colors: PrestadorColors,
    actionButton: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, Modifier.size(20.dp), iconColor)
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                if (actionButton != null) {
                    Spacer(Modifier.weight(1f))
                    actionButton()
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── FILA DE DATO CON EMOJI ───────────────────────────────────────────────────
@Composable
internal fun ProfileInfoRow(
    emoji: String,
    label: String,
    value: String,
    colors: PrestadorColors
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 14.sp, modifier = Modifier.width(26.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = colors.textPrimary)
        }
    }
}

// ── CARD EMPRESA CON SUCURSALES Y EQUIPO ─────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfileEmpresaCard(company: CompanyProvider, colors: PrestadorColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header empresa
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, null, Modifier.size(20.dp), Color(0xFF8B5CF6))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    company.name.ifBlank { "Empresa" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            if (company.razonSocial.isNotBlank()) {
                ProfileInfoRow("🏢", "Razón Social", company.razonSocial, colors)
                Spacer(Modifier.height(8.dp))
            }
            if (company.cuit.isNotBlank()) {
                ProfileInfoRow("🆔", "CUIT", company.cuit, colors)
                Spacer(Modifier.height(8.dp))
            }
            if (company.description.isNotBlank()) {
                Text(company.description, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
            }

            // Rubros empresa
            if (company.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    company.categories.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f))
                        ) {
                            Text(
                                cat,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Sucursales
            company.branches.forEachIndexed { index, branch ->
                BranchSection(index = index, branch = branch, colors = colors)
            }
        }
    }
}

// ── BARRA INFERIOR DE ACCIONES ───────────────────────────────────────────────
@Composable
internal fun ProfileBottomBar(
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    colors: PrestadorColors
) {
    Surface(
        color = colors.surfaceColor,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileBottomBarButton(
                icon = Icons.Default.Edit,
                label = "EDITAR",
                tint = colors.primaryOrange,
                onClick = onEdit
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(colors.textSecondary.copy(alpha = 0.2f))
            )
            ProfileBottomBarButton(
                icon = Icons.Default.Settings,
                label = "AJUSTES",
                tint = colors.textPrimary,
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun ProfileBottomBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            letterSpacing = 0.5.sp
        )
    }
}

