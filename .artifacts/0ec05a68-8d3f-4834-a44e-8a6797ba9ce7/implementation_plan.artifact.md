# Plan de Soberanía del Cliente v2026: Sistema de Borradores (App Azul)

Este plan detalla la implementación de un sistema de borradores en memoria (RAM) para la edición de perfil del cliente en la App Azul, asegurando paridad arquitectónica con la App Naranja y cumpliendo con la Ley de Soberanía.

## User Review Required

> [!IMPORTANT]
> Se introducirá el `BorradorPerfilUsuarioGestor` para evitar que las ediciones parciales del usuario impacten Room antes de pulsar "Sincronizar". Esto previene la corrupción de datos locales si el usuario cancela una edición.

## Proposed Changes

### [Component Name] App Azul (Gestión de Estado)

#### [NEW] [BorradorPerfilUsuarioGestor.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/BorradorPerfilUsuarioGestor.kt)
- Centralizar el estado temporal de edición del cliente (Perfil, Direcciones, Empresas).
- Implementar lógica de detección de cambios (`hayCambiosPendientes`).

#### [MODIFY] [ArmadorUsuarioViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/ArmadorUsuarioViewModel.kt)
- Absorber lógica de `UsIdentidadViewModel`.
- Integrar el nuevo `BorradorPerfilUsuarioGestor`.
- Manejar mutaciones tácticas: `guardarCambiosIdentidad`, `actualizarDirección`, `eliminarDirección`.
- Integrar `GestorUbicacionMav` para GPS.

---

### [Component Name] App Azul (Interfaz de Usuario)

#### [MODIFY] [PerfilUsuarioScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/PerfilUsuarioScreen.kt)
- Migrar inyección de `UsIdentidadViewModel` a `ArmadorUsuarioViewModel`.
- Conectar estados del borrador a los componentes de UI.

---

### [Component Name] Refactorización Pasiva (Legacy)

#### [MODIFY] [UsIdentidadViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/UsIdentidadViewModel.kt)
- Vaciar lógica interna (Comentado total).

## Verification Plan

### Manual Verification
1. **Edición Segura**: Cambiar el nombre visible del usuario, salir de la pantalla sin guardar, y verificar que al volver el nombre original se mantenga (Borrador descartado).
2. **Commit Atómico**: Editar dirección y biografía, pulsar "Sincronizar", y verificar que los datos se guarden en Room y suban a Firebase.
3. **GPS**: Validar que la detección de ubicación actualice el borrador correctamente.
