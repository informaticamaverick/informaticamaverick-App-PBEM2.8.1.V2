package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

/**
 * ==========================================================================================
 * --- 🛡️ COMPONENTES DE AVISOS Y NOTIFICACIONES MAVERICK ---
 * ==========================================================================================
 * Centraliza todos los popups informativos, advertencias y banners tácticos del sistema.
 * Estilo: Elite Cyberpunk / Striking Visuals.
 */

/**
 * --- MODAL: MODERN ADDRESS POPUP (AVISO DIRECCIÓN FALTANTE) ---
 * Diseño moderno y descriptivo con emojis para incentivar al usuario a configurar su ubicación.
 * Estilo: Cyberpunk / Glassmorphism Maverick.
 */
@Composable
fun ModernAddressPopup(
    onDismiss: () -> Unit,
    onGoToProfile: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaverickColors.AbsoluteBlack.copy(alpha = 0.95f))
                .border(2.dp, MaverickColors.GeminiBrush, RoundedCornerShape(28.dp))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            // Fondo con degradado de profundidad Maverick
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icono Striking Central
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaverickColors.GeminiAccent.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, MaverickColors.GeminiAccent.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📍", fontSize = 44.sp)
                    }

                    Text(
                        text = "¡OPTIMIZA TU EXPERIENCIA!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.2.sp
                    )

                    Text(
                        text = "Detectamos que aún no tienes una dirección guardada. \n\nConfigúrala ahora para que Maverick encuentre automáticamente a los mejores profesionales en tu zona. \n\n🚀 ¡No pierdas tiempo buscando!",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón de Acción Principal (Gemini Brush)
                    Surface(
                        onClick = onGoToProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaverickColors.GeminiBrush)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaverickColors.GeminiAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CONFIGURAR DIRECCIÓN",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Botón de Salida Silenciosa
                    Text(
                        text = "Quizás más tarde",
                        color = MaverickColors.ElectricCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { onDismiss() }
                    )
                }
            }
        }
    }
}

/**
 * --- MODAL: INFORMATIVO GENÉRICO PREMIUM ---
 * Modal reutilizable para noticias, actualizaciones o avisos de mantenimiento.
 */
@Composable
fun ModalInformativoMaverick(
    title: String,
    message: String,
    emoji: String = "✨",
    buttonText: String = "ENTENDIDO",
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onConfirm) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaverickColors.AbsoluteBlack.copy(alpha = 0.9f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(emoji, fontSize = 48.sp)
                
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaverickColors.ElectricCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

/**
 * --- BANNER: ALERTA TÁCTICA SUPERIOR ---
 * Banner sutil pero visible que aparece en la parte superior para avisos rápidos.
 */
@Composable
fun BannerAlertaTactica(
    message: String,
    icon: ImageVector = Icons.Default.Info,
    accentColor: Color = MaverickColors.ElectricCyan,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        color = MaverickColors.AbsoluteBlack.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
            
            Text(
                text = message,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * --- COMPONENTE: SNACKBAR / TOAD MAVERICK ---
 * Estilo: Glassmorphism / Luminous Border.
 * Diseñado para notificaciones rápidas y elegantes que flotan sobre la UI.
 */
@Composable
fun MaverickToadNotification(
    message: String,
    emoji: String = "🚀",
    accentColor: Color = MaverickColors.ElectricCyan
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaverickColors.AbsoluteBlack.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==========================================================================================
// --- 🎨 SECCIÓN: PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
fun PreviewModernAddressPopup() {
    MyApplicationTheme {
        ModernAddressPopup(
            onDismiss = { },
            onGoToProfile = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
fun PreviewModalInformativo() {
    MyApplicationTheme {
        ModalInformativoMaverick(
            title = "Nueva Actualización",
            message = "Hemos mejorado el sistema de búsqueda por voz para que encuentres todo más rápido.",
            emoji = "🚀",
            onConfirm = {}
        )
    }
}

@Preview
@Composable
fun PreviewBannerTactica() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            BannerAlertaTactica(
                message = "Modo sin conexión activado. Algunos servicios podrían no estar disponibles.",
                icon = Icons.Default.Warning,
                accentColor = MaverickColors.WarningRed,
                onClose = {}
            )
        }
    }
}

@Preview
@Composable
fun PreviewMaverickToad() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.BottomCenter) {
            MaverickToadNotification(
                message = "Presupuesto enviado con éxito a la nube Maverick.",
                emoji = "✅",
                accentColor = MaverickColors.SuccessGreen
            )
        }
    }
}
