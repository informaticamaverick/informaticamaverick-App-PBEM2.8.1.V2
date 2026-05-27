# 🚀 Procedimientos de Navegación y Herramientas Be Assistant (ELITE EDITION)

Este documento es la **Guía Maestra** para la gestión de interfaces, navegación y el asistente Be bajo los estándares de **Arquitectura Premium Elite**.

---

## 1. 🏛️ Arquitectura de Capacidades (HUDContext)

La visibilidad y el comportamiento de la UI no se manejan con lógica condicional, sino mediante **Capacidades de Contexto** en `HUDModels.kt`.

### Cómo crear o editar un Contexto
Al definir un `HUDContext`, configuras sus capacidades elite:
```kotlin
MI_CONTEXTO(
    isSearchable = true,      // ¿Be puede buscar aquí?
    requiresBottomBar = true,  // ¿Se muestra la Nav Bar?
    showBeAssistant = true,    // ¿Be está presente?
    allowsCustomTools = true   // ¿Los Obreros pueden inyectar herramientas?
)
```

---

## 2. 🗺️ Gestión de Pantallas (Flujo Elite)

### A. Crear una Nueva Pantalla
1. **Ruta**: Define la constante en `Screen` (dentro de `AppNavigation.kt`).
2. **Contexto**: Agrega el nuevo enum en `HUDContext` con sus capacidades.
3. **Mapeo**: En `BeBrainViewModel.onRouteChanged`, añade la ruta al `when` para que el Cerebro reconozca el contexto.
4. **UI**: Crea el `composable` en el `NavHost`. No necesitas manejar visibilidad de barras; el sistema lo hará por ti basándose en el Contexto.

### B. Gestión de Sub-Vistas (Internal State)
Si una pantalla tiene estados internos que requieren cambiar la visibilidad (ej: abrir un Chat), llama a `beViewModel.setHUDContext(...)` para alternar entre contextos (ej: de `CHAT` a `CHAT_CONVERSATION`).

---

## 3. 🛠️ Sistema de Herramientas (Brain-Worker Sync)

### A. Registro de Herramientas Estáticas
Se definen en `BeBrainViewModel.updateActionsForContext`. Son herramientas que pertenecen al núcleo de la app.

### B. Inyección Dinámica (The Elite Way)
Los "Obreros" (ViewModels de pantalla) inyectan sus propias herramientas al activarse:
```kotlin
// En el ViewModel del Obrero
fun onActive() {
    beBrain.setCustomActions(
        actions = listOf(BeSmallActionModel(...)),
        context = HUDContext.MI_PANTALLA
    )
}
```

---

## 4. ✨ Estándares de Fluidez y SSOT

### A. Visibilidad Reactiva
La visibilidad de la barra inferior y de Be es una **función pura** del contexto y el estado de búsqueda:
- **NavBar**: Se oculta automáticamente si el contexto lo requiere O si la búsqueda está activa.
- **Be**: Se muestra/oculta puramente por la propiedad `showBeAssistant` del contexto.

### B. Capas de Z-Index
- **Nav Bar**: 950
- **Be Assistant**: 1100
- **Overlays/Sheets**: 1200+

---

## 🔄 Resumen del Ciclo de Vida Elite

1. **Navegación**: El usuario cambia de ruta o estado interno.
2. **Identificación**: El Cerebro actualiza el `HUDContext`.
3. **Reacción**: La Nav Bar y Be se animan automáticamente sin intervención manual en la pantalla.
