package com.example.myapplication.prestador.ui.pantallas.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun TerminosScreen(onBack: () -> Unit = {}) {
    LegalContentScreen(
        title = "Términos y condiciones",
        icon = Icons.Default.Gavel,
        onBack = onBack
    ) {
        LegalParagraph(
            heading = "1. Aceptación de los términos",
            body = "Al usar la plataforma app aceptás estos términos. Si no estás de acuerdo, por favor no uses la aplicación."
        )
        LegalParagraph(
            heading = "2. Descripción del servicio",
            body = "app es una plataforma que conecta prestadores de servicios con clientes. No somos parte de los acuerdos entre prestadores y clientes."
        )
        LegalParagraph(
            heading = "3. Responsabilidades del prestador",
            body = "El prestador es responsable de la veracidad de la información publicada, del cumplimiento de los servicios ofrecidos y del trato correcto a los clientes."
        )
        LegalParagraph(
            heading = "4. Modificaciones",
            body = "Nos reservamos el derecho de modificar estos términos en cualquier momento. Te notificaremos ante cambios importantes."
        )
        LegalParagraph(
            heading = "5. Contacto",
            body = "Para consultas sobre estos términos escribinos a: soporte@app.app"
        )
    }
}

@Composable
fun PrivacidadScreen(onBack: () -> Unit = {}) {
    LegalContentScreen(
        title = "Política de privacidad",
        icon = Icons.Default.PrivacyTip,
        onBack = onBack
    ) {
        LegalParagraph(
            heading = "1. Datos que recolectamos",
            body = "Recolectamos nombre, correo electrónico, número de teléfono y datos del perfil profesional que vos ingresás voluntariamente."
        )
        LegalParagraph(
            heading = "2. Uso de los datos",
            body = "Los datos se usan para personalizar tu experiencia, mostrarte a clientes potenciales y mejorar el servicio. No vendemos tu información a terceros."
        )
        LegalParagraph(
            heading = "3. Almacenamiento",
            body = "Los datos se almacenan de forma segura en Firebase (Google). Podés solicitar la eliminación de tu cuenta y datos en cualquier momento."
        )
        LegalParagraph(
            heading = "4. Tus derechos",
            body = "Tenés derecho a acceder, corregir o eliminar tus datos personales. Para ejercer estos derechos contactanos en: privacidad@app.app"
        )
        LegalParagraph(
            heading = "5. Cookies y analytics",
            body = "Usamos herramientas de análisis para mejorar la app. No usamos cookies de rastreo externo."
        )
    }
}

// ── Pantalla base compartida ──────────────────────────────────────────────────
@Composable
private fun LegalContentScreen(
    title: String,
    icon: ImageVector,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colors.textPrimary)
            }
            Icon(icon, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        HorizontalDivider(color = colors.divider)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun LegalParagraph(heading: String, body: String) {
    val colors = getPrestadorColors()
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(heading, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryOrange)
            Text(body, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 20.sp)
        }
    }
}
















































