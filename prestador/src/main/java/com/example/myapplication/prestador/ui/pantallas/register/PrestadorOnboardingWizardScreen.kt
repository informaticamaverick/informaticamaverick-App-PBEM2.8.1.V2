package com.example.myapplication.prestador.ui.pantallas.register

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

/**
 * --- PANTALLA DE ELECCIÓN MAVERICK (WIZARD) ---
 * Guía al usuario para definir su perfil: Independiente vs Empresa.
 * [v2026.12]: Diseño moderno y segmentación clara.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestadorOnboardingWizardScreen(
    isGoogle: Boolean,
    onBack: () -> Unit,
    onNavigateToForm: (tieneNegocio: Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    var seleccion by remember { mutableStateOf<Boolean?>(null) } // null: nada, false: personal, true: empresa

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configura tu Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.backgroundColor)
            )
        },
        containerColor = colors.backgroundColor,
        bottomBar = {
            AnimatedVisibility(
                visible = seleccion != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(shadowElevation = 8.dp, color = colors.surfaceColor) {
                    Button(
                        onClick = { onNavigateToForm(seleccion ?: false) },
                        modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                    ) {
                        Text("CONTINUAR", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            
            Text(
                text = "¿Cómo vas a ofrecer tus servicios?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            
            Text(
                text = "Seleccioná la opción que mejor describa tu actividad actual.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            WizardChoiceCard(
                titulo = "Prestador Independiente",
                descripcion = "Trabajo solo, a domicilio o en un espacio compartido.",
                icono = Icons.Default.Person,
                seleccionada = seleccion == false,
                onClick = { seleccion = false }
            )

            Spacer(Modifier.height(16.dp))

            WizardChoiceCard(
                titulo = "Empresa o Local Físico",
                descripcion = "Tengo mi propio local, sucursales y manejo un equipo de trabajo.",
                icono = Icons.Default.Business,
                seleccionada = seleccion == true,
                onClick = { seleccion = true }
            )
            
            Spacer(Modifier.weight(1f))
            
            if (seleccion == true) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.primaryOrange.copy(0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Acceso a Jerarquía 3x3: Gestioná hasta 3 sucursales y todo tu equipo operativo.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primaryOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WizardChoiceCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    val borderColor = if (seleccionada) colors.primaryOrange else Color.White.copy(0.1f)
    val bgColor = if (seleccionada) colors.primaryOrange.copy(0.05f) else Color.White.copy(0.02f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(BorderStroke(if (seleccionada) 2.dp else 1.dp, borderColor), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (seleccionada) colors.primaryOrange else Color.White.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = if (seleccionada) Color.White else Color.Gray, modifier = Modifier.size(28.dp))
            }
            
            Spacer(Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if (seleccionada) Color.White else Color.Gray)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 16.sp)
            }
            
            if (seleccionada) {
                Icon(Icons.Default.CheckCircle, null, tint = colors.primaryOrange)
            }
        }
    }
}















































