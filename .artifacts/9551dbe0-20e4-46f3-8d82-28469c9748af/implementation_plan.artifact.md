# Plan de Saneamiento: Asistente Be y Flujo de Herramientas (v2026.ELITE)

Este plan aborda la falta de visibilidad de las herramientas en la `HomeScreen` y la ausencia de feedback visual (Toasts/Logs) en el sistema del Asistente Be, especialmente durante los cambios de estado en el flujo de ubicaciones.

## User Review Required

> [!IMPORTANT]
> Se ha detectado que el `BeCuerpoViewModel` no estaba vinculando el flujo de `toastActivo` del Coordinador al `UiState`, lo que impedía que cualquier Toast enviado por los "Obreros" se mostrara en pantalla.

## Proposed Changes

### [Componente: HUD & Asistente Be]

#### [MODIFY] [BeCuerpoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/vm/BeCuerpoViewModel.kt)
*   Vincular `coordinador.toastActivo` dentro del `combine` que genera el `uiState`.
*   Pasar el valor del Toast al constructor de `EstadoUiBeAsistente` para que sea consumido por la UI.

#### [MODIFY] [ArmadorHerramientasCaja.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/herramientas/ArmadorHerramientasCaja.kt)
*   Refinar la lógica de `visibleRealmente` para que respete el flag `mostrarHerramientas` del estado de UI para las acciones, pero permita que los Toasts (Logs) se muestren siempre que estén activos.

### [Componente: Home App Azul]

#### [MODIFY] [HomeScreenViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/home/HomeScreenViewModel.kt)
*   Actualizar la configuración inicial de Be en `HomeScreenComplete` para establecer `mostrarHerramientas = true`, garantizando que "fast" y "favoritos" sean visibles desde el inicio.

#### [MODIFY] [UbicacionObrero.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/home/UbicacionObrero.kt)
*   Añadir llamadas a `coordinator.mostrarToast` en la función `seleccionarDireccion` para informar al usuario sobre el cambio de estado de ubicación (feedback táctico).

## Verification Plan

### Automated Tests
*   No se requieren pruebas automatizadas nuevas, se verificará mediante despliegue y uso de herramientas de diagnóstico.

### Manual Verification
1.  **Carga de Home**: Iniciar la app y verificar que el Asistente Be muestre las herramientas "fast" (⚡) y "favoritos" (❤️) automáticamente.
2.  **Cambio de Ubicación**: Abrir el menú de direcciones, seleccionar una dirección y verificar que aparezca un Toast de Be confirmando el cambio.
3.  **GPS**: Activar/Desactivar GPS y verificar que los Toasts de "Conectando..." y "Ubicación Encontrada" sean visibles en la barra de herramientas de Be.
