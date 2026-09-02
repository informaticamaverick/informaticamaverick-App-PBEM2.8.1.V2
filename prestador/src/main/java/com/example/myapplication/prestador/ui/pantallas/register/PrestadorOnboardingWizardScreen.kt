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
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme

/**
 * --- PANTALLA DE ELECCIÓN MAVERICK (WIZARD) ---
 * Guía al usuario para definir su perfil: Independiente vs Empresa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestadorOnboardingWizardScreen(
    isGoogle: Boolean,
    onBack: () -> Unit,
    onNavigateToForm: (tieneNegocio: Boolean) -> Unit
) {
    val colors = GestionTurnosTheme
    var seleccion by remember { mutableStateOf<Boolean?>(null) } // null: nada, false: personal, true: empresa

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configurá tu Perfil", fontWeight = FontWeight.Black, color = colors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.TextPrimary) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.DarkBg)
            )
        },
        containerColor = colors.DarkBg,
        bottomBar = {
            AnimatedVisibility(
                visible = seleccion != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(shadowElevation = 8.dp, color = colors.CardBg, border = BorderStroke(1.dp, colors.BorderGlass)) {
                    Button(
                        onClick = { onNavigateToForm(seleccion ?: false) },
                        modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(colors.BrandOrange, Color(0xFFFB923C))),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("CONTINUAR", color = Color.Black, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
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
                color = colors.TextPrimary
            )

            Text(
                text = "Seleccioná la opción que mejor describa tu actividad actual.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colors.TextSecondary,
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
                Surface(
                    color = colors.AccentViolet.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = colors.AccentViolet, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Acceso a Jerarquía 3x3: Gestioná hasta 3 sucursales y todo tu equipo operativo.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.AccentViolet,
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
    val colors = GestionTurnosTheme
    val borderColor = if (seleccionada) colors.BrandOrange else colors.BorderGlass
    val bgColor = if (seleccionada) colors.BrandOrange.copy(alpha = 0.08f) else colors.CardBg

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
                    .background(if (seleccionada) colors.BrandOrange else colors.SurfaceInput),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = if (seleccionada) Color.Black else colors.TextMuted, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if (seleccionada) colors.TextPrimary else colors.TextSecondary)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = colors.TextMuted, lineHeight = 16.sp)
            }

            if (seleccionada) {
                Icon(Icons.Default.CheckCircle, null, tint = colors.BrandOrange)
            }
        }
    }
}















































