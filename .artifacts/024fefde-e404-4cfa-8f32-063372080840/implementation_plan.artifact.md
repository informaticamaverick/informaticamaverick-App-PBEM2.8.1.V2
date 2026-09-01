# Plan de Acción: Optimización de Fluidez y Resiliencia de Sincronización

Este plan aborda los problemas de rendimiento (Jank) detectados en teléfonos reales al liberar memoria y los fallos de cancelación en el motor de sincronización.

## User Review Required

> [!IMPORTANT]
> **Optimización de Hilo Principal:** Moveremos el procesamiento de grandes volúmenes de datos (mapeo de categorías) fuera del hilo de la interfaz de usuario. Esto eliminará los congelamientos de ~1 segundo reportados en Logcat.

> [!NOTE]
> **Política de Sincronización:** Cambiaremos la política de WorkManager de `REPLACE` a `KEEP` para evitar que las tareas de sincronización se interrumpan mutuamente, asegurando que la Ley #2 (Costo Zero) se cumpla sin errores.

## Proposed Changes

### [Componente: Core Worker]

#### [MODIFY] [GestorSincronizacionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/worker/GestorSincronizacionMav.kt)
- Cambiar `ExistingWorkPolicy.REPLACE` a `ExistingWorkPolicy.KEEP` en `encolarSincronizacionUsuario` y `encolarSincronizacionPrestador`. Esto permite que una tarea en curso termine antes de iniciar la siguiente del mismo tipo.

### [Componente: Home Feature]

#### [MODIFY] [CategoryViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/CategoryViewModel.kt)
- **Procesamiento Asíncrono**: Asegurar que el mapeo de `allCategories` y `selectedSuperCategoryItems` ocurra en `Dispatchers.Default`.
- **Liberación Suave**: Introducir un pequeño `delay` táctico (100ms) antes de emitir `emptyList()` al cerrar la búsqueda. Esto da margen a que la animación de cierre de la barra Be termine antes de que el Recolector de Basura (GC) inicie la limpieza masiva de objetos, eliminando el Jank visual.

### [Componente: App Features]

#### [MODIFY] [BeAdsManager.kt] (Pendiente localizar)
- Revisar el manejo de estados `NOT_READY` para evitar re-inicializaciones constantes que generen ruido en el log.

---

## Verification Plan

### Automated Tests
- Verificar que el `ObreroSincronizacionMav` no reporte `Job was cancelled` en logs tras múltiples llamadas rápidas.

### Manual Verification
1. **Prueba de Stress (Jank)**: Abrir búsqueda global (500 rubros), escribir y cerrar rápidamente. Verificar en Logcat si el mensaje `Skipped frames` disminuye significativamente.
2. **Sincronización**: Realizar cambios en el perfil y verificar que el log `✅ [WORKER_OK]` aparezca consistentemente sin interrupciones.
