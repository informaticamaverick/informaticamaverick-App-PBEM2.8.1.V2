# 🏛️ Protocolo de Disponibilidad en Cascada (v2026.RESOURCES - SUPREME)

Este protocolo define las leyes de prioridad y la arquitectura técnica para el cálculo de tiempos en todo el ecosistema Informática Maverick. Implementa una separación estricta entre persistencia, dominio y visualización (Clean Architecture).

---

## ⚖️ Leyes de Prioridad (Soberanía Temporal)

1.  **Excepción > Todo**: Si existe una `ExcepcionHorariaEntity` (Feriado/Cierre), el tiempo queda bloqueado inmediatamente sin consultar otros niveles.
2.  **Recurso > Local**: El horario de disponibilidad del recurso tiene prioridad sobre el de atención general si `requiereHorarioPropio` es verdadero en `RecursoEntity`.
3.  **Evento > Disponibilidad**: Un turno ya agendado (`EventoMavEntity`) en estado CONFIRMADO o EN_PROCESO resta capacidad al bloque de tiempo correspondiente.

---

## 🏗️ Arquitectura de Capas (Elite Mappings)

Para cumplir con el blindaje de **Modo Release** y la **Ley #9 (Núcleo Atómico)**, los datos fluyen a través de traductores especializados:

### 1. Capa de Persistencia (Room)
*   **`HorarioEntity`**: Almacena los rangos crudos. Posee un `TipoHorario` (`Horario_Atencion`, `Horario_DisponibilidadTurnos` o `Horario_DisponibilidadVisitas`).
*   **`RecursoEntity`**: Representa el activo físico o humano. Incluye el interruptor táctico `estaHabilitado`.
*   **`ExcepcionHorariaEntity`**: Registra bloqueos privados (Feriados). **Privada para la App Naranja.**

### 2. Capa de Transformación (Mappers)
*   **`HorarioMapper`**: Traduce entre la entidad Room y el modelo de interfaz.
*   **`RecursoMapper`**: Transforma activos en datos "masticados".
*   **`EquipoTrabajoMapper`**: Gestiona la identidad del personal técnico.
*   **`EventoMapper`**: El Único Punto de Verdad (SSOT) para convertir eventos de agenda a la UI.

### 3. Capa de Interfaz (UiModels)
*   **`HorarioUiModel`**: Datos listos para calendarios. Contiene `RangoHorarioUiModel`.
*   **`RecursoUiModel`**: Activos con precios y capacidades formateados.
*   **`TurnoUiModel`**: Modelo para la fase de propuesta/invitación en el chat.
*   **`EventoUiModel`**: Representación visual de un turno confirmado.

---

## 🧹 Limpieza de Redundancias
Se han eliminado los siguientes archivos obsoletos para evitar confusión:
*   `DisponibilidadMavUtils.kt` (Lógica migrada al motor privado del Prestador).
*   `CalendarMapper.kt` (Lógica JSON eliminada en favor de Room-First).
*   Prefijos "Mav" eliminados de entidades de Recursos y Horarios.

---
**Informática Maverick - Arquitectura de Software 2026**
