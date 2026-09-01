# Protocolo Maverick Elite: Burbujas Interactivas Universales (v2026)

Este plan detalla la unificación y optimización de las burbujas de chat para turnos, citas y visitas técnicas, convirtiéndolas en componentes interactivos y dinámicos siguiendo las Leyes Elite.

## User Review Required

> [!IMPORTANT]
> Se propone centralizar toda la lógica visual de "Turnos/Citas" en un único componente base interactivo en `:ui-shared`. Las burbujas actuales (`ChatBubbleLocalAppointment`, `ChatBubbleTechnicalVisit`) se convertirán en variantes de este nuevo modelo.

## Proposed Changes

### 1. Módulo `:ui-shared` (Núcleo Visual)

#### [NEW] [ChatBubbleAppointmentElite.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/chat/ChatBubbleAppointmentElite.kt)
- Componente universal para: `VISIT`, `APPOINTMENT`, `CALENDAR_INVITE`.
- **Características**:
    - Emoji dinámico según categoría/tipo.
    - Colores temáticos diferenciados (Púrpura para Local, Cian para Técnica, Naranja para Calendario).
    - Soporte para **Estados Dinámicos**: `PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED`, `CANCELLED`.
    - Botones de acción inteligentes (solo visibles si no es `fromMe` y el estado es `PENDING`).
    - Click interactivo para abrir pantalla de detalles.

#### [ChatBubbleBase.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/chat/ChatBubbleBase.kt)
- Añadir parámetro `onClick` opcional para permitir interactividad en toda la superficie de la burbuja.

---

### 2. Módulo `:prestador` (Optimización de Flujo)

#### [ChatMessageComponents.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/chat/ChatMessageComponents.kt)
- Refactorizar `MessageBubble` para usar el nuevo `ChatBubbleAppointmentElite`.
- Implementar la apertura del `EliteAppointmentBookingDialog` o una nueva pantalla de detalles al tocar la burbuja.

---

### 3. Módulo `:app` (Cliente)

#### [ChatConversationScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatConversationScreen.kt)
- Actualizar el mapeo de mensajes para usar el componente Elite.
- Vincular el click de la burbuja con el `BookingDialog` (para abiertas) o `AppointmentDetailScreen` (para cerradas).

## Verification Plan

### Manual Verification
- **Inspección Visual**: Verificar que los colores y emojis cambien según el tipo de turno.
- **Flujo de Interacción**:
    1. Enviar turno cerrado -> Verificar botones Aceptar/Rechazar en la otra app.
    2. Enviar turno abierto -> Verificar botón "Ver Calendario" y apertura de diálogo.
    3. Tocar cuerpo de burbuja -> Verificar apertura de detalles.
- **Auditabilidad**: Los logs tácticos deben reflejar las transiciones de estado de la burbuja.
