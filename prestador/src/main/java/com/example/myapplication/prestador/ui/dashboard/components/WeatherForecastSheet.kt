package com.example.myapplication.prestador.ui.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.repository.DayForecast
import com.example.myapplication.core.data.repository.WeatherData
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherForecastSheet(
    weather: WeatherData,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFF3EE),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── HEADER ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pronóstico del tiempo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3D1100)
                    )
                    Text(
                        text = weather.cityName,
                        fontSize = 13.sp,
                        color = Color(0xFF5D2000).copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFF5D2000))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── CLIMA ACTUAL (grande, animado) ───────────────────────────
            val infiniteAnim = rememberInfiniteTransition(label = "main")
            val mainScale by infiniteAnim.animateFloat(
                initialValue = 1f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    tween(1400, easing = EaseInOutSine), RepeatMode.Reverse
                ), label = "mainScale"
            )
            val mainRotation by infiniteAnim.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    tween(10000, easing = LinearEasing)
                ), label = "mainRotate"
            )
            val isSunny = weather.weatherEmoji == "☀️"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF7043), Color(0xFFFFCCBC))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = weather.weatherEmoji,
                        fontSize = 64.sp,
                        modifier = Modifier
                            .scale(mainScale)
                            .then(if (isSunny) Modifier.rotate(mainRotation) else Modifier)
                    )
                    Column {
                        Text(
                            text = weather.temperature,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = weather.weatherDescription,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("💧 ${weather.humidity}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                            Text("💨 ${weather.windSpeed}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Próximos 7 días",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3D1100)
            )

            Spacer(Modifier.height(12.dp))

            // ── PRONÓSTICO 7 DÍAS (entrada escalonada) ───────────────────
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(weather.forecast) { index, day ->
                    DayForecastCard(day = day, index = index)
                }
            }
        }
    }
}

@Composable
private fun DayForecastCard(day: DayForecast, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 80).milliseconds)
        visible = true
    }
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 30.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "offsetY_$index"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha_$index"
    )
    val infiniteAnim = rememberInfiniteTransition(label = "day_$index")
    val emojiScale by infiniteAnim.animateFloat(
        initialValue = 1f, targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            tween(1000 + (index * 150), easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "emojiScale_$index"
    )

    Surface(
        modifier = Modifier
            .width(80.dp)
            .offset { androidx.compose.ui.unit.IntOffset(0, offsetY.toPx().toInt()) }
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(day.dayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D1100))
            Text(day.emoji, fontSize = 28.sp, modifier = Modifier.scale(emojiScale))
            Text(day.tempMax, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF7043))
            Text(day.tempMin, fontSize = 11.sp, color = Color(0xFF5D2000).copy(alpha = 0.6f))
        }
    }
}