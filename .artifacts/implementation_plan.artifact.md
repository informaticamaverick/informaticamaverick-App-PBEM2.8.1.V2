# Plan de Implementación: Navegación a Pantalla Nuevo Concurso Público

Este plan detalla los cambios necesarios para que, al tocar el botón de "Nuevo Concurso" en el Asistente Be desde la pantalla de `ConcursosPublicosScreen`, se abra una nueva pantalla completa (`NuevoConcursoPublicoScreen`) en lugar de un BottomSheet, siguiendo las Leyes Maverick (especialmente la #12).

## Propuesta de Cambios

### 1. Definición de Ruta en Navegación

#### [MODIFY] [NavegacionBarRutas.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/home/NavegacionBarRutas.kt)
- Agregar el objeto `NuevoConcurso` a la clase sellada `Screen`.

### 2. Orquestación de Navegación

#### [MODIFY] [NavegacionLienzoPrincipal.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/home/NavegacionLienzoPrincipal.kt)
- Registrar la nueva ruta `Screen.NuevoConcurso.route` en el `NavHost`.
- Pasar el callback de navegación a `ConcursoPublicoScreen`.

### 3. Lógica del ViewModel de Concursos

#### [MODIFY] [ConcursoPublicoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/budget/ConcursoPublicoViewModel.kt)
- Eliminar el estado `mostrarCrearConcursoSheet` (si ya no es necesario).
- Implementar un `SharedFlow` llamado `eventosNavegacion` para emitir la orden de ir a la nueva pantalla cuando se reciba la acción `concurso_nuevo` del coordinador.

### 4. Actualización de Pantallas de Concursos

#### [MODIFY] [ConcursoPublicoScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/ConcursoPublicoScreen.kt)
- Recibir el callback `alNavegarANuevoConcurso`.
- Observar el flujo de navegación del ViewModel y disparar el callback.

#### [NEW] [NuevoConcursoPublicoScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/NuevoConcursoPublicoScreen.kt)
- Crear una nueva pantalla que envuelva el `ArmadorConcursoLienzo`.
- Configurar el HUD de Be para mostrar las herramientas de navegación del Wizard (Siguiente, Atrás, Publicar).
- Manejar el éxito de la publicación regresando a la pantalla anterior.

## Verificación Plan

### Manual Verification
1. Abrir la pantalla de **Mis Concursos**.
2. Verificar que el Asistente Be muestra el botón "+" (Nuevo Concurso).
3. Tocar el botón y verificar que se abre la pantalla **Nueva Licitación** (pantalla completa, no sheet).
4. Completar los pasos del wizard usando las herramientas de Be (Siguiente).
5. Publicar y verificar que regresa automáticamente a la lista de concursos con un mensaje de éxito.
6. Probar el botón de retroceso en la cabecera y verificar que limpia el contexto del asistente.
