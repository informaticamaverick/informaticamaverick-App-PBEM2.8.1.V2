# Tareas: Unificación y Optimización de Chat Maverick Elite

## Investigación y Planeamiento
- [x] Investigar componentes de chat en `prestador`, `app` y `ui-shared`
- [x] Crear plan de implementación inicial
- [x] Investigar diseños modernos (Telegram 2026) y flujos interactivos de turnos
- [x] Crear plan de unificación de burbujas de turnos

## Ejecución: Módulo UI-SHARED (Núcleo)
- [ ] Crear `ChatBubbleAppointmentElite.kt`
    - [ ] Implementar diseño base Telegram 2026 (Glassmorphism/Glow)
    - [ ] Soporte para tipos: Local, Técnica, Calendario, Presupuesto
    - [ ] Lógica de botones interactivos según estado y rol
- [ ] Actualizar `ChatBubbleBase.kt` con soporte `onClick`

## Ejecución: Módulo Prestador
- [ ] Refactorizar `MessageBubble` en `ChatMessageComponents.kt`
- [ ] Vincular clicks de burbujas con diálogos/pantallas de detalle
- [ ] Auditoría de logs tácticos en transiciones de turnos

## Ejecución: Módulo App (Cliente)
- [ ] Refactorizar mapeo de burbujas en `ChatConversationScreen.kt`
- [ ] Sincronizar interacciones de turnos abiertos/cerrados

## Verificación Final
- [ ] Prueba de paridad visual 2026
- [ ] Verificación de flujos Aceptar/Rechazar y Selección de Turno Abierto
- [ ] Auditoría de logs [CALENDAR_SYNC]
