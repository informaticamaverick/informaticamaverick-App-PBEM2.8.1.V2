# Auditoría y Mejora del Calendario y Chat (Prestador)

Este plan aborda la auditoría del flujo de datos de calendario, imágenes y chat, y propone mejoras para cumplir con las Leyes Maverick Elite, incluyendo una nueva "Tarjeta Moderna" y campos de datos faltantes (localidad, detalle).

## User Review Required

- **Migración de Datos**: Se agregarán campos `locality` y `notes` a las entidades de Room. Se requiere verificar si el esquema de la base de datos permite migraciones automáticas o si se debe incrementar la versión.
- **UI del Calendario**: La nueva tarjeta será más densa en información. Se debe confirmar si se prefiere mantener el estilo actual o migrar totalmente al estilo "Elite" (Cyberpunk/Premium) del cliente.

## Proposed Changes

### 🛠️ Módulo Core (SSOT & Data Flow)

Se ampliarán las entidades y el repositorio para capturar más contexto del servicio.

#### [MessageEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/MessageEntity.kt)

- Agregar campos `locality: String?` y `appointmentNotes: String?` (o similar) para persistir el detalle del problema y la ubicación específica.

#### [CalendarEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/CalendarEntity.kt)

- Agregar campos `locality: String?` y `notes: String?` a `CalendarEventEntity`.

#### [CalendarRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/CalendarRepository.kt)

- Actualizar `saveSmartEventFromMessage` para mapear los nuevos campos desde `MessageEntity`.
- **Auditoría de Identidad**: Ajustar la lógica para que en la App del Prestador, el campo `provider` guarde el nombre del **Cliente** y `providerPhotoUrl` guarde su foto/avatar, permitiendo una visualización correcta en el calendario.

#### [ChatMessageMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/ChatMessageMapper.kt)

- Actualizar `fromDataSnapshot` para extraer `locality` y `appointmentNotes` (o similares) de Firebase Realtime Database.

---

### 📱 Módulo Prestador (UI & Business Logic)

Se mejorará la creación de eventos y su visualización.

#### [ScheduleAppointmentDialog.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/chat/ChatDialogs.kt) (Función `ScheduleAppointmentDialog`)

- Agregar campos de entrada para:
    - **Detalle del Problema** (Notas).
    - **Dirección y Localidad** (con sugerencias basadas en las ubicaciones compartidas recientemente por el cliente en el chat).

#### [ChatViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/chat/ChatViewModel.kt)

- Actualizar `sendAppointmentElite` y `sendAppointment` para incluir los nuevos campos en el `MessageEntity` enviado.
- Implementar la lógica de "Auto-completado" de dirección si el cliente compartió su ubicación.

#### [PrestadorCalendarScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/calendar/PrestadorCalendarScreen.kt)

- **Rediseño de `AppointmentCard`**: Implementar una tarjeta moderna basada en el diseño "Elite" del cliente.
    - Mostrar Categoría con Icono/Emoji.
    - Mostrar Título del Servicio y Detalle (Notas).
    - Mostrar Dirección y Localidad de forma clara.
    - Mostrar Avatar del Cliente con estado online (si está disponible).
    - Botones de acción rápida mejorados.

---

## Verification Plan

### Automated Tests
- Ejecutar `core/src/test/java/com/example/myapplication/core/utils/CalendarUtilsTest.kt` para asegurar que el parsing de fechas siga funcionando.
- Crear un nuevo test unitario en `CalendarRepositoryTest` (nuevo) para verificar el mapeo de `MessageEntity` a `CalendarEventEntity` con los nuevos campos.

### Manual Verification
1.  **Flujo de Creación**:
    - Abrir chat con un cliente.
    - Enviar una "Visita Técnica" completando Dirección, Localidad y Detalle.
    - Verificar que la burbuja de chat muestre estos datos.
2.  **Sincronización de Calendario**:
    - Abrir el Calendario del Prestador.
    - Verificar que aparezca la nueva tarjeta con todos los campos.
    - Verificar que el nombre y la foto correspondan al cliente, no al prestador.
3.  **Auditoría de Imagen**:
    - Enviar una imagen desde el cliente al prestador.
    - Verificar en Logcat el tag `[EPHEMERAL_CLEANUP]` que indica la eliminación de la nube tras la persistencia local.
4.  **Confirmación de Eventos**:
    - Cambiar el estado del evento (Aceptar/Cancelar) desde el calendario.
    - Verificar que se envíe el mensaje de sistema correspondiente al chat.
