
//-------------------------------------MAINSCREEN CONTROLA LOS GESTOS --------------------------------------



/**  package com.example.myapplication.Client

import com.example.myapplication.presentation.features.chat.*
import com.example.myapplication.presentation.features.calendar.*
import com.example.myapplication.presentation.features.profile.*
import com.example.myapplication.presentation.features.budget.*
import com.example.myapplication.presentation.features.auth.*

import com.example.myapplication.presentation.features.auth.*

import com.example.myapplication.presentation.features.home.*

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlin.math.sqrt

/**
 * MainScreen ahora es simplemente un HorizontalPager que muestra las diferentes pantallas principales.
 * No contiene lógica de navegación compleja, solo la interfaz de usuario para deslizar entre las pantallas.
 * @param navController El controlador de navegación para navegar a pantallas secundarias desde las pantallas principales.
 * @param pagerState El estado del paginador para controlar la página actual.
 * @param navItems La lista de pantallas principales para mostrar en el paginador.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavHostController, pagerState: PagerState, navItems: List<Screen>) {
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        when (navItems[page]) {
            Screen.Home -> HomeScreenCliente(navController = navController)
            Screen.Presupuestos -> PresupuestosScreen(onBack = { navController.popBackStack() })
            Screen.Chat -> ChatScreen(onBack = { navController.popBackStack() })
            Screen.Calendar -> CalendarScreen(onBack = { navController.popBackStack() })
            Screen.Promo -> PromoScreen(navController = navController, onBack = { navController.popBackStack() })
            else -> {}
        }
    }
}

/**
 * Una forma personalizada para la barra de navegación inferior que crea un recorte para el
 * Botón de Acción Flotante (FAB).
 * @param fabSize El tamaño del FAB.
 * @param fabPadding El espaciado alrededor del FAB.
 * @param xOffset El desplazamiento horizontal del recorte.
 */
class DynamicCutoutShape(
    private val fabSize: Dp,
    private val fabPadding: Dp,
    private val xOffset: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val fabSizePx = with(density) { fabSize.toPx() }
        val fabPaddingPx = with(density) { fabPadding.toPx() }
        val fabRadius = fabSizePx / 2f

        val cutoutRadius = fabRadius + fabPaddingPx
        val cutoutDiameter = cutoutRadius * 2f

        val path = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
        }

        val cutoutPath = Path().apply {
            val x = with(density) { xOffset.toPx() }
            
            // FIX: Se usa coerceAtLeast(0f) para evitar un valor negativo dentro de sqrt(),
            // lo que causaba el error "Invalid rectangle, make sure no value is NaN".
            val verticalOffset = fabRadius - sqrt((fabRadius * fabRadius - cutoutRadius * cutoutRadius).coerceAtLeast(0f))
            
            addArc(
                oval = Rect(
                    left = x - cutoutRadius,
                    top = -verticalOffset,
                    right = x + cutoutRadius,
                    bottom = cutoutDiameter - verticalOffset
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f
            )
        }
        
        return Outline.Generic(Path.combine(PathOperation.Difference, path, cutoutPath))
    }
}

/**
 * La barra de navegación inferior de la aplicación.
 * Muestra los iconos para las diferentes pantallas principales y un FAB para la pantalla seleccionada.
 * @param modifier El modificador para aplicar a la barra de navegación.
 * @param navController El controlador de navegación para cambiar entre pantallas.
 * @param allItems La lista de todas las pantallas principales.
 * @param selectedScreen La pantalla actualmente seleccionada.
 */
@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    allItems: List<Screen>,
    selectedScreen: Screen
) {
    val fabSize = 56.dp
    val fabPadding = 8.dp

    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(fabSize)) {
        val segmentWidth = constraints.maxWidth / allItems.size
        val selectedIndex = allItems.indexOf(selectedScreen)

        // Anima el desplazamiento horizontal del FAB cuando se selecciona una nueva pantalla.
        val fabXOffset by animateDpAsState(
            targetValue = with(LocalDensity.current) {
                (segmentWidth.toFloat() * selectedIndex + segmentWidth.toFloat() / 2).toDp()
            },
            label = "fabXOffsetAnimation"
        )

        // La barra de la aplicación inferior con el recorte para el FAB.
        BottomAppBar(
            modifier = Modifier.fillMaxWidth()
                .clip(DynamicCutoutShape(fabSize, fabPadding, fabXOffset)),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Muestra los iconos para todas las pantallas excepto la seleccionada.
                allItems.forEach { screen ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (screen != selectedScreen) {
                            IconButton(onClick = { navController.navigate(screen.route) }) {
                                Icon(
                                    imageVector = getUnselectedIconForScreen(screen),
                                    contentDescription = screen.title
                                )
                            }
                        }
                    }
                }
            }
        }
        // El Botón de Acción Flotante que muestra el emoji de la pantalla seleccionada.
        FloatingActionButton(
            onClick = { /* El FAB ya está seleccionado, no hace nada al hacer clic. */ },
            modifier = Modifier
                .offset(x = fabXOffset - (fabSize / 2)) // Centra el FAB en su segmento.
                .size(fabSize),
            shape = CircleShape,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(getEmojiForScreen(selectedScreen), fontSize = 24.sp)
        }
    }
}

/**
 * Devuelve el emoji para la pantalla dada.
 */
fun getEmojiForScreen(screen: Screen): String {
    return when (screen) {
        Screen.Home -> "🏠"
        Screen.Presupuestos -> "💰"
        Screen.Chat -> "💬"
        Screen.Calendar -> "📅"
        Screen.Promo -> "🔥"
        else -> ""
    }
}

/**
 * Devuelve el icono no seleccionado para la pantalla dada.
 */
fun getUnselectedIconForScreen(screen: Screen): ImageVector {
    return when (screen) {
        Screen.Home -> Icons.Outlined.Home
        Screen.Presupuestos -> Icons.Outlined.AttachMoney
        Screen.Chat -> Icons.Outlined.Chat
        Screen.Calendar -> Icons.Outlined.CalendarToday
        Screen.Promo -> Icons.Outlined.LocalFireDepartment
        else -> Icons.Outlined.Home // Icono por defecto.
    }
}

/**
 * Una vista previa para la barra de navegación inferior.
 */
@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreview() {
    MyApplicationTheme {
        val navController = rememberNavController()
        val navItems = listOf(
            Screen.Home,
            Screen.Presupuestos,
            Screen.Chat,
            Screen.Calendar,
            Screen.Promo
        )
        val selectedScreen = Screen.Promo

        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
            AppBottomNavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                navController = navController,
                allItems = navItems,
                selectedScreen = selectedScreen
            )
        }
    }
}
**/









