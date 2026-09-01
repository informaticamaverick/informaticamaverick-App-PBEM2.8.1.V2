# Plan de Solución para Problemas de Publicación, Ads y Mercado

Este plan aborda la falta de visualización de anuncios de video al publicar, el cierre incorrecto del Wizard, la actualización de la lista de concursos en la App Azul, y la falta de suscripción a tópicos FCM y visualización de mercado en la App Naranja.

## User Review Required

> [!IMPORTANT]
> Se requiere confirmar que las pantallas `ConcursoPublicoScreen` y `NuevoConcursoPublicoScreen` comparten la misma instancia de `BorradorConcursoViewModel`. Si están en diferentes actividades o grafos de navegación sin un dueño común, la publicidad disparada desde el ViewModel podría no ser captada por la pantalla que queda activa.

## Proposed Changes

### App Azul (Cliente)

#### [MODIFY] [BorradorConcursoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/budget/BorradorConcursoViewModel.kt)
- Corregir el flujo de publicación para que el Wizard se cierre inmediatamente y se limpien los datos.
- Poblar correctamente el campo `filtrosBusqueda` en `ConcursoPublicoEntity` usando el protocolo `Z_{CP}` y `C_{CP}_{RUBRO}`.
- Asegurar que `dispararRecargaEnPantallaPrincipal()` se llame antes de mostrar los anuncios.

#### [MODIFY] [ConcursoPublicoDao.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/dao/ConcursoPublicoDao.kt)
- Corregir la unión (JOIN) de la tabla FTS. Cambiar `cp.idConcurso = fts.rowid` por `cp.rowid = fts.rowid`, ya que `idConcurso` es un String y `rowid` es un Long.

---

### App Naranja (Prestador)

#### [MODIFY] [NotificacionesViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/dashboard/NotificacionesViewModel.kt)
- Inyectar `MotorIndiceConcursoPrestador`.
- Implementar la llamada a `sincronizarOportunidadesDeTrabajo` en `refrescarMercado()` y en el flujo de inicialización si es posible.

#### [MODIFY] [MotorPerfilPrestadorDeep.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/dominio/motores/MotorPerfilPrestadorDeep.kt)
- Inyectar `MotorIndiceConcursoPrestador`.
- Asegurar que al impactar el ecosistema (actualizar perfil), se sincronicen también las oportunidades de trabajo (suscripción a tópicos FCM).

## Verification Plan

### Automated Tests
- No se proponen tests unitarios nuevos en esta fase, se prioriza la corrección funcional.

### Manual Verification
1. **App Azul:**
   - Crear un nuevo concurso.
   - Al pulsar "Publicar", verificar que el Wizard se cierra inmediatamente.
   - Verificar que se muestra la publicidad de video.
   - Al cerrar la publicidad, verificar que el concurso aparece en la lista de "Mis Concursos".
2. **App Naranja:**
   - Abrir la pantalla de "Mercado de Concursos".
   - Pulsar el botón de "Refrescar" y verificar en los logs que se sincronizan los tópicos FCM (`MOTOR_CONCURSO_PREST`).
   - Verificar que aparecen los concursos publicados desde la App Azul que coincidan con la zona (CP) y rubros del prestador.
