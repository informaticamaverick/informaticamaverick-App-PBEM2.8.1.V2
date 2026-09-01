# Plan de Refactorización de BeCerebroViewModel (Protocolo Elite v2026)

Este plan tiene como objetivo aplicar las **Leyes Maverick (especialmente la #12)**, moviendo la lógica de identidad y ubicación a sus respectivos "Obreros" (ViewModels especializados) y dejando a `BeCerebroViewModel` como un portavoz ligero.

## User Review Required

> [!IMPORTANT]
> Se delegará la gestión del **Perfil Activo** al `ArmadorUsuarioViewModel` y la lógica de **Sincronización de Ubicación** al `UbicacionObrero`.
> `BeCerebroViewModel` dejará de calcular nombres y fotos, delegando estas tareas a los especialistas.

## Proposed Changes

### [Component: Identity & Profile]

#### [MODIFY] [ArmadorUsuarioViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/profile/ArmadorUsuarioViewModel.kt)
- Agregar `nombrePerfilActivo` y `fotoPerfilActiva` como `StateFlow`s.
- La lógica combinará el `ecosistemaMaestro` (SSOT) con el `idPerfilSeleccionado` del `Coordinador`.

### [Component: Location & Sync]

#### [MODIFY] [UbicacionObrero.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/home/UbicacionObrero.kt)
- Implementar `seleccionarDireccion(idDireccion)`.
- Mover aquí la lógica de:
    1. Selección de perfil automático si la dirección es de una empresa.
    2. Disparo de `sincronizarEcosistemaRed` basado en el Código Postal.

### [Component: Assistant HUD]

#### [MODIFY] [BeCerebroViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/vm/BeCerebroViewModel.kt)
- Eliminar `estadoCuenta`, `nombrePerfilActivo`, `fotoPerfilActiva`.
- Simplificar `seleccionarDireccion` para que sea una delegación simple o eliminarla si el HUD usa directamente al Obrero.
- Mantener únicamente la lógica de búsqueda (`searchResults`) y visibilidad de diálogos específicos de Be.

### [Component: UI Integration]

#### [MODIFY] [HomeScreenViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/home/HomeScreenViewModel.kt)
- Actualizar `HomeScreenContent` para obtener la identidad del `userViewModel` (ArmadorUsuarioViewModel).
- Actualizar las llamadas de selección de dirección para usar el `ubicacionObrero`.

## Verification Plan

### Automated Tests
- No se dispone de tests automáticos en el contexto actual, se basará en la consistencia de tipos.

### Manual Verification
- Verificar que el nombre y foto de perfil sigan apareciendo correctamente en la Home.
- Verificar que al cambiar de dirección, Be siga notificando la sincronización y cambie el perfil si corresponde.
