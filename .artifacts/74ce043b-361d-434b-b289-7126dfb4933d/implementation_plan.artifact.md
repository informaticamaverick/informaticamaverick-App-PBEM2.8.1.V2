# Actualización de Documentación y Alineación de Protocolos

Se han identificado discrepancias entre la estructura actual del código (v2026.ELITE/SUPREME) y los manuales técnicos (READMEs). Este plan detalla las actualizaciones necesarias para reflejar los cambios recientes en las entidades de Room y asegurar el cumplimiento de las Leyes de Arquitectura Maverick.

## User Review Required

> [!IMPORTANT]
> Se activará `exportSchema = true` en las bases de datos para cumplir con el protocolo de persistencia. Esto generará archivos JSON en la carpeta del proyecto que deben ser incluidos en el control de versiones (Git) para el seguimiento de la evolución del esquema.

## Proposed Changes

### [Componente: Documentación Core]

#### [MODIFY] [README_RECURSOS_Y_HORARIOS.md](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Naranja/README_RECURSOS_Y_HORARIOS.md)
Se añadirán las secciones correspondientes a la gestión de bloqueos temporales y la vinculación técnica de personal.
- Inclusión de `ExcepcionHorariaEntity` para el manejo de vacaciones y feriados.
- Documentación del campo `idRecursoVinculado` en `EquipoTrabajoEntity`.

#### [MODIFY] [PROTOCOL_IDENTIDAD_MAV.md](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOL_IDENTIDAD_MAV.md)
Actualización de la tabla de componentes para reflejar la especialización de identidades.
- Reemplazar la referencia genérica a `IdentidadMavEntity` por `IdentidadUsuarioMavEntity` e `IdentidadPrestadorMavEntity`.

---

### [Componente: Persistencia Local]

#### [MODIFY] [AppDatabase.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/AppDatabase.kt)
#### [MODIFY] [PrestadorDatabase.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/datos/local/PrestadorDatabase.kt)
- Activación de `exportSchema = true` (Ley #16).
- Mejora de los comentarios de versión para mayor claridad semántica.

## Verification Plan

### Manual Verification
1. Verificar que los READMEs actualizados sean legibles y sigan el tono de los protocolos existentes.
2. Ejecutar un build para confirmar que `exportSchema = true` no causa errores (puede requerir configurar `room.schemaLocation` en el `build.gradle`, pero usualmente Studio lo maneja o da un warning informativo).
