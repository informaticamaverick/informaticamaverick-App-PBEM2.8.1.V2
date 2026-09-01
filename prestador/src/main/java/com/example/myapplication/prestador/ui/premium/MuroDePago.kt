package com.example.myapplication.prestador.ui.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 💎 [MURO DE PAGO ELITE - V3 ULTRA CONVERSIÓN]
 * Diseño publicitario Full HD. Incorpora Canvas de Precio y Botón Fijo (Sticky CTA).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuroDePago(
    onBack: () -> Unit,
    onSubscribeClick: () -> Unit,
    onSimulateClick: () -> Unit = {}
) {
    // Manteniendo la integridad de tus variables originales
    val gold = Color(0xFFFFD700)
    val maverickOrange = Color(0xFFF97316)
    val surfaceDark = Color(0xFF020617) // Más oscuro para mayor contraste OLED

    // Colores derivados para la UI premium
    val textMuted = Color.White.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceDark)
    ) {
        // --- LUCES AMBIENTALES (Background Glows) ---
        // Luz Naranja Superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(maverickOrange.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(800f, -100f),
                        radius = 1000f
                    )
                )
        )
        // Luz Dorada Central/Inferior
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(gold.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(-100f, 1200f),
                        radius = 900f
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent, // Permite ver las luces de fondo
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        // Etiqueta superior estilo píldora
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(
                                "MEMBRESÍA ELITE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp,
                                color = gold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White.copy(alpha = 0.5f))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            // --- STICKY CTA (Botón flotante siempre visible) ---
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Gradiente difuminado hacia arriba para separar el botón del contenido
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, surfaceDark, surfaceDark),
                                startY = 0f,
                                endY = 150f
                            )
                        )
                        .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = onSubscribeClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp),
                            contentPadding = PaddingValues(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(maverickOrange, Color(0xFFEA580C))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "OBTENER ELITE AHORA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Facturado de forma segura por Google Play",
                            fontSize = 11.sp,
                            color = textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- HERO TEXT PUBLICITARIO ---
                Spacer(Modifier.height(8.dp))
                Text(
                    "DOMINA TU\nMERCADO HOY",
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Únete al 5% de profesionales que acaparan los mejores clientes.",
                    fontSize = 15.sp,
                    color = textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(32.dp))

                // --- CANVAS DE PRECIO PROMOCIONAL ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        // Borde dorado transparente para destacar el cuadro
                        .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.02f))
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Etiqueta Oferta
                        Text(
                            "PLAN ILIMITADO",
                            color = textMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        // Fila de precios (Tachado + Ahorro)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$999",
                                color = Color.Gray,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(Modifier.width(12.dp))
                            Surface(
                                color = Color(0xFF22C55E).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    "AHORRAS 60%",
                                    color = Color(0xFF4ADE80),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Precio Principal Promocional
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                "$",
                                color = gold,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(bottom = 6.dp, end = 4.dp)
                            )
                            Text(
                                "XXX", // PRECIO SOLICITADO
                                color = Color.White,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-2).sp
                            )
                            Text(
                                "/mes",
                                color = textMuted,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Cancela cuando quieras. Sin compromisos.",
                            color = textMuted.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    // Cinta roja absoluta (Oferta Lanzamiento)
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text(
                            "OFERTA DE LANZAMIENTO",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    "TODO INCLUIDO:",
                    color = textMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(Modifier.height(16.dp))

                // --- LISTA DE BENEFICIOS (Rediseñada para agilidad visual) ---
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    BeneficioItemModerno(
                        title = "Licitaciones Ilimitadas",
                        desc = "Acceso VIP a concursos públicos y privados antes que la competencia."
                    )
                    BeneficioItemModerno(
                        title = "Top en Búsquedas",
                        desc = "Aparece siempre primero cuando los clientes busquen tu servicio."
                    )
                    BeneficioItemModerno(
                        title = "Insignia Maverick Pro",
                        desc = "Sello de verificación que aumenta la confianza y multiplica cierres."
                    )
                }

                Spacer(Modifier.height(32.dp))

                // 🔥 [DEBUG] Botón Provisional para Simular Pago
                OutlinedButton(
                    onClick = onSimulateClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.BugReport, null, modifier = Modifier.size(16.dp), tint = Color.Green)
                    Spacer(Modifier.width(8.dp))
                    Text("SIMULAR PAGO EXITOSO (DEBUG)", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Espacio extra al final para que el scroll pase por encima del bottomBar fluidamente
                Spacer(Modifier.height(140.dp))
            }
        }
    }
}

@Composable
private fun BeneficioItemModerno(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        // Ícono de Check Premium y pequeño
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(28.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFF97316))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Black
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                desc,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020617)
@Composable
fun MuroDePagoPreview() {
    MuroDePago(
        onBack = {},
        onSubscribeClick = {}
    )
}


/*
package com.example.myapplication.prestador.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
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

/**
 * 💎 [MURO DE PAGO ELITE] - Pantalla de Conversión Premium
 * [ELITE v2026.FINAL]: Diseño inmersivo Material 3 con gradientes tácticos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuroDePago(
    onBack: () -> Unit,
    onSubscribeClick: () -> Unit
) {
    val gold = Color(0xFFFFD700)
    val maverickOrange = Color(0xFFF97316)
    val surfaceDark = Color(0xFF0F172A)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MEMBRESÍA ELITE", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceDark,
                    titleContentColor = gold
                )
            )
        },
        containerColor = surfaceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(
                    Brush.verticalGradient(
                        listOf(surfaceDark, Color(0xFF1E293B))
                    )
                )
        ) {
            // --- HEADER VISUAL ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Decoración de fondo
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(gold.copy(alpha = 0.05f), CircleShape)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = gold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "LLEVA TU NEGOCIO AL\nPRÓXIMO NIVEL",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }

            // --- LISTA DE BENEFICIOS ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BeneficioItem(
                    icon = Icons.Default.Gavel,
                    title = "Participación en Concursos",
                    desc = "Responde a licitaciones públicas y privadas en tu zona de forma ilimitada."
                )
                BeneficioItem(
                    icon = Icons.Default.Campaign,
                    title = "Promociones y Ofertas",
                    desc = "Publica historias y promociones que aparecerán primero en la app del cliente."
                )
                BeneficioItem(
                    icon = Icons.Default.Verified,
                    title = "Insignia Verificada",
                    desc = "Tu perfil mostrará el sello de confianza Maverick, aumentando tus ventas."
                )
                BeneficioItem(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    title = "Posicionamiento Prioritario",
                    desc = "Aparece en los primeros resultados de búsqueda de tu rubro."
                )
            }

            Spacer(Modifier.height(40.dp))

            // --- CTA & PRICING ---
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Suscripción Mensual Auto-renovable",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        color = gold.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    "Gestionado a través de Google Play",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onSubscribeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = maverickOrange),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        "COMENZAR AHORA",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(onClick = onBack) {
                    Text("Quizás más tarde", color = Color.White.copy(alpha = 0.5f))
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun BeneficioItem(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = Color(0xFFF97316).copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = Color(0xFFF97316)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                desc,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MuroDePagoPreview() {
    MuroDePago(
        onBack = {},
        onSubscribeClick = {}
    )
}
*/












































