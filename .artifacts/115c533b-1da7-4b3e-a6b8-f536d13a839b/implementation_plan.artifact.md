# Plan de Reparación Elite: Multimedia, Respuestas y Lectura v2026

Este plan corrige la desaparición de mensajes en la App Naranja, habilita la función de respuesta (Reply) en ambas aplicaciones y optimiza la purga multimedia para que ocurra solo después de la confirmación de lectura.

## User Review Required

> [!IMPORTANT]
> **Purga por Lectura:** Se desactivará la purga inmediata de imágenes y audios al recibirlos. Ahora, los archivos multimedia permanecerán en la nube hasta que el destinatario abra el chat y los marque como leídos. Esto garantiza que no se pierdan datos por problemas de conexión inicial.

> [!WARNING]
> **Activación de Respuestas (Reply):** Se habilitará el soporte técnico para que al responder un mensaje, el remitente y el contenido del mensaje original viajen integrados. Esto permitirá ver el globo de respuesta como en WhatsApp.

## Proposed Changes

### [Component] Core (Lógica de Tránsito)

#### [MODIFY] [ChatMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/ChatMavRepository.kt)
- **`enviarMensajeTexto`**: Aceptar `respondidoAId` y `respondidoAContenido`. Incluirlos en el mapa de Firebase.
- **`marcarComoLeido`**:
    1. Obtener mensajes multimedia no leídos de la otra parte desde Room.
    2. Ejecutar la purga en Firebase RTDB solo para esos IDs específicos.
- **`observarChat`**: Eliminar el bloque de purga automática en `onChildAdded`.

#### [MODIFY] [ChatMavDao.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/dao/ChatMavDao.kt)
- Añadir query `obtenerMultimediaNoLeidosDeOtro(idChat)` para facilitar la purga selectiva.

### [Component] App (Cliente - Respuestas)

#### [MODIFY] [ChatViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatViewModel.kt)
- Actualizar `enviarTextoMav` para usar `uiState.value.replyingToMessage`.
- Resetear el mensaje de respuesta tras el envío exitoso.

### [Component] Prestador (Naranja - Respuestas y Visibilidad)

#### [MODIFY] [PrestadorChatViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/chat/PrestadorChatViewModel.kt)
- Añadir `replyingToMessage` al estado.
- Implementar `setReplyMessage`.
- Actualizar `enviarTexto` para incluir datos de respuesta.
- **Garantizar Carga**: Asegurar que el flujo de mensajes no se vea interrumpido por la resolución de identidad.

#### [MODIFY] [ChatConversationScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/chat/ChatConversationScreen.kt)
- Conectar `mensajeRespuesta` y `alResponderMensaje` con el ViewModel.

## Verification Plan

### Manual Verification
1. **Respuestas:** En la App Azul, deslizar un mensaje para responder. Verificar que el mensaje enviado incluya la referencia al original.
2. **Multimedia:** Enviar una imagen desde la App Azul. Verificar que la App Naranja la reciba y muestre (antes se borraba demasiado rápido).
3. **Purga:** Verificar en Firebase Console que la imagen se borra del nodo `chats` solo después de que la App Naranja marca el chat como leído.
4. **Visibilidad:** Entrar al chat de la App Naranja y confirmar que la lista de mensajes aparece completa e inmediata.
