# Auditoría de Capas (zIndex) y Autoridad Visual (Grandes Ligas)

He analizado la distribución de "alturas" en la app azul. Tal como sospechabas, existe una colisión de autoridad donde algunos elementos de la pantalla (como paneles de detalles en la Home) comparten el mismo nivel de `zIndex` que la base del Asistente Be, lo que puede causar que las herramientas del sistema queden ocultas.

## 1. Mapeo de Alturas Actual (Hallazgos)

| Valor Actual | Componente | Observación |
| :--- | :--- | :--- |
| **6000f** | Barra de Búsqueda Be | Máxima autoridad. Correcto. |
| **4000f** | Navegación HUD (FAB) | Contenedor principal de Be. |
| **3600f** | Overlays de Home | Diálogos y pantallas de carga locales. |
| **3000f** | **BeCuerpo** y **DetailsPanel** | **COLISIÓN**: Ambos están en 3000f. Si el panel se abre, puede tapar a Be según el orden de composición. |
| **2000f** | **Sheets (V3)** | Nivel estándar de hojas emergentes. |
| **500f** | Armador Herramientas | Nivel interno de la barra de acciones. |

## 2. ¿Cómo lo resuelven las Apps de Grandes Ligas? (Telegram/Instagram)

Tras investigar la arquitectura de apps como **Telegram (Liquid Glass)** y las recomendaciones del **Android Knowledge Base**, estas son las estrategias ganadoras:

1.  **Enums/Constants de Capas**: Nunca usan "números mágicos". Definen un sistema de niveles: `Background`, `Content`, `Overlay`, `HUD`, `System`.
2.  **Soberanía de Ventana (Window Level)**: Las herramientas críticas de sistema (como el botón "Cerrar" o el indicador de carga) viven en un nivel de ventana superior o en un contenedor que envuelve a todo el NavHost.
3.  **Composición sobre zIndex**: Telegram prefiere el orden de declaración en el código (el último es el que está arriba) y usa `zIndex` solo para ajustes dinámicos mínimos.

## 3. Propuesta de "Soberanía de Capas v2.9"

Implementaremos un objeto centralizado `SoberaniaZIndex` para unificar la autoridad:

| Capa | Valor Sugerido | Descripción |
| :--- | :--- | :--- |
| **MUNDO** | `0f` | Listas, Canvas, Mapas. |
| **HOJAS_EMERGENTES** | `1000f` | BottomSheets y diálogos estándar. |
| **SOBRE_HOJAS** | `2000f` | Paneles de detalles que deben tapar la navegación pero no a Be. |
| **ASISTENTE_BASE** | `3000f` | El cuerpo de Be (FAB) y las herramientas de sistema. |
| **ASISTENTE_FEEDBACK** | `4000f` | Burbujas de texto y Toasts de Be. |
| **ASISTENTE_ESCÁNER** | `5000f` | Cabecera de búsqueda (Máxima autoridad). |

## 4. Problema de las Herramientas "Debajo"

El problema ocurre porque `ArmadorHerramientasCaja` tiene un `zIndex(500f)` interno, pero está dentro de un `Box` con `zIndex(3000f)` en `BeCuerpo`.
**El error real**: Si una pantalla usa un `zIndex` de `3600f` (como vimos en `HomeScreenClienteV4`), ésta se posiciona por encima del contenedor de Be (`3000f`).

**Solución**: Subiremos el nivel base del HUD de Be para que sea indiscutible frente a los elementos de las pantallas.

---
**Informática Maverick - Departamento de Arquitectura UI (2026)**
