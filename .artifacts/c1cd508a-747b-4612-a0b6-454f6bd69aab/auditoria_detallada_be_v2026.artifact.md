# Auditoría Detallada: Ecosistema de Herramientas y Búsqueda Be (v2026.ELITE)

Este reporte detalla el funcionamiento interno, problemas de lógica y oportunidades de mejora del asistente Be y sus herramientas.

## 1. Auditoría de Herramientas (Tools Architecture)

### A. ¿Quién las arma? (The Builder)
El proceso es **Tripartito**:
1.  **Soberano (Pantalla)**: Define el contrato vía `ConfiguracionContextoBe`.
2.  **Obrero (BeCuerpoViewModel)**: Materializa los IDs de texto en objetos `ModeloAccionPequenaBe` usando `BeDictionary.Actions`.
3.  **Armador (ArmadorHerramientasLienzo)**: Distribuye los objetos en "Islas Bento" segmentadas por rol.

### B. Análisis de Islas (Segmentación)
Actualmente existen 4 islas fijas:
- **Isla Primaria**: Acciones circulares de alto impacto (ej: `fast`, `fav`).
- **Isla Navegación**: Control de flujos (ej: `sig`, `atras`).
- **Isla Edición/Multi**: Acciones sobre selección (ej: `delete_multi`).
- **Isla Sistema**: Control del HUD (ej: `teclado`, `cerrar_todo`).

> [!WARNING]
> **Rigidez Geométrica**: El diseño de las islas es estático en el código. Si una pantalla requiere una agrupación diferente, no puede hacerlo sin modificar el `ArmadorHerramientasLienzo`.

### C. Problemas de Lógica Detectados
1.  **Colisión de Colores**: `BeDictionary` define un `tint` para cada acción, pero `BotonHerramientaSupreme` fuerza `MaverickColors.ElectricCyan` para la mayoría de las islas, ignorando el diseño original del diccionario excepto en el modo reposo.
2.  **Lógica Hardcoded**: En `BeCuerpoViewModel.kt`, la acción `select_all` tiene una lógica "incrustada" para cambiar su icono a `AppIcons.Close` si todo está seleccionado. Esto debería estar definido en el diccionario o ser un estado del modelo, no un `if` en el mapeador.
3.  **Redundancia de IDs**: Existen IDs de simulación (`sim_chat`, `sim_massive`) que ensucian el diccionario y el autocompletado si ya no están en fase de pruebas.

## 2. Auditoría del Modo Búsqueda (Elite Search)

### A. Funcionamiento Interno
- **Activación**: Disparada por `BeBusquedaViewModel`.
- **Sincronización**: Usa el `consultaBusquedaGlobal` del coordinador.
- **Higiene**: El `ejecutarCierreMaestro()` limpia la consulta, evitando que una búsqueda en "Chats" se herede a "Presupuestos".

### B. Problemas Detectados
1.  **Doble Identidad**: El parpadeo de ojos de Be ocurre tanto en el FAB como en la Cabecera de búsqueda. Aunque visualmente parece el mismo personaje moviéndose, son dos instancias de `BeAssistantEyes`.
2.  **Teclado Invasivo**: Aunque existe la regla de "Higiene #4" (No abrir teclado en sheets), la lógica de `NavegacionHUDAsistente` tiene un `LaunchedEffect` que dispara el foco basándose únicamente en `abrirTecladoEnBusqueda`. Esto puede causar fricción si la sheet ya tiene su propio foco.

## 3. Rendimiento y Optimización (Performance Audit)

### A. Recomposiciones (Recomposition Stress)
El `uiState` de Be se recalcula con cada cambio en el `ContextoHUD`.
- **Mejora**: Se ha fragmentado el ViewModel en 3 (Búsqueda, Física, Cuerpo), lo cual mitiga el impacto.
- **Pendiente**: Los iconos de `BeDictionary` son `ImageVector` pesados. Se recomienda usar `remember` o `Painter` si se detecta lag en dispositivos gama media.

### B. Latencia de Feedback
El sistema de Toasts de Be (`BeToast`) comparte el mismo canal de visibilidad que las herramientas.
- **Riesgo**: Si Be está mostrando un "Procesando...", el usuario pierde acceso visual a las herramientas hasta que el toast desaparezca.

## 4. Recomendaciones de Mejora (Roadmap v2.9)

1.  **Dinamismo de Islas**: Permitir que el contrato defina la agrupación de herramientas en lugar de tener islas fijas.
2.  **Centralización de Íconos**: Mover la lógica de cambio de ícono (ej: `select_all` -> `close`) al `BeDictionary` mediante una función de conveniencia.
3.  **Sincronización Unificada**: Asegurar que la instancia de ojos en la cabecera y el FAB compartan el mismo `StateFlow` de animación para una transición fluida.

---
**Informática Maverick - Auditoría de Arquitectura (2026)**
