# Auditoría y Mejora del Calendario y Chat

## Fase 1: Auditoría y Research
- [x] Investigar implementación actual de Calendario en Prestador y Cliente.
- [x] Investigar flujo de datos de imágenes (Ley #8).
- [x] Investigar sincronización Chat-Calendario en el Core.
- [x] Identificar campos de datos faltantes (localidad, notas).

## Fase 2: Implementación en Core (SSOT)
- [ ] Agregar campos `locality` y `notes` a `MessageEntity`.
- [ ] Agregar campos `locality` y `notes` a `CalendarEventEntity`.
- [ ] Actualizar `CalendarRepository` para mapear nuevos campos.
- [ ] Actualizar `ChatMessageMapper` para sincronización con Firebase.

## Fase 3: Mejoras en Chat (Prestador)
- [ ] Actualizar `ScheduleAppointmentDialog` con campos de Dirección, Localidad y Detalle.
- [ ] Actualizar `ChatViewModel` para enviar estos datos en los mensajes de cita/visita.

## Fase 4: Mejoras en Calendario (Prestador)
- [ ] Rediseñar `AppointmentCard` en `PrestadorCalendarScreen` (Tarjeta Moderna Elite).
- [ ] Asegurar que la información del Cliente (Nombre/Foto) se muestre correctamente.

## Fase 5: Verificación
- [ ] Pruebas manuales de flujo completo (Creación -> Chat -> Calendario).
- [ ] Verificar auditoría de logs y limpieza de imágenes (Ley #8).
- [ ] Validar cumplimiento de las 7 Leyes Maverick Elite.
