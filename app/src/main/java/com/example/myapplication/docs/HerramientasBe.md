# 🛠️ MÓDULO: HERRAMIENTAS DEL ASISTENTE BE (Toolbox)

Este módulo gestiona el sistema dinámico de "Pequeñas Acciones" que aparecen al presionar prolongadamente al asistente Be.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Toolbox & Buttons)
*   [`BeBuild.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/BeBuild.kt): Implementa `BeActionsBar`, `BeSmallActionsBuilder` y el botón `BeSmallActionButton`.
*   [`BeAssistant.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/BeAssistant.kt): Contenedor animado que despliega la barra sobre la Nav Bar.

### Lógica (Inyección de Estado)
*   [`BeBrainViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/BeBrainViewModel.kt): Sector 6. Gestiona la lista activa de acciones y la multiselección.
*   [`BeSmallActionModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/BeBuild.kt): Modelo de datos para las herramientas.

---

## 🔄 2. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Inyección de Herramientas (BeBrainViewModel)
| Función | Recibe | Entrega | Acción |
| :--- | :--- | :--- | :--- |
| `setCustomActions()` | `List<BeSmallActionModel>` | `Unit` | Reemplaza las acciones actuales por las del Obrero de pantalla. |
| `updateActionsForContext()` | `HUDContext` | `Unit` | **Privada:** Define las acciones base (ej: "Simular Chat", "Favoritos"). |
| `triggerAction()` | `actionId: String` | `Unit` | Emite el ID al `Mediador` para que la Screen lo capture. |

### B. Sistema de Multiselección
*   **Función:** `syncMultiSelection(active, selectedIds)`
*   **Lógica:** Si la multiselección está activa, Be cambia su barra de herramientas por defecto a herramientas de edición (Cerrar, Seleccionar Todo, Eliminar).
*   **Kotlin:**
    ```kotlin
    if (_isMultiSelectionActive.value) {
        actions.add(BeSmallActionModel("delete_multi", Icons.Default.Delete, "ELIMINAR", tint = Color.Red))
    }
    ```

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo crear una nueva Herramienta para una Pantalla
1.  **En el Obrero (ViewModel):** Definir una lista de acciones.
    ```kotlin
    val actions = listOf(BeSmallActionModel(id = "mi_accion", icon = Icons.Default.Star, label = "Estrella"))
    ```
2.  **En la Screen:** Inyectar al entrar.
    ```kotlin
    LaunchedEffect(Unit) { beViewModel.setCustomActions(misAcciones) }
    ```
3.  **Captura:** Escuchar el click en la Screen.
    ```kotlin
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { if(it == "mi_accion") miLogica() }
    }
    ```

### Cómo modificar el diseño de la Barra
1.  Ir a `BeBuild.kt` -> `BeActionsBar`.
2.  **Animación:** La barra usa un `AnimatedContent` basado en la `toolboxKey`. Cambiar esta key en el ViewModel fuerza un refresco visual con transición `slideIn`.

---

## 💾 4. RELACIÓN CON EL HUD
*   **Posicionamiento:** La barra se dibuja siempre pegada al borde derecho (`Alignment.CenterEnd`).
*   **Visibilidad:** Controlada por `showBeTools`. Se oculta automáticamente si Be se "duerme" o se inicia una búsqueda.
*   **Costo Cero:** El sistema de herramientas no realiza peticiones de red; los iconos y acciones son definidos estáticamente en Kotlin.
