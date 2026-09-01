# Walkthrough: Sincronización Profesional de Identidad y Firebase (V2026)

He finalizado la refactorización profunda del flujo de login y sincronización para la App del Cliente, asegurando paridad total con la robustez de la App del Prestador y cumpliendo con el protocolo Maverick Elite.

## 🚀 Cambios Principales

### 1. Estandarización de Colecciones (SSOT Cloud)
- **Renombramiento Crítico**: He migrado todas las referencias de la colección `"usuarios"` a **`"clientes"`**. Esto alinea la nomenclatura con `"proveedores"` y cumple con tu requerimiento técnico.
- **Coherencia Core**: El [MotorSincronizacionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/MotorSincronizacionMav.kt) ahora reconoce `"CLIENTE"` como un tipo de identidad de primer nivel.

### 2. Flujo de Login "Grandes Ligas" (Warm-up)
- **Carga Jerárquica**: He actualizado [UsUsuarioMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UsUsuarioMavRepository.kt). Ahora, al iniciar sesión, la app no solo descarga tu perfil personal, sino que también busca y sincroniza tus **Empresas** y **Sucursales** vinculadas.
- **Persistencia Atómica**: El [AutenticacionViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/auth/AutenticacionViewModel.kt) ahora guarda una identidad básica en Room inmediatamente tras el éxito de Firebase Auth. Esto garantiza que el `SyncWorker` siempre tenga datos válidos para subir/espejar en la nube.

### 3. Sincronización Proactiva
- **Espejado Automático**: Cualquier actualización en el perfil del cliente ahora dispara una sincronización inmediata a Firebase.
- **Soberanía de Identidad**: He habilitado la inyección de `EmpresaMavDao` y `SucursalMavDao` en el repositorio de usuario para que la App del Cliente pueda gestionar su ecosistema profesional con la misma potencia que la App del Prestador.

## 🛠️ Verificación Realizada

- **Integridad de Datos**: Se validó el mapeo de `FirebaseUser` a `UsuarioUiModel` incluyendo el correo electrónico, necesario para la creación de la `CuentaMavEntity`.
- **Rastro de Sincronización**: Se confirmó que el `SyncWorker` utiliza el motor centralizado para actualizar tanto la colección `clientes` como el `indice_busqueda`.

---
> [!TIP]
> Con este cambio, la App del Cliente ha dejado de ser un simple visor para convertirse en un gestor de identidad profesional. El flujo de datos es ahora bidireccional y Local-First, garantizando la máxima estabilidad.
