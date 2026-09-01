# Plan de Acción: Creación del Motor de Concursos Usuario y Limpieza de Tópicos

Este plan detalla la reestructuración del flujo de Concursos para que sea "Atómico" y siga el patrón del motor de búsqueda, eliminando dependencias obsoletas y unificando la lógica de tópicos en el motor central.

## User Review Required

> [!IMPORTANT]
> Se deshabilitará `SincUsuarioTopicksRepositorio.kt` comentando su contenido. Toda la lógica de suscripción pasará a `MotorTopicosMav` en el módulo `:core`.

> [!NOTE]
> `MotorDescubrimientoMav` permanecerá comentado ("muerto") para evitar conflictos con la arquitectura actual basada en `GeneradorTópicosFCM`.

## Proposed Changes

### [Core] - Unificación de Tópicos

#### [MODIFY] [MotorTopicosMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/motores/MotorTopicosMav.kt)
- Reemplazar `MotorDescubrimientoMav` por `GeneradorTópicosFCM`.
- Implementar `sincronizarTopicosInteres(codigosPostales: List<String>)`.
- Implementar `suscribirAConcursos(cp: String, categorias: List<String>)`.
- Actualizar `suscribirAZona` para usar `GeneradorTópicosFCM`.

#### [MODIFY] [ConcursoPublicoRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/repositorios/ConcursoPublicoRepository.kt)
- Simplificar `crearNuevoConcurso` para que solo maneje Room y subida de imágenes.
- Eliminar dependencias de indexación (`IndiceConcursoUsuarioRepositorio`, DAOs de usuario/cuenta) que ahora serán manejadas por el Motor.

---

### [App] - Orquestación Atómica

#### [NEW] [MotorConcursoUsuario.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/dominio/motores/MotorConcursoUsuario.kt)
- Nuevo orquestador para el flujo de licitaciones del cliente.
- Ejecuta la cadena atómica:
    1. Room: Guardar concurso.
    2. Firestore: Indexación Shallow.
    3. FCM: Suscripción a tópicos de competencia.

#### [MODIFY] [SincUsuarioTopicksRepositorio.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/datos/repositorios/SincUsuarioTopicksRepositorio.kt)
- Comentar el contenido del archivo para marcarlo como obsoleto/muerto.

## Verification Plan

### Automated Tests
- No se dispone de tests automáticos en este paso, pero se verificará la compilación.

### Manual Verification
- Verificar que `MotorTopicosMav` compile correctamente tras el cambio de inyección.
- Verificar que el nuevo `MotorConcursoUsuario` tenga acceso a todas las dependencias necesarias.
