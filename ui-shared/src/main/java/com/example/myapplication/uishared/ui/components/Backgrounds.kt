package com.example.myapplication.uishared.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.cos

/**
 * --- TACTICAL CUBE BACKGROUND ---
 * Dibuja un fondo con un patrón de cubos 3D (hexágonos con líneas internas) optimizado.
 * Reemplaza texturas pesadas por dibujo procedural en tiempo real.
 */
@Composable
fun TacticalCubeBackground(
    modifier: Modifier = Modifier,
    cubeSize: Float = 75f, // Tamaño del radio del hexágono (ajustable)
    strokeColor: Color = Color(0xFF1E293B).copy(alpha = 0.4f), // Color sutil de las líneas
    strokeWidth: Float = 2f,
    backgroundColor: Color = Color(0xFF050508),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            
            // Cálculos trigonométricos para el hexágono
            val angle30 = Math.toRadians(30.0)
            val wHalf = (cubeSize * cos(angle30)).toFloat()
            
            // Cantidad de columnas y filas necesarias para cubrir la pantalla
            val cols = (size.width / (wHalf * 2)).toInt() + 2
            val rows = (size.height / (cubeSize * 1.5f)).toInt() + 2
            
            for (r in -1..rows) {
                for (c in -1..cols) {
                    // Centro de cada hexágono (desfase en filas impares para encastrar)
                    val cx = c * (wHalf * 2) + if (r % 2 != 0) wHalf else 0f
                    val cy = r * (cubeSize * 1.5f)
                    
                    // 6 Vértices externos del hexágono
                    val top = Offset(cx, cy - cubeSize)
                    val topRight = Offset(cx + wHalf, cy - cubeSize / 2)
                    val bottomRight = Offset(cx + wHalf, cy + cubeSize / 2)
                    val bottom = Offset(cx, cy + cubeSize)
                    val bottomLeft = Offset(cx - wHalf, cy + cubeSize / 2)
                    val topLeft = Offset(cx - wHalf, cy - cubeSize / 2)
                    val center = Offset(cx, cy)
                    
                    // Dibujar el contorno del hexágono
                    path.moveTo(top.x, top.y)
                    path.lineTo(topRight.x, topRight.y)
                    path.lineTo(bottomRight.x, bottomRight.y)
                    path.lineTo(bottom.x, bottom.y)
                    path.lineTo(bottomLeft.x, bottomLeft.y)
                    path.lineTo(topLeft.x, topLeft.y)
                    path.close()
                    
                    // Trazar las 3 líneas internas para formar el cubo 3D (Forma de "Y" invertida)
                    // Línea hacia abajo
                    path.moveTo(center.x, center.y)
                    path.lineTo(bottom.x, bottom.y)
                    
                    // Línea hacia arriba-derecha
                    path.moveTo(center.x, center.y)
                    path.lineTo(topRight.x, topRight.y)
                    
                    // Línea hacia arriba-izquierda
                    path.moveTo(center.x, center.y)
                    path.lineTo(topLeft.x, topLeft.y)
                }
            }
            
            // Dibujar todo el patrón en una sola pasada para máximo rendimiento
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = strokeWidth)
            )
        }
        
        // Contenido superior (Tu Bottom Sheet u otras vistas)
        content()
    }
}

// ==================================================================================
// --- 🧪 SECCIÓN DE PREVIEWS ---
// ==================================================================================

@Preview(name = "Tactical Cube Background", showBackground = true)
@Composable
fun PreviewTacticalCubeBackground() {
    TacticalCubeBackground {
        // Ejemplo de contenido
    }
}
