# Relevamiento Técnico: Asistente Be (v2026.ELITE)

Este documento detalla el estado actual de las funciones del asistente Be, identificando qué está en uso, qué es redundante y qué debe ser eliminado tras la decisión de desactivar el movimiento ("vuelo").

## 1. Análisis de Funciones

| Función / Módulo | Estado | Propósito | Recomendación |
| :--- | :--- | :--- | :--- |
| **Física de Vuelo (Offsets)** | 🔴 COMENTADO | Permitía arrastrar a Be y que volara al activar la búsqueda. | **ELIMINAR**. Los campos `offsetHorizontal` y `offsetVertical` en `EstadoFisicoBeAsistente` ya no tienen propósito. |
| **Physics Docking (Relleno)** | 🟢 EN USO | Ajusta la posición vertical de Be según la presencia de la barra de navegación inferior. | **MANTENER**. Es vital para que Be no se solape con los botones del sistema. |
| **Animación de Ojos (Blinking)** | 🟢 EN USO | Lógica de parpadeo aleatorio y movimiento de pupilas. | **MANTENER**. Da vida al asistente. |
| **Motor de Herramientas** | 🟢 EN USO | Construcción dinámica de `herramientasPrimarias`, `sistema`, `navegacion`, etc. | **MANTENER/REFINAR**. Se detectaron herramientas de simulación (`sim_chat`, `sim_tender`) que podrían ser obsoletas si ya no se está en fase de pruebas. |
| **Ciclo de Sueño (Hibernación)** | 🟢 EN USO | Doble tap para dormir/despertar al asistente. | **MANTENER**. |
| **Burbujas de Texto (Toast/Msg)** | 🟢 EN USO | Sistema de feedback visual y respuestas del asistente. | **MANTENER**. |

## 2. Errores Detectados (Bloqueantes de Compilación)

- **`NavegacionHUDAsistente.kt`**: Intenta instanciar `AccionesAsistenteBe` sin pasar los parámetros de posicionamiento (`alActualizarPosicion`, `alEstablecerArrastrando`), los cuales fueron comentados en el constructor pero no en la definición de la clase.
- **`BeMotorEstado.kt`**: Contiene métodos comentados que ensucian el código y no son llamados por nadie.

## 3. Código Viejo / Redundante

- **`BeDictionary.kt`**: Contiene una sección de `Filters` y `Sorts` con una nota indicando que "Be ya no maneja estos datos directamente (Ley #12)". Sin embargo, el código sigue ahí.
- **`AccionesAsistenteBe`**: La estructura de datos en `BeCuerpo.kt` sigue pidiendo los callbacks de arrastre.

## 4. Plan de Acción Inmediato

1.  **Saneamiento de Modelos**: Limpiar `AccionesAsistenteBe` y `EstadoFisicoBeAsistente` de todo lo relacionado con el movimiento.
2.  **Limpieza de UI**: Eliminar los bloques de código comentados en `BeCuerpo.kt` y `NavegacionHUDAsistente.kt`.
3.  **Corrección de Compilación**: Sincronizar la instanciación de acciones en el HUD.
4.  **Eliminación de Redundancia**: Comentar o eliminar los mapas de filtros/sorts en `BeDictionary` si ya no se usan en el flujo del asistente.
