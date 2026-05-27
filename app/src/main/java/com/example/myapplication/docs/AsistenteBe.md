# 🤖 MÓDULO: ASISTENTE BE (EL CEREBRO VISUAL)

Este documento detalla el funcionamiento técnico de la burbuja inteligente, su sistema de emociones y la orquestación del HUD.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Burbuja & Diálogos)
*   [`BeAssistant.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/BeAssistant.kt): **Orquestador FAB**. Implementa el Canvas del asistente y la barra de búsqueda.
*   [`BeBubbleComic.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/BeBubbleComic.kt): Implementa `BeBottomBubble` y `BeTopBubble` con estética de cómic.

### Lógica (Cerebro & Coreógrafo)
*   [`BeBrainViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/BeBrainViewModel.kt): **El Cerebro**. Define *qué* dice Be y *cómo* se siente (SSOT de mensajes).
*   [`BeAssistantViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/BeAssistantViewModel.kt): **El Coreógrafo**. Gestiona la posición física (X, Y) y el arrastre.

---

## 🔄 2. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Estados de Be (BeBrainViewModel)
| Estado (`BeState`) | Descripción | Acción Visual |
| :--- | :--- | :--- |
| `IDLE` | Reposo absoluto. | Be flota suavemente. Ojos parpadean. |
| `NOTIFICATION_READY` | Hay un consejo nuevo. | Aparece el Badge (Corazón/Emoji) vibrando. |
| `TALKING` | Mostrando burbuja. | Se despliega `BeBottomBubble` con texto y botones. |

### B. Sistema de Emociones (`BeEmotion`)
*   **Función:** `Canvas.drawCircle/drawOval` en `BeAssistantSearchFab`.
*   **Lógica:** La firma visual de los ojos cambia según el enum:
    *   `HAPPY`: Arcos hacia arriba (`drawPath` con `quadraticTo`).
    *   `ANGRY`: Cejas inclinadas hacia el centro (`drawLine`).
    *   `SURPRISED`: Pupilas pequeñas y ojos circulares.
    *   `SLEEPING`: Ojos como líneas horizontales (`eyeScaleY = 0.1f`).

### C. Ciclo de Vida del Consejo
1.  `startBeBrainLoop()`: Inicia un `Job` que espera entre 6 y 15 segundos.
2.  `updateBeContextMessages(route)`: Filtra mensajes del `BeDictionary` según la pantalla actual.
3.  `nextTip()`: Cambia el `currentTipIndex` para rotar el mensaje visible.

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo añadir una nueva frase al Asistente
1.  **Diccionario:** Ir a `BeDictionary.kt`.
2.  **Lista:** Añadir un objeto `BeMessage` a la lista correspondiente (ej: `HomeMessages`).
3.  **Kotlin:**
    ```kotlin
    BeMessage(icon = "💡", text = "¡Prueba el nuevo modo oscuro!", bubbleColor = Color.Cyan)
    ```

### Cómo reaccionar a Gestos (Táctico)
*   **Tap Simple:** Ejecuta `onBeClick()`. Si Be está dormido, lo despierta. Si está despierto, activa/desactiva la búsqueda.
*   **Double Tap:** Ejecuta `onBeDoubleClick()`. Alterna entre modo `IDLE` y `SLEEPING` (Hibernación estilo WhatsApp).
*   **Long Press:** Ejecuta `onBeLongClick()`. Despliega el panel de herramientas (`showBeTools`).

---

## 💾 4. SINCRONIZACIÓN CON EL COORDINADOR

*   **Búsqueda:** Al escribir en la `SearchBarComponent`, se llama a `coordinator.updateSearchQuery(it)`.
*   **Resultados:** El `Cerebro` observa `coordinator.hasMatches`. Si es `false`, Be cambia automáticamente a `BeEmotion.SAD`.
*   **Posición:** `BeAssistantViewModel` usa `Animatable` para que el regreso a la "posición de descanso" ( resting position) sea fluido tras cerrar el teclado.
