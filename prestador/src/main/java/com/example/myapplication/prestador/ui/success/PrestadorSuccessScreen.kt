package com.example.myapplication.prestador.ui.success

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.delay

@Composable
fun PrestadorSuccessScreen(
    onNavigateToDashboard: () -> Unit
) {
    val colors = getPrestadorColors()
    
    // Animación de escala del check
    val infiniteTransition = rememberInfiniteTransition(label = "bounceAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )
    
    // Animación de rotación del loader
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAnimation"
    )
    
    // Animación de fade in
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(2500) // Espera 2.5 segundos y navega al dashboard
        onNavigateToDashboard()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primaryOrange)
    ) {
        // --- FONDO DECORATIVO SUTIL ---
        
        // Círculo superior izquierdo (blanco)
        Box(
            modifier = Modifier
                .offset(x = (-200).dp, y = (-200).dp)
                .size(600.dp)
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .blur(radius = 100.dp)
        )
        
        // Círculo inferior derecha (negro)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 200.dp, y = 200.dp)
                .size(600.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .blur(radius = 100.dp)
        )
        
        // --- CONTENIDO PRINCIPAL ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Círculo con check animado
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    // Ícono de Check
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = android.R.drawable.ic_menu_upload
                        ),
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Texto "¡Bienvenido!"
            Text(
                text = "¡Bienvenido!",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Subtítulo
            Text(
                text = "Ingresando a tu cuenta...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loader circular giratorio
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.7f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
