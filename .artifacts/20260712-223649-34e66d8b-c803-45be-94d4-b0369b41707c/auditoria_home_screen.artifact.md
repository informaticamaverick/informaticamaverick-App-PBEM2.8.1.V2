# Auditoría Técnica: Home Screen (V2026.FINAL)

He realizado una auditoría profunda de la pantalla de inicio y su infraestructura de datos. A continuación, detallo los hallazgos y el rastro del fallo detectado en tu imagen.

## 📊 1. Diagnóstico de Fallos Visuales (Basado en Captura)

### A. Cabecera (Identity & Location Slot)
*   **Problema**: El nombre del usuario aparece truncado ("USUA...") y la foto de perfil es un icono genérico de `Person`.
*   **Causa Raíz**:
    1.  **Fallo de SSOT**: El `BeBrainViewModel` está mapeando `accountState` de forma reactiva, pero el `HomeScreenViewModel` está instanciando localmente un `IdentidadUsuarioViewModel`. Esto genera dos fuentes de verdad para el mismo perfil.
    2.  **Inmediatez (Ley #4)**: El `direccionActiva` está en estado `SCANNING...` pero el clima ya cargó. Esto indica que el flujo de coordenadas GPS terminó pero la geocodificación inversa (de lat/lng a calle) falló o está bloqueada por el hilo principal.

### B. Carrusel de Promociones
*   **Problema**: Se muestra un espacio negro vacío.
*   **Causa Raíz**:
    1.  **Sincronización Paging**: El `feedPagingData` en `PromoViewModel` depende del `activeZipCode`. Si la geocodificación está bloqueada (como se ve en la cabecera), el código postal es `null` y el carrusel nunca recibe items.
    2.  **Error de Inicialización**: La función `generateHomeBanners` requiere que tanto `allRawCategories` como `localPromotions` estén listos. Si uno falla, la lista de banners es `emptyList()`, activando el esqueleto de carga infinito.

---

## 🏛️ 2. Análisis de Arquitectura y Leyes Maverick

### ⚖️ Ley #1: Pantallas Tontas (VIOLADA)
*   **Hallazgo**: `HomeScreenComplete` tiene lógica de permisos de Android y lanzadores de GPS.
*   **Impacto**: Esto ralentiza la renderización inicial (TTC) y ensucia el orquestador. Las leyes dictan que esto debe vivir en un Repositorio o un Obrero especializado (`UbicacionClimaViewModel`).

### ⚖️ Ley #3: Carga On-Demand (OPTIMIZABLE)
*   **Hallazgo**: Se están cargando todos los ViewModels (`user`, `category`, `promo`, `ubicacion`) simultáneamente en el arranque.
*   **Impacto**: Pico de consumo de RAM al inicio. Una app de "Grandes Ligas" (Netflix/Instagram) carga primero el esqueleto y luego dispara los hilos de datos de forma escalonada.

### ⚖️ Ley #7: Trazabilidad Hormiga (OPTIMIZABLE)
*   **Hallazgo**: Faltan logs tácticos en la transición de `produceState` para los banners. Si el mapper falla, la app muere en silencio con una caja negra.

---

## 🚀 Plan de Mejora "Grandes Ligas"

1.  **Unificación de Identidad**: El `BeBrainViewModel` debe ser la **Única Fuente de Verdad** para el perfil en Home. Debemos eliminar la dependencia de `IdentidadUsuarioViewModel` en esta pantalla.
2.  **Pipeline de Ubicación Asíncrono**: Mover la lógica de permisos al `UbicacionClimaViewModel`. La UI solo debe decir "Quiero mi ubicación" y reaccionar al `UiState` resultante.
3.  **Gestión de Animaciones Pesadas**: Implementar la detención de animaciones de carrusel mientras se hace scroll en la lista de categorías (Ley de Rendimiento).
4.  **Skeleton Inteligente**: Mejorar el `CarouselSkeleton` para que ocupe el espacio exacto y evitar el "salto" visual (Layout Shift) cuando cargan las promos.

---
> [!IMPORTANT]
> El flujo de datos actual es "Push" (la UI intenta forzar los datos). Para ser de Grandes Ligas, el flujo debe ser **"Pull"** (la UI se suscribe a estados inmutables que emiten los obreros).

**Estado de la Auditoría**: Completada. No se han realizado cambios en el código.
