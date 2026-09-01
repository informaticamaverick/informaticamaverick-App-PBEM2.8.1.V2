# Auditoría del Sistema de Chat Multi-Perfil (Maverick v8.6)

Se ha realizado una auditoría profunda de la arquitectura de chat, desde la generación de IDs hasta la visualización en la UI. A continuación los hallazgos críticos.

## 1. Hallazgos Críticos

### 🚨 Desincronización Pager vs ViewModel (El problema del "aparece y desaparece")
El componente [ListaElementosMoldeV2.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/ListaElementosMoldeV2.kt) utiliza un `HorizontalPager` para mostrar las pestañas de perfiles (Personal, Empresa A, etc.).
- **Error**: El `ChatScreen.kt` le pasa a este Pager una ÚNICA lista de hilos (`chattingThreads`) que ya viene filtrada por el ViewModel para el perfil global activo.
- **Efecto**: Cuando el usuario swipea o cambia de perfil, el ViewModel actualiza la lista global. Todas las páginas del Pager (incluyendo las que están en transición) pasan a mostrar los chats del nuevo perfil. Esto causa parpadeos, que los chats desaparezcan de la pestaña actual o aparezcan en la pestaña incorrecta temporalmente.

### 🧩 Debilidad en la Extracción de Contexto (ChatIdHelper)
Aunque [ChatIdHelper.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/ChatIdHelper.kt) v6.1 es robusto, depende de que el `chatId` tenga exactamente 4 partes (`uA_uB_bA_bB`).
- **Riesgo**: Si existen IDs antiguos (v4 o v5) con 3 o 6 partes, la extracción de `branchId` puede fallar, asignando el chat a una categoría errónea (ej: Personal en lugar de Empresa) y haciéndolo invisible bajo los filtros actuales.

### 🏷️ Etiquetado de Mensajes (Soberanía Local)
En [ChatMessageMapper.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/ChatMessageMapper.kt), se están usando los tags `localBranchId` y `localCompanyId`.
- **Hallazgo**: Estos tags son vitales para que Room sepa a qué pestaña pertenece el chat sin re-calcular el ID constantemente. Si un mensaje llega sin estos tags (de una versión vieja o de la web), el sistema intenta extraerlos del `chatId`. Si el `chatId` no sigue el formato esperado, el mensaje queda "huérfano" de contexto corporativo.

## 2. Respuesta a tus Consultas

### ¿Por qué no agregar la etiqueta del perfil a la cadena?
**Respuesta**: En realidad, **ya está ahí** en el formato `uA_uB_bA_bB`. El `bA` y `bB` son las "etiquetas" de sucursal.
- **Sugerencia de la Auditoría**: Para simplificar, podríamos mover la etiqueta de identidad del usuario local al PRINCIPIO del ID para que el filtrado en Room sea por prefijo (INDEXADO), lo cual es mucho más rápido que los JOINs actuales. Ejemplo: `[IDENTITY_ID]_[OTHER_UID]`. Sin embargo, esto rompería la simetría si no se hace con cuidado.
- **Decisión**: Mantendremos la estructura simétrica pero blindaremos la extracción.

### ¿imageURL y thumbnailBase64 en el Chat?
**Respuesta**: Tienes razón en tu sospecha. Actualmente `ChatMessageMapper` procesa estas imágenes, pero son para el **contenido del mensaje** (fotos enviadas), NO para el perfil.
- **Mejora**: Las fotos de perfil se cargan desde Room (`user_profile` / `provider_profile`). Eliminaremos cualquier intento de enviar metadatos de perfil dentro del mensaje de chat para ahorrar ancho de banda, confiando en que el "shallow search" ya pobló las tablas de Room.

## 3. Plan de Acción Inmediato

1.  **Refactorizar ChatListViewModel**: Entregar un `Map<String, List<ChatThread>>` para que el Pager de la UI pueda renderizar cada pestaña de forma independiente y estable.
2.  **Blindar ChatIdHelper**: Asegurar que la normalización de "none" a `null` sea atómica y que no haya fugas de contexto entre v4 y v6.
3.  **Sincronización de Identidad en Navegación**: Corregir todos los puntos de `navController.navigate` que olvidan pasar el `clientBranchId`.
4.  **Limpieza de Mappers**: Eliminar campos redundantes en el envío de mensajes que no pertenecen al contenido multimedia.

---
**Resultado Esperado**: Un chat que no "parpadea", donde los mensajes caen instantáneamente en la pestaña correcta y donde el consumo de datos es mínimo al no reenviar fotos de perfil en cada mensaje.
