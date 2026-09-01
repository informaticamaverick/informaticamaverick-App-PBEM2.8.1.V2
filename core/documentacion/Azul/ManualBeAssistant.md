# 🤖 Manual de Operaciones: Asistente Be (Elite v2026.FINAL)

Este manual documenta el funcionamiento técnico, físico y conversacional del asistente Be, el núcleo de interacción inteligente del ecosistema Informática Maverick.

---

## 🏛️ 1. Arquitectura de Control Segmentada (Elite SRP)

El Asistente Be opera bajo un modelo de **Soberanía Declarativa Unificada**.

*   **Coordinador de Navegación (HUD Register):** Mantiene un mapa de registros soberanos. El último insertado dicta qué se ve (Be, Barra Nav, Herramientas). Esto permite que una hoja (Sheet) defina sus herramientas y al cerrarse, la pantalla base recupere automáticamente el control.
*   **BeBusquedaMotor (El Cerebro):** Único dueño de la búsqueda. Normalización Atómica y Debounce.
*   **BeCuerpoViewModel (El Arquitecto):** Materializa las herramientas según el contrato activo del Coordinador de Navegación.

---

## 🛠️ 2. Guía: Crear Herramientas Paso a Paso

### Paso 1: Definir Identidad en `BeDictionary.kt`
Añadir la acción al mapa `Actions` con sus visuales. 
- **Tinte**: Use `Color.Red` para acciones de cancelación/borrado.
- **Label**: Texto corto en mayúsculas (v2026.ELITE).

### Paso 2: Declarar Contrato en la Screen
En el `DisposableEffect` de la Pantalla:
```kotlin
val beConfig = remember { 
    ContextoHUD.MI_PANTALLA.crearConfiguracionBase(
        primarias = listOf("mi_accion"), // Botones en reposo
        edicion = listOf("accion_multi"), // Botones en multiselección
        ocultarNavOverride = true 
    ) 
}
DisposableEffect(Unit) {
    navCoordinador.registrarPantalla(beConfig)
    onDispose { navCoordinador.removerPantalla(beConfig.id) }
}
```

### Paso 3: Escuchar la Acción
Capture el evento en un `LaunchedEffect`:
```kotlin
LaunchedEffect(Unit) {
    brainViewModel.actionEvent.collect { actionId ->
        when(actionId) {
            "mi_accion" -> { /* Lógica */ }
        }
    }
}
```

---

## 🧪 3. Mecánicas y Física (Elite Physics)

### A. Estados Visuales (`EstadoBe`)
- **REPOSO:** Mirada errante aleatoria.
- **HABLANDO:** Sistema de Toasts (Logs) activo. Mirada centrada.

### B. Gestos Tácticos
- **Toque Simple:** Alternar modo Búsqueda.
- **Doble Toque:** **Hibernación**. Be duerme y se aparta 30dp para liberar espacio visual.
- **Toque Largo:** Forzar visibilidad de herramientas secundarias o por eleccion segun corresponda , acciones especiales .

### C. Posicionamiento Soberano
Siguiendo la visión de **"Be vive en un solo lugar"**, el asistente mantiene una posición fija:
- **Bias Vertical (0.85f):** Posición estándar sobre la barra inferior. No se desplaza al abrir hojas de datos para garantizar predictibilidad.

---

## 🌪️ 3. El Embudo: Filtrado y Búsqueda (Ley #14 & #15)

### A. Búsqueda se escribe con Be
La búsqueda está centralizada. Ningún componente debe implementar normalización de texto localmente. Todos deben "beber" de `BeBusquedaMotor.consultaNormalizadaDebounced`.

### B. Filtrado SQL-First
Be es un **Portavoz Pasivo**. No filtra datos. 
1. El usuario escribe en Be.
2. El Motor normaliza: "Cáñería" -> "caneria".
3. El ViewModel de la pantalla recibe "caneria" y dispara una consulta SQL `@Query`.

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
