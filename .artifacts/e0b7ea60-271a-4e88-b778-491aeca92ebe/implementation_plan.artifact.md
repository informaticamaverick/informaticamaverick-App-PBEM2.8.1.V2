# Plan de Optimización y Corrección de Entidades Core (Protocolo Maverick)

Este plan detalla los ajustes necesarios en las entidades de Room del módulo `:core` para asegurar el cumplimiento estricto de las Leyes Maverick Elite (especialmente Ley #2, #4 y #16) y las buenas prácticas de Room de Google.

## User Review Required

> [!IMPORTANT]
> Los campos marcados con `@Ignore` que intentaban inyectar listas de objetos complejos directamente en las entidades de Room (`IdentidadUsuarioMavEntity`) serán removidos de la entidad de base de datos para evitar errores de sincronización y poblamiento nulo. Las relaciones 1:N se gestionarán mediante DAOs con consultas `@Relation` o `Embedded` dedicadas.

## Open Questions

Ninguna. La estructura de relaciones quedó definida en la discusión previa (Usuario con direcciones y empresas referenciadas, Prestador con direcciones, y el modo Empresa con Sucursales, Horarios, Recursos y Equipo).

## Proposed Changes

### Módulo Core (`:core`)

#### [MODIFY] [IdentidadUsuarioMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/entidades/IdentidadUsuarioMavEntity.kt)
- Remover las propiedades `@Ignore` (`direccionPrincipal`, `direccionesAdicionales`, `empresasVinculadas`) de la entidad Room pura, trasladando la construcción de relaciones a consultas DAO o modelos de UI dedicados.

#### [MODIFY] [IdentidadPrestadorMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/entidades/IdentidadPrestadorMavEntity.kt)
- Revisar y limpiar propiedades que interfieran con la correcta normalización en Room. Asegurar compatibilidad de tipos de listas.

## Verification Plan

### Automated Tests
- Ejecutar compilación de Gradle para verificar que no existan errores en las anotaciones de Room.
  - Comando: `gradle_build(":core:assembleDebug")`

### Manual Verification
- Revisar la correcta compilación y sincronización del módulo `:core`.
