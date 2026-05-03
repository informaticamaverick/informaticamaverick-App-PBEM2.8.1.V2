# Refactor de Módulo de Chat (Fix Visualización, Multimedia & Reglas de Oro)

Este plan corrige el error de mensajes no mostrados y asegura que el módulo siga la **Regla de Oro** (lógica en ViewModel, pantallas tontas) y la **Política Zero Cost** (Room como SSOT, limpieza de Firebase). Además, garantiza la compresión exhaustiva de multimedia.

## User Review Required

> [!IMPORTANT]
> El `ChatViewModel` ahora requerirá una inicialización explícita mediante `initialize(chatId: String)` ya que `hiltViewModel` con una `key` no siempre puebla el `SavedStateHandle` en navegaciones locales.

## Proposed Changes

### [Chat Component]

#### [ChatViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/ChatViewModel.kt)

- **Sincronización de Identidad:** Corregir la extracción de `chatId` y `receiverId`.
- **Inicialización Manual:** Implementar `initialize(chatId)` para arrancar la observación de Room y Typing Status de forma segura.
- **Compresión Multimedia:**
    - **Imágenes:** Usar `ImageUtils.compressImageToWebP` antes de generar el Base64.
    - **Audio:** Asegurar que el `MediaRecorder` use un bitrate optimizado (ya configurado en 32kbps).
- **Política Zero Cost:** Implementar `markAsRead()` para llamar a `repository.markChatAsRead()`, disparando la limpieza de Base64 en Firebase RTDB.
- **Sincronización Contextual:** Se mantiene el uso de `beBrainViewModel.onRouteChanged("chat_conversation")`.

#### [ChatScreen.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/ChatScreen.kt)

- **Orquestación:** Asegurar que al cambiar a `activeProviderId`, se genere el `chatId` y se llame a `viewModel.initialize(chatId)`.
- **Regla de Oro:** La pantalla solo "pinta" lo que recibe del `ChatViewModel` y del `BeBrainViewModel`.

#### [ChatConversationScreen.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/ChatConversationScreen.kt)

- **Lectura Automática:** Al abrir la conversación o recibir mensajes nuevos estando activos, llamar a `markAsRead` para sincronizar estados y limpiar Firebase.
- **Dumb UI:** Mantener `ChatConversationContent` como UI pura.

---

### [Data Layer]

#### [ChatRepository.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/ChatRepository.kt)

- (Verificación) Confirmado que `markChatAsRead` invoca `syncReadStatusToRTDB`, eliminando el campo `content` (Base64) tras la lectura exitosa.

## Verification Plan

### Manual Verification
1. **Flujo de Envío de Mensajes:**
    - Abrir un chat -> Enviar texto.
    - **Verificar:** Aparece inmediatamente en la burbuja (Room) y en la lista (preview).
2. **Compresión y Multimedia:**
    - Enviar imagen pesada.
    - **Verificar:** `ChatViewModel` loguea la compresión WebP.
    - **Verificar:** La imagen se ve nítida pero el tiempo de subida es mínimo.
3. **Política Zero Cost (ADB/Logcat):**
    - Recibir mensaje multimedia de otro dispositivo.
    - Abrir chat.
    - **Verificar Logs:** `ChatRepository: Limpieza: Contenido Base64 eliminado para [msgId]`.
4. **Reglas de Oro:**
    - Navegar entre lista y detalle.
    - **Verificar:** La barra inferior se oculta/muestra correctamente vía `BeBrainViewModel`.
    - **Verificar:** El Asistente Be reacciona al cambio de ruta a `chat_conversation`.
