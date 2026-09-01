package com.example.myapplication.ui.componentes.sistema

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * MODIFICADOR SHIMMER (EFECTO SKELETON)
 * Crea un gradiente animado que simula la carga de contenido.
 */
fun Modifier.efectoShimmer(): Modifier = composed {
    val transicion = rememberInfiniteTransition(label = "shimmer")
    val animacionTraslacion by transicion.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val coloresShimmer = listOf(
        Color.White.copy(alpha = 0.02f),
        Color.White.copy(alpha = 0.12f),
        Color.White.copy(alpha = 0.02f),
    )

    val pincel = Brush.linearGradient(
        colors = coloresShimmer,
        start = Offset(animacionTraslacion - 200f, animacionTraslacion - 200f),
        end = Offset(animacionTraslacion, animacionTraslacion)
    )

    background(pincel)
}

/**
 * SKELETON PARA TARJETA DE NEGOCIO (PRESTADOR BUSINESS CARD)
 */
@Composable
fun ShimmerPrestadorBusinessCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .padding(4.dp),
        color = Color(0xFF0F1520),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxSize().efectoShimmer()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = androidx.compose.ui.Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.05f), CircleShape))
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.width(30.dp).height(10.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                        Box(modifier = Modifier.fillMaxWidth(0.4f).height(10.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp)))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(6) { Box(modifier = Modifier.size(20.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))) }
                }
            }
        }
    }
}

/**
 * SKELETON PARA ELEMENTO DE LISTA DE CHAT (Bandeja de Entrada)
 */
@Composable
fun ShimmerElementoChat() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).efectoShimmer())
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).clip(RoundedCornerShape(2.dp)).efectoShimmer())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(10.dp).clip(RoundedCornerShape(2.dp)).efectoShimmer())
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Box(modifier = Modifier.width(30.dp).height(10.dp).clip(RoundedCornerShape(2.dp)).efectoShimmer())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).efectoShimmer())
        }
    }
}

/**
 * SKELETON PARA CABECERA DE CONVERSACIÓN
 */
@Composable
fun ShimmerCabeceraChat() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).efectoShimmer())
        Spacer(Modifier.width(12.dp))
        Column {
            Box(modifier = Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).efectoShimmer())
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.width(60.dp).height(10.dp).clip(RoundedCornerShape(2.dp)).efectoShimmer())
        }
    }
}

/**
 * SKELETON PARA BURBUJA DE MENSAJE
 */
@Composable
fun ShimmerBurbujaChat(esMio: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .width(if (esMio) 200.dp else 240.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(
                    topStart = 12.dp, topEnd = 12.dp,
                    bottomStart = if (esMio) 12.dp else 2.dp,
                    bottomEnd = if (esMio) 2.dp else 12.dp
                ))
                .efectoShimmer()
        )
    }
}

/**
 * GRILLA DE SKELETONS PARA CONVERSACIÓN
 */
@Composable
fun ListaShimmerChat() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(8) { index ->
            ShimmerBurbujaChat(esMio = index % 3 == 0)
        }
    }
}

/**
 * GRILLA DE SKELETONS PARA RESULTADOS DE BÚSQUEDA
 */
@Composable
fun GrillaShimmerPrestadores(cantidad: Int = 6) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        val filas = (cantidad + 1) / 2
        repeat(filas) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) { ShimmerPrestadorBusinessCard() }
                Box(modifier = Modifier.weight(1f)) { ShimmerPrestadorBusinessCard() }
            }
        }
    }
}

/**
 * GRILLA DE SKELETONS PARA LA BANDEJA DE ENTRADA
 */
@Composable
fun ListaShimmerInbox() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(10) { ShimmerElementoChat() }
    }
}

/**
 * SKELETON PARA TARJETA DE CATEGORÍA
 */
@Composable
fun ShimmerTarjetaCategoria() {
    Surface(modifier = Modifier.fillMaxWidth().height(195.dp).clip(RoundedCornerShape(12.dp)), color = Color(0xFF1A1F26).copy(alpha = 0.5f)) {
        Box(modifier = Modifier.fillMaxSize().efectoShimmer())
    }
}

/**
 * SKELETON PARA TARJETA BENTO DE SUPERCATEGORÍA
 */
@Composable
fun ShimmerTarjetaSuperCategoria() {
    Surface(modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(22.dp)), color = Color(0xFF1A1F26).copy(alpha = 0.5f)) {
        Box(modifier = Modifier.fillMaxSize().efectoShimmer())
    }
}

/**
 * SKELETON PARA TARJETA DE CATEGORÍA TÁCTICA (Radar Urgencias)
 */
@Composable
fun ShimmerTarjetaCategoriaTactica() {
    Surface(
        modifier = Modifier
            .width(110.dp)
            .height(115.dp)
            .padding(2.dp),
        color = Color(0xFF1A1F26).copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize().efectoShimmer())
    }
}

/**
 * SKELETON PARA CARPETA DE CONCURSO
 */
@Composable
fun EsqueletoConcurso() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(280.dp).padding(vertical = 8.dp, horizontal = 12.dp),
        color = Color(0xFF161C24).copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxSize().efectoShimmer()) {
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).background(Color.White.copy(alpha = 0.05f)))
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Box(modifier = Modifier.width(150.dp).height(10.dp).background(Color.White.copy(alpha = 0.05f)))
                Spacer(Modifier.height(12.dp)); Box(modifier = Modifier.fillMaxWidth().height(24.dp).background(Color.White.copy(alpha = 0.05f)))
                Spacer(Modifier.height(8.dp)); Box(modifier = Modifier.width(200.dp).height(24.dp).background(Color.White.copy(alpha = 0.05f)))
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.width(80.dp).height(12.dp).background(Color.White.copy(alpha = 0.05f)))
                    Box(modifier = Modifier.width(80.dp).height(12.dp).background(Color.White.copy(alpha = 0.05f)))
                }
            }
        }
    }
}
