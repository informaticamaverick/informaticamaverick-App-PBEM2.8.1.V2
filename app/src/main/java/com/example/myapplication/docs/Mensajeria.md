# 💬 MÓDULO: MENSAJERÍA Y COMUNICACIÓN (CHATS)

Gestión de chats en tiempo real, intercambio multimedia con compresión y estados online mediante una arquitectura híbrida Firestore/RTDB.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Screens & Components)
*   [`ChatScreen.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatScreen.kt): Orquestador de la lista de hilos de conversación.
*   [`ChatConversationScreen.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatConversationScreen.kt): Interfaz de la conversación activa.
*   [`ChatComponents.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatComponents.kt): Implementa las burbujas de mensaje por tipo (Texto, Imagen, Audio, Presupuesto, Ubicación).

### Lógica (Obreros & Datos)
*   [`ChatViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatViewModel.kt): **El Obrero**. Gestiona el estado de la conversación, carga de mensajes y lógica de grabación de audio.
*   [`ChatRepository.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/ChatRepository.kt): **El Sincronizador Maestro**. Orquesta Room, RTDB y Firestore.
*   [`ChatDao.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/local/dao/ChatDao.kt): Consultas SQLite para mensajes y resúmenes de hilos.

---

## 🔄 2. FLUJO DE DATOS: ENVÍO Y RECEPCIÓN

### A. Procedimiento de Envío (Paso a Paso)
1.  **Activación:** La Screen llama a `viewModel.sendMessage(text)`.
2.  **Preparación:** El Obrero construye la `MessageEntity` con un ID único (`UUID`).
3.  **Persistencia Local:** `repository.sendMessage(entity)` guarda el mensaje en **Room** inmediatamente (`chatDao.insertMessage`).
4.  **Sincronización Firestore:** Se actualiza el documento del chat en `chats/{chatId}` con la metadata (último mensaje, participantes, timestamp).
5.  **Sincronización Realtime (RTDB):** Se inserta el cuerpo del mensaje en `chats/{chatId}/messages/{messageId}`. Be Assistant del receptor detectará este nodo al instante.

### B. Procedimiento de Recepción (Realtime)
1.  **Escucha:** Al entrar al chat, se llama a `repository.startListening(chatId)`.
2.  **Listener:** Se registra un `ChildEventListener` en el nodo de **Realtime Database**.
3.  **Procesamiento:** 
    *   `onChildAdded`: Si el ID no existe en Room, se inserta la nueva entidad.
    *   `onChildChanged`: Se actualizan los estados de lectura (`isRead`) y entrega (`isDelivered`).
4.  **Notificación:** Si el mensaje llega mientras la app está en segundo plano o en otra pantalla, `notificationHelper` dispara una notificación push.

---

## 🖼️ 3. GESTIÓN MULTIMEDIA (Base64 y Compresión)

La aplicación utiliza una política de **Costo Cero** en multimedia, evitando el uso excesivo de Firebase Storage para mensajes efímeros.

### Imágenes
*   **Envío:** `sendImageMessage(uri)`.
    1.  `ImageUtils.compressImageToWebP`: Comprime la imagen a formato WebP (calidad 70%).
    2.  `ImageUtils.saveBytesToFile`: Guarda la copia comprimida en el almacenamiento privado de la app.
    3.  `ImageUtils.bytesToBase64`: Convierte la imagen comprimida a un String **Base64**.
    4.  **RTDB:** Se envía el String Base64 directamente en el campo `content`.
*   **Recepción:** `saveBase64ToFile`.
    1.  Se decodifica el Base64.
    2.  Se guarda como un archivo `.webp` local.
    3.  Room almacena la **Ruta Local** (`imageUrl`), nunca el Base64, para mantener la fluidez del scroll.

### Audios
*   **Envío:** `sendAudioMessage(localPath)`.
    1.  Se lee el archivo `.3gp` grabado por el Obrero.
    2.  Se convierte a Base64 y se envía a RTDB.
*   **Recepción:** Se decodifica y se guarda como archivo local, permitiendo la reproducción offline.

---

## 🛠️ 4. PROCEDIMIENTOS TÉCNICOS ESPECÍFICOS

### Cómo Eliminar Conversaciones
*   **Función:** `deleteChats(chatIds: List<String>)`
*   **Proceso:** 
    1.  `chatDao.deleteMessagesByChatIds(ids)` (Limpia Room).
    2.  `database.reference.child("chats").child(id).removeValue()` (Limpia RTDB).
    3.  `firestore.collection("chats").document(id).update("participants", FieldValue.arrayRemove(myUid))` (Salida silenciosa de Firestore).

### Cómo Marcar como Leído
*   **Función:** `markChatAsRead(chatId, myUserId)`
*   **Acción:** Actualiza Room localmente y envía un `setValue(true)` al campo `isRead` en el nodo del mensaje en RTDB.

---

## 💾 5. RELACIÓN CON FIREBASE Y ROOM

| Componente | Rol | Datos Almacenados |
| :--- | :--- | :--- |
| **Room** | SSOT (Fuente de Verdad) | Historial completo de mensajes, rutas locales a fotos/audios, estados de lectura. |
| **Realtime DB** | Transporte Rápido | Cuerpo de mensajes (Texto/Base64), estados de "Escribiendo..." y "Online". |
| **Firestore** | Metadata y Seguridad | Listas de participantes, permisos de chat, versiones de perfiles. |
| **Storage** | Multimedia Permanente | Fotos de perfil y banners (No se usa para mensajes de chat). |
