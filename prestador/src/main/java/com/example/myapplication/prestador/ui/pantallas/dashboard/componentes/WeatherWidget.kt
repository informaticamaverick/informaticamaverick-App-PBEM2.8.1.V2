package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.dominio.modelos.InformacionClima
import com.example.myapplication.prestador.viewmodel.dashboard.WeatherViewModel

@Composable
fun WeatherWidget(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = state !is WeatherViewModel.WeatherState.Loading,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 },
        modifier = modifier
    ) {
        when (val s = state) {
            is WeatherViewModel.WeatherState.Success -> {
                WeatherContent(
                    data = s.data,
                    onRefresh = { viewModel.loadWeather() },
                    onClick = { showSheet = true }
                )
                if (showSheet) {
                    WeatherForecastSheet(
                        weather = s.data,
                        onDismiss = { showSheet = false }
                    )
                }
            }
            is WeatherViewModel.WeatherState.Error ->
                WeatherError(onRefresh = { viewModel.loadWeather() })
            else -> Unit
        }
    }
}

@Composable
private fun WeatherContent(
    data: InformacionClima,
    onRefresh: () -> Unit,
    onClick: () -> Unit
) {
    val infiniteAnim = rememberInfiniteTransition(label = "weather")
    val scale by infiniteAnim.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "pulse"
    )
    val rotation by infiniteAnim.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(8000, easing = LinearEasing)
        ), label = "rotate"
    )
    val isSunny = data.emojiClima == "☀️"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.30f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = data.emojiClima,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .scale(scale)
                        .then(if (isSunny) Modifier.rotate(rotation) else Modifier)
                )
                Column {
                    Text(
                        text = data.temperatura,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3D1100)
                    )
                    Text(
                        text = data.nombreCiudad,
                        fontSize = 11.sp,
                        color = Color(0xFF5D2000).copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = data.descripcionClima,
                        fontSize = 11.sp,
                        color = Color(0xFF5D2000),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💧 ${data.humedad}", fontSize = 10.sp, color = Color(0xFF5D2000).copy(alpha = 0.7f))
                        Text("💨 ${data.velocidadViento}", fontSize = 10.sp, color = Color(0xFF5D2000).copy(alpha = 0.7f))
                    }
                }
                Icon(
                    Icons.Default.KeyboardArrowDown, null,
                    tint = Color(0xFF5D2000).copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WeatherError(onRefresh: () -> Unit) {
    val rotation by rememberInfiniteTransition(label = "spin")
        .animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
            label = "refresh"
        )
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.20f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sin datos de clima", fontSize = 12.sp, color = Color(0xFF5D2000))
            IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Refresh, null,
                    tint = Color(0xFF5D2000),
                    modifier = Modifier.size(18.dp).rotate(rotation)
                )
            }
        }
    }
}























