# Auditoría de Pantalla de Perfil (v2026.100)

## Resumen de Problemas Detectados

Tras una revisión exhaustiva del ecosistema de identidades, se han identificado varios fallos críticos de lógica y arquitectura que explican por qué los datos no se actualizan o borran correctamente.

### 1. Inconsistencia en la Estrategia de Persistencia (Local vs. Remoto)
- **Problema**: El `ProfileScreen` llama a `viewModel.sincronizarIdentidadLocal(it)` para cambios inmediatos, pero luego usa `viewModel.subirCambiosAFirebase()` (que usa `sincronizarEcosistemaRemoto`) para la nube.
- **Error de Lógica**: `sincronizarEcosistemaRemoto` en el repositorio **vuelve a leer de Room** antes de subir a Firestore. Si Room no se actualizó correctamente con todos los sub-objetos (direcciones, horarios), se suben datos viejos o incompletos.
- **Fuga de Datos**: Al borrar una dirección en la UI, se llama a `alGuardarIdentidad` con la lista filtrada, pero el repositorio en `sincronizarLocal` borra y vuelve a insertar basándose en lo que recibe. Hay inconsistencias entre `listaDirecciones` y `direccionesAdicionales`.

### 2. Fallo en la Propagación de Estado (Borradores huérfanos)
- **Problema**: Existe un `IdentidadEditorViewModel` que maneja un `EstadoEdicionPerfilMav` (borrador), pero el `ProfileScreen` principal **no usa consistentemente este borrador**.
- **Duplicidad**: `ProfileScreenContent` tiene su propio `modoEdicionActivo` y lógica de guardado que a veces ignora lo que está en el `editorViewModel`.
- **Efecto**: El usuario edita datos en un diálogo, el diálogo actualiza el borrador, pero la pantalla principal sigue mirando la entidad persistida de Room que aún no ha sido actualizada.

### 3. Error en la Gestión de Horarios y Sucursales
- **Problema**: Los horarios en las sucursales no se actualizan porque `PerfilMaverickV3` usa `alSincronizarIdentidad` para la sucursal, pero el `IdentidadPrestadorViewModel.sincronizarIdentidadLocal` está optimizado para la identidad principal (USUARIO).
- **ID Mismatch**: `HorarioMavDao` usa `idReferencia`. Si al guardar una sucursal no se pasa correctamente el ID de la sucursal como referencia, el horario se pisa o se pierde.

### 4. Código Redundante y Obsoleto (Aliases Legacy)
- **Problema**: `IdentidadMavEntity` tiene campos duplicados como `listaRecursos` / `recursosMav` / `recursos` y `listaEquipo` / `equipo`.
- **Confusión**: Diferentes partes de la UI (`ProfileSectionsV3` vs `ProfileSectionsMav`) usan diferentes campos para lo mismo. Un cambio en uno no se refleja en el otro.
- **Riesgo**: La función `fusionarActivos` intenta arreglar esto pero es propensa a errores si se olvida actualizar un alias.

### 5. Fallo en el Borrado de Direcciones
- **Problema**: En `ProfileSectionsMav.kt`, el borrado de direcciones adicionales intenta llamar a `alGuardarIdentidad`, pero pasa una copia que puede no ser detectada como "nueva" por Room debido a cómo se manejan las listas `@Ignore`.
- **SSOT Quebrada**: Room no guarda las direcciones dentro de la tabla de identidades, sino en una tabla aparte (`direcciones_mav`). Si la lógica del repositorio no es atómica para borrar lo que ya no existe en la lista recibida, la dirección persiste en DB.

## Solución Propuesta (Plan de Acción)

1.  **Unificar el SSOT**: Eliminar los alias legacy en `IdentidadMavEntity` y estandarizar en un solo set de nombres (ej: `recursosMav`, `equipoMav`).
2.  **Atocimidad en Repositorio**: Modificar `sincronizarLocal` para que realice un "diff" real o limpie y re-inserte de forma transaccional todos los hijos vinculados.
3.  **Refactor de ProfileScreen**: Eliminar el estado duplicado en la View y delegar 100% al `IdentidadEditorViewModel` durante la edición.
4.  **Corrección de Flujos de Datos**: Asegurar que cada `alSincronizarIdentidad` pase por el validador del `IdentidadEditorViewModel` antes de tocar el repositorio.
