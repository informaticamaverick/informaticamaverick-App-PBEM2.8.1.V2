# Walkthrough - Refactor de Módulo de Chat

Se ha corregido el error de visualización de mensajes y se han integrado las **Reglas de Oro** y la **Política Zero Cost**.

## Cambios Principales

### 1. Solución de Mensajes No Mostrados
- Se detectó que `ChatViewModel` se inicializaba con IDs vacíos debido a limitaciones de `hiltViewModel` en sub-navegación.
- **Solución:** Se implementó el método `initialize(chatId)` en `ChatViewModel` y se invoca desde `ChatScreen` mediante un `LaunchedEffect`.

### 2. Política Zero Cost (Firebase Cleanup)
- Se integró el flujo de `markAsRead` en `ChatConversationScreen`.
- Al marcar un mensaje como leído, el `ChatRepository` elimina automáticamente el campo `content` (Base64) de Firebase RTDB, manteniendo el almacenamiento en **costo cero**.

### 3. Compresión Multimedia Exhaustiva
- **Imágenes:** Ahora pasan obligatoriamente por `ImageUtils.compressImageToWebP` antes de convertirse a Base64.
- **Audio:** Se mantiene la configuración de baja latencia (32kbps) para optimizar el consumo de datos.

### 4. Adherencia a las Reglas de Oro
- **Dumb Screens:** `ChatConversationContent` es puramente visual.
- **SSOT (Room):** La UI observa exclusivamente a Room; Firebase solo actúa como transporte.
- **BeBrain Sync:** Se garantiza `onRouteChanged` al entrar en la conversación para sincronizar el asistente Be.

## Verificación Realizada

- **Análisis de Código:** Se verificó la consistencia de tipos y el uso de `StateFlow`.
- **Flujo de Datos:** Se confirmó que `ChatScreen` genera el `chatId` correcto y lo pasa al ViewModel.
- **Multimedia:** Se validó la ruta de compresión en `ChatViewModel.sendImage`.

---

> [!NOTE]
> Las advertencias de parámetros no usados en `TenderSelectionDialog` se mantienen para preservar la firma de la función mientras se completa la implementación visual de ese diálogo específico en tareas futuras, siguiendo la regla de no modificar código ajeno al plan.
