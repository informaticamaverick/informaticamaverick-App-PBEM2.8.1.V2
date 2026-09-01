# 🟠 Recursos y Horarios: La Cocina Profesional (v2026.RESOURCES)

Este manual detalla la gestión del inventario de tiempo en la App Naranja, implementando el **Interruptor Táctico** y la jerarquía de recursos bajo el estándar de Clean Architecture.

---

## 1. El Interruptor Táctico (`estaHabilitado`)
El profesional puede "apagar" un recurso (empleado, cancha, consultorio) mediante un switch en la UI.
*   **Campo Técnico**: `RecursoEntity.estaHabilitado`.
*   **Comportamiento**: Un recurso apagado es filtrado automáticamente en las consultas de búsqueda del cliente (`WHERE estaHabilitado = 1`).
*   **Uso**: Pausas por mantenimiento, vacaciones de staff o indisponibilidad temporal sin pérdida de datos históricos.

## 2. Jerarquía de Horarios (Tipos de Tiempo)
Para evitar conflictos de agenda, Maverick diferencia la naturaleza del tiempo:
1.  **`Horario_Atencion`**: Marco legal de apertura del local. Es el límite máximo de todos los recursos.
2.  **`Horario_DisponibilidadTurnos / Horario_DisponibilidadVisitas`**: El turno específico de un activo. Solo se activa si el recurso/personal tiene marcado `requiereHorarioPropio` o es gestionado de forma independiente.

## 3. Equipo de Trabajo (Staff)
Toda mención a "Empleados" ha sido migrada a **`EquipoTrabajo`** para reflejar un modelo de colaboración profesional. 
*   **Entidad**: `EquipoTrabajoEntity`.
*   **Vinculación Técnica**: Mediante el campo `idRecursoVinculado`, un miembro del staff puede "ser" un recurso físico, permitiendo que su agenda se gestione de forma unificada.
*   **UI Masticada**: El sistema genera automáticamente iniciales y nombres completos mediante el `EquipoTrabajoMapper`.

## 4. Excepciones y Bloqueos (Vacaciones/Feriados)
Para gestionar la indisponibilidad temporal sin alterar el horario base, se utiliza la entidad de excepciones.
*   **Entidad**: `ExcepcionHorariaEntity`.
*   **Propósito**: Bloquear fechas específicas (ej: "Cerrado por reformas") o rangos parciales.
*   **Prioridad**: Una excepción de tipo `estaCerrado = true` anula cualquier disponibilidad del recurso para esa fecha.

## 5. Motor de Cálculo Privado
El cálculo de huecos libres mediante intersección de capas (Local ∩ Recurso - Turnos) ocurre exclusivamente en la App Naranja mediante la **`CalculadoraDisponibilidad`**. Los resultados se envían a la App Azul ya procesados para máxima eficiencia.

---
**Informática Maverick - Departamento de Taller Profesional (2026)**
