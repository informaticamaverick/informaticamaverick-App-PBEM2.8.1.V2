# 🗺️ MÓDULO: NAVEGACIÓN Y RUTAS TÁCTICAS

Este módulo centraliza la coreografía de navegación entre pantallas, el paso de argumentos y la sincronización del estado global del HUD.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### Orquestador de Navegación
*   [`AppNavigation.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/AppNavigation.kt): **El Mapa Maestro**. Define el `NavHost`, las rutas y las transiciones animadas entre pantallas.

### Modelos de Pantalla
*   [`Screen` (Sealed Class)](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/AppNavigation.kt): Define los objetos de ruta (`Home`, `Chat`, `Presupuestos`, etc.) y sus constantes de string.

---

## 🔄 2. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Registro de Rutas (Kotlin DSL)
La aplicación utiliza `Jetpack Navigation Compose` con rutas tipadas mediante parámetros opcionales en el string de ruta.

| Pantalla | Ruta Técnica | Parámetros | Acción |
| :--- | :--- | :--- | :--- |
| **Chat** | `"chat?providerId={pId}&companyId={cId}"` | `providerId`, `companyId`, `categoryId` | Abre una conversación específica o la lista general. |
| **Búsqueda** | `"result_busqueda/{category}"` | `category` (Obligatorio) | Muestra prestadores de un rubro seleccionado. |
| **Perfil** | `"perfil_prestador/{providerId}"` | `providerId` | Muestra el detalle técnico de un profesional. |

### B. Sincronización de HUD (Regla de Oro)
Cada vez que el usuario navega, se dispara un efecto secundario que informa al `BeBrainViewModel`.
*   **Función:** `onRouteChanged(route: String?)`
*   **Ubicación:** `BeBrainViewModel.kt`
*   **Detalle:** Esta función mapea la ruta de navegación a un `HUDContext`. Si entramos a `"chat"`, el Cerebro pone el contexto en `HUDContext.CHAT`, lo que automáticamente habilita/deshabilita la barra inferior y las herramientas de Be.

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo añadir una nueva Pantalla
1.  **Definir la Ruta:** Añadir un `object` a la clase sellada `Screen` en `AppNavigation.kt`.
2.  **Registrar Composable:** Añadir un bloque `composable(route = Screen.MiNuevaRuta.route)` dentro del `NavHost`.
3.  **Animaciones:** Configurar `enterTransition` y `exitTransition` usando `slideIntoContainer` para efectos de deslizamiento lateral.
4.  **Mapeo de Contexto:** Ir a `BeBrainViewModel.kt` -> `onRouteChanged` y asignar el `HUDContext` adecuado a la nueva ruta.

### Procedimiento para el Botón Atrás
*   **Sistema:** Se usa `navController.popBackStack()`.
*   **Chat:** En `ChatScreen`, se usa un `BackHandler` personalizado para asegurar que al volver desde una conversación se regrese primero a la lista de chats antes de salir del módulo.

---

## 💾 4. RELACIÓN CON EL MEDIADOR

*   **Paso de Intenciones:** Al navegar a una búsqueda (`result_busqueda`), la Screen lee el argumento de la ruta y llama a `coordinator.updateSearchQuery(category)` para que toda la app sepa qué se está buscando.
*   **Persistencia de Estado:** Se usa `saveState = true` y `restoreState = true` en el `AppBottomNavigationBar` para que al cambiar entre Home y Chat no se pierda el scroll o la posición de los Obreros.

---

## 🤖 5. NAVEGACIÓN DESDE EL ASISTENTE BE
*   Be Assistant puede disparar navegaciones directas mediante IDs de acción (ej: `"fast"` -> navega a `Screen.Fast`).
*   **Función:** El `LaunchedEffect` en `AppNavigationContent` escucha `beViewModel.actionEvent` y ejecuta `navController.navigate()`.
