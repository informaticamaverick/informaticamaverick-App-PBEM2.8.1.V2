# Plan de Acción: Refuerzo de Login y Carrusel Elite (V2026)

Este plan integra las recomendaciones para unificar el flujo de autenticación, asegurar la persistencia local y optimizar el carrusel compartido, cumpliendo con la **Ley #9 (Idioma Español)** y el estándar de **"Grandes Ligas"**.

## 📊 1. Rediseño del Flujo de Autenticación (Offline-First)

### [NUEVO] `GestorArranqueMav.kt` (Reemplaza lógica en `BeBrain`)
- **Propósito**: Único responsable de verificar la sesión inicial y decidir la ruta de navegación.
- **Lógica**:
    1.  Si `auth.currentUser != null`:
        - Verificar existencia de `CuentaMavEntity` en Room.
        - Lanzar `SyncWorker` para refrescar perfil en segundo plano.
        - Navegar a `main_screen`.
    2.  Si es nulo: Navegar a `login_screen`.

### Refactor de `AutenticacionViewModel.kt` (Ex `LoginViewModel`)
- **Sincronización**: Al igual que la App del Prestador, este ViewModel disparará la subida de datos a Firebase tras un registro exitoso.
- **Persistencia Atómica**:
    ```kotlin
    // Al loguear con éxito:
    repositorioCuenta.crearCuentaLocal(uid, email) // Asegura SSOT en Room
    syncManager.enqueueUserSync(uid) // Lanza Worker del Core para bajar perfil
    ```

---

## 🏗️ 2. Carrusel y Google Ads (UI-Shared Core)

### Auditoría de `CarruselPromocionesV3.kt`
- **Estado**: Ya soporta el modelo híbrido (Promociones reales + Google Ads).
- **Mejora de Rendimiento**: Optimizar el uso de `beyondViewportPageCount` para que los anuncios no consuman recursos si no están visibles.
- **Coherencia Core**: Las tarjetas de Ads (`NativeCarouselAdCard`) están centralizadas en `ui-shared`, permitiendo que ambas apps (Cliente y Prestador) muestren publicidad de alta calidad con el mismo código.

---

## 🔄 3. Integración con Core (Worker & Sync)

### Uso de `SyncManager` y `SyncWorker`
- Utilizaremos el `SyncManager` del Core para encolar tareas de sincronización tras el login.
- **Beneficio**: El usuario entra a la App instantáneamente con sus datos locales (Room), mientras el `SyncWorker` actualiza el perfil en segundo plano sin bloquear la UI.

---

## 🚀 Pasos de Ejecución (Estándar Mav 2026)

1.  **Saneamiento de Nombres**: Renombrar `UsAuthRepository` a `UsAutenticacionRepository` y `LoginViewModel` a `AutenticacionViewModel`.
2.  **Implementación del Gestor**: Crear `GestorArranqueMav` y limpiar el `BeBrainViewModel`.
3.  **Refuerzo de Login**: Asegurar que el login escriba en `CuentaMavDao` antes de navegar.
4.  **Validación de Carrusel**: Revisar que los banners de Google Ads se carguen correctamente usando los IDs de prueba del `BeAdsManager`.

---
> [!IMPORTANT]
> Todo el código se escribirá estrictamente en **Español**. Las apps de "Grandes Ligas" no solo funcionan bien, sino que tienen una estructura interna impecable y predecible.

**¿Procedo con la implementación de este nuevo flujo de autenticación soberano?**
