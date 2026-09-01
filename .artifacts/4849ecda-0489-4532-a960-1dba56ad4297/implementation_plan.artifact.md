# Implementación de Pantalla Soberana de Presupuestos de Licitación

Migración del archivero de presupuestos (sheet) a una pantalla táctica completa siguiendo el protocolo Maverick Elite v2026.

## Proposed Changes

### Capa de Datos (ViewModel & State)

#### [NEW] [LicitacionPresupuestosUiState.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/budget/LicitacionPresupuestosUiState.kt)
- Definición atómica del estado visual.
- Incluye: lista de presupuestos, info del concurso, filtros y estados de carga.

#### [NEW] [LicitacionPresupuestosViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/budget/LicitacionPresupuestosViewModel.kt)
- Orquestador de la lógica de negocio.
- Carga de metadatos del concurso y filtrado SQL de presupuestos.
- Gestión de soberanía HUD para el asistente Be.

---

### Capa de Interfaz (UI)

#### [NEW] [LicitacionPresupuestosScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/LicitacionPresupuestosScreen.kt)
- Implementación de la pantalla "tonta".
- Cabecera Elite con título dinámico.
- Integración de `BarraFiltrosV3` y `ArmadorGridPantallaCompleta`.

#### [MODIFY] [ConcursoPublicoScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/ConcursoPublicoScreen.kt)
- Actualizar el evento `onClick` de la tarjeta de concurso para disparar la navegación en lugar de la sheet.

---

### Navegación y Contratos

#### [MODIFY] [Screen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/Screen.kt)
- Registro de la nueva ruta: `licitacion_presupuestos/{idConcurso}`.

#### [MODIFY] [AppNavigation.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/AppNavigation.kt) (o equivalente)
- Integrar la nueva pantalla en el NavGraph principal.

## Verification Plan

### Manual Verification
- Navegar a la pantalla desde "Mis Cotizaciones".
- Verificar que la barra de navegación desaparezca.
- Verificar que el asistente Be no muestre herramientas (botones flotantes).
- Probar el filtrado y ordenamiento en tiempo real.
- Verificar que la flecha de los menús apunte correctamente.
- Validar el scroll colapsable de las cabeceras.
