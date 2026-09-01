# Auditoría de Arquitectura: Unificación de Cabeceras de Lista (v2026.ELITE)

He analizado las diferencias entre las cabeceras de `Presupuestos`, `Chats`, `Calendario` y `Home`. A continuación, el detalle técnico de por qué son distintas y el plan para estandarizarlas.

## 1. Análisis de Inconsistencias

| Pantalla | Componente Base | Estado de Estandarización | Observación |
| :--- | :--- | :--- | :--- |
| **Presupuestos** | `ArmadorListaPantallaCompleta` | 🟢 ALTA | Usa el estándar ROG. El emoji aparece doble porque se envía al `topBar` y al `Armador`. |
| **Chats** | `MoldeSheetEmergenteV3` | 🟡 MEDIA | No usa el Armador. Construye la cabecera manualmente dentro de una Sheet. |
| **Calendario** | `MoldeSheetEmergenteV3` | 🔴 BAJA | Cabecera manual muy compleja debido al selector de fecha central (slot no soportado por el Armador). |
| **Home** | `ArmadorListaPantallaCompleta` | 🟢 ALTA | Sigue el protocolo correctamente (sin iconos redundantes). |

## 2. Por qué son distintas? (Causas Raíz)

1.  **Evolución Temporal**: El `ArmadorListaPantallaCompleta` es una pieza de infraestructura más reciente. Pantallas core como `Chat` y `Calendario` fueron diseñadas antes de que este componente estuviera maduro.
2.  **Rigidez del Armador**: La `CabeceraMaverickV3` actual tiene slots para: `Icono Izquierdo`, `Perfiles`, `Títulos` y `Acciones Derecha`. Carece de un **Slot Central** o un sistema de **Slots Dinámicos** para casos especiales como el selector de fecha del calendario.
3.  **Doble Identidad (Sheets vs Roots)**: `Chat` y `Calendario` están envueltos en `MoldeSheetEmergenteV3` incluso cuando actúan como pantallas raíz, lo que crea una anatomía diferente a la de `Home` o `Presupuestos`.

## 3. Problema del Emoji Redundante (Presupuestos)

En la captura se observa que el emoji `💰` aparece tanto en la cabecera superior del sistema como en la cabecera de la lista. Esto rompe la Ley de "Higiene Visual".

## 4. Plan de Acción Inmediato

1.  **Saneamiento Presupuestos**: Eliminar el parámetro `icono` en la llamada al `ArmadorListaPantallaCompleta` en `PresupuestosScreen.kt`.
2.  **Evolución de Infraestructura**: Actualizar `CabeceraMaverickV3` para soportar un `slotCentral` opcional. Esto permitirá migrar `Calendario` al estándar.
3.  **Migración de Chats**: Reemplazar la cabecera manual de `ChatScreen.kt` por la llamada estandarizada al Armador una vez que los slots sean flexibles.

---
**Informática Maverick - Departamento de Diseño de Sistemas (2026)**
