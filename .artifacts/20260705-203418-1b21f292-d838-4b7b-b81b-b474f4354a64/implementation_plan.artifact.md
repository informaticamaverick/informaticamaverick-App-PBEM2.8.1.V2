# Auditoría y Reestructuración de Arquitectura Maverick Elite (v2026)

Este plan detalla la reestructuración necesaria para garantizar que el sistema de identidades soporte múltiples empresas y sucursales (hasta 3x3), y corrige errores críticos de persistencia de direcciones y horarios en la App del Prestador, siguiendo las reglas estrictas de jerarquía.

## User Review Required

- **Jerarquía Multi-Entidad (3x3)**: Se mantiene `USUARIO -> EMPRESAS -> SUCURSALES`.
- **Regla de Ubicación**:
    - **EMPRESA**: NO posee dirección propia. Su función es legal/comercial y define las **categorías** (rubros).
    - **SUCURSAL/USUARIO (Cliente)**: Son las únicas identidades que poseen **dirección** y **horarios** operativos.
    - **Obligatoriedad**: Cada empresa creada DEBE tener al menos una sucursal (Casa Central) vinculada para ser válida.
- **Segmentación Firestore**:
    - App Prestador -> Colección `prestadores`.
    - App Cliente -> Colección `usuarios`.
- **Herencia de Categorías**: Las sucursales heredarán automáticamente los rubros de su empresa padre para garantizar que sean encontrables en la búsqueda por categoría y CP.

## Proposed Changes

### [Core: Datos e Identidad]

#### [IdentidadMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/IdentidadMavRepository.kt)

- **Corrección de Sincronización**:
    - Implementar discriminación de colecciones (`prestadores` vs `usuarios`) basándose en el módulo/contexto.
    - Ajustar `sincronizarIdentidad` para propagar categorías de la Empresa a todas sus Sucursales vinculadas antes de subir a Firestore.
- **Validación de Integridad**: Asegurar que al guardar una Empresa, se valide/cree su Sucursal principal.

#### [IdentidadMavMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/IdentidadMavMapper.kt)

- Reconstruir el mapeador para procesar la jerarquía 3x3, asegurando que las direcciones y horarios se asignen exclusivamente a los niveles operativos (Sucursal/Usuario).

---

### [Prestador: Lógica de Negocio]

#### [IdentidadPrestadorViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/profile/IdentidadPrestadorViewModel.kt)

- **Fix `guardarCambios`**:
    - Reforzar que si la identidad es `EMPRESA`, los cambios de dirección se redirijan o ignoren (la UI debe forzar la edición en la Sucursal).
    - Asegurar que el `geohash` y `codigoPostal` se calculen y guarden en la Sucursal.
- **Fix `mapIdentidadToEstadoEdicion`**: Cargar correctamente la jerarquía completa para que la UI refleje las sucursales vinculadas a cada empresa.

---

### [Shared UI: Componentes de Perfil]

#### [V3AddressBottomSheet.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/molecules/ProfileDialogsV3.kt)

- Corregir la detección de cambios para que `onSave` devuelva siempre un objeto válido con ID.
- Asegurar que la validación visual de "Dirección Verificada" sea persistente.

#### [V3ScheduleBottomSheet.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/molecules/ProfileDialogsV3.kt)

- Corregir la gestión del estado interno para que los cambios en los rangos horarios se reflejen inmediatamente en el borrador de edición.

---

## Verification Plan

### Automated Tests
- Crear test en `IdentidadMavRepositoryTest` que valide:
    - Intento de guardar Empresa sin Sucursal (Debe fallar o auto-crear).
    - Propagación de rubros de Empresa A -> Sucursal A1, A2, A3.

### Manual Verification
1. **Flujo Empresa**: Crear una Empresa, verificar que obligue a poner una dirección (que se guardará en su Sucursal principal).
2. **Búsqueda**: Verificar en Firebase que los documentos en `prestadores` tengan las categorías heredadas y el CP correcto.
3. **Persistencia**: Cerrar y abrir la app, verificar que la dirección de la sucursal y los horarios aparezcan correctamente en el perfil.
4. **Logs**: Validar `[PROPAGACION_RUBROS]` y `[SYNC_PRESTADOR]`.
