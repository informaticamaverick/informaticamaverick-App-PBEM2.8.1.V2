# Tareas de Refactorización Maverick Elite (v2026)

## Fase 1: Núcleo Atómico (Core - Usuario) [x]
- [x] Modificar `UsuarioCompletoMav.kt` para incluir `CuentaMavEntity`.
- [x] Actualizar `UsuarioMapper.kt` para integrar datos de la cuenta.

## Fase 2: Implementación de Lógica en Repositorios (App Azul) [x]
- [x] Actualizar `SincUsuarioRepositorio.kt` con persistencia Room real.
- [x] Actualizar `SincUsuarioDwnPrestadorRepositorio.kt` con persistencia Room real.
- [x] Crear `ConsultasUsuarioRepositorio.kt` para lectura reactiva.
- [x] Crear `ConsultasUsuarioPrestadorRepositorio.kt` para lectura de perfiles ajenos.

## Fase 3: Implementación de Lógica en Repositorios (App Naranja) [x]
- [x] Actualizar `SincPrestadorRepositorio.kt` con persistencia Room real y lógica de guardado jerárquico.
- [x] Actualizar `SincPrestadorDwnUsuarioRepositorio.kt` con persistencia Room real.
- [x] Crear `ArmadorPrestadorPerfilUsuarioViewModel.kt`.
- [x] Crear `ConsultasPrestadorRepositorio.kt` para lectura reactiva (Ensamblado).

## Fase 4: Integración en ViewModels [x]
- [x] Actualizar `ArmadorUsuarioViewModel` para usar la descarga de perfil propio.
- [x] Actualizar `ArmadorPrestadorViewModel` para usar la descarga de perfil propio.
- [x] Actualizar `ArmadorUsuarioPrestadorViewModel` para usar los nuevos métodos de descarga.
- [x] Migrar todos los ViewModels de la App Naranja (Dashboard, Chat, Mercado, etc.) a los nuevos repositorios.
- [x] Migrar todos los ViewModels de la App Azul (Auth, Budget, Chat, Calendar, etc.) a los nuevos repositorios.

## Fase 5: Unificación y Estandarización (Mudanza Final) [x]
- [x] Crear `GestorUbicacionMav.kt` (Centralizado en Core).
- [x] Renombrar `ObreroSincronizacionMav` a `SincronizacionWorkerMav`.
- [x] Crear `ArmadorPrestadorPremiumViewModel.kt` (Facturación).
- [x] Refactorizar `ArmadorPrestadorViewModel` (Soberanía + Borrador + GPS).
- [x] Vaciar y comentar `PrestadorPerfilViewModel.kt` (Legacy).
- [x] Vaciar y comentar `PrestadorPerfilRepository.kt` (Legacy).
- [x] Vaciar y comentar `CoordinadorPrestadorMav.kt` (Legacy).

## Fase 6: Soberanía del Cliente (Borradores App Azul) [x]
- [x] Crear `BorradorPerfilUsuarioGestor.kt` (Especialista en RAM para App Azul)
- [x] Refactorizar `ArmadorUsuarioViewModel` para manejar Soberanía y Edición
- [x] Reconectar `PerfilUsuarioScreen.kt` al nuevo Armador
- [x] Vaciar y comentar `UsIdentidadViewModel` (Legacy)
- [x] Vaciar y comentar `UsUsuarioRepository` (Legacy)
- [x] Vaciar y comentar `UsPrestadorRepository` (Legacy)
- [x] Vaciar y comentar `UsIdentidadDetalleViewModel` (Legacy)

## Fase 7: Verificación [x]
- [x] Compilación exitosa de App Naranja (:prestador).
- [x] Compilación exitosa de App Azul (:app).
- [x] Validar logs tácticos `[SYNC_...]` y `[COMMIT_...]`.
- [x] Validar que no queden referencias activas al código legacy.
