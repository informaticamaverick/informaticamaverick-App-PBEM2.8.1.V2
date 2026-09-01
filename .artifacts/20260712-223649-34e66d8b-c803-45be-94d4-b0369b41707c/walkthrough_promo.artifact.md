# Walkthrough: Sincronización de Promociones con el Núcleo Core (V2026)

He realizado una refactorización profunda para asegurar que la pantalla de promociones (`PromoScreen.kt`) y su lógica asociada respeten al **Módulo Core** como la única fuente de verdad (SSOT).

## 🚀 Cambios Principales

### 1. Refactorización del Repositorio (`PromotionRepository.kt`)
- **Alineación de Dominio**: El repositorio ahora devuelve modelos de dominio `Promotion` y `PromoComment` en lugar de entidades de Room.
- **Ley de Inmediatez**: Se implementó `toggleLike` con actualización reactiva local, asegurando que el corazón rojo se refleje instantáneamente sin esperar a la nube.

### 2. Fortalecimiento del ViewModel (`PromoViewModel.kt`)
- **Implementación de Paginación**: Se creó `PromoPagingSource` para manejar el feed infinito con inyección táctica de anuncios de Google.
- **Gestión de Estado Completa**: Se restauraron los flujos de estado para filtros (tipos, categorías, supercategorías) que estaban ausentes, permitiendo que la `PromoFilterBar` sea plenamente funcional.
- **Centralización de Mapeo**: El ViewModel ahora delega la transformación de datos a las entidades del Core.

### 3. Sincronización de Entidades (`PromotionEntity.kt`)
- **Mapeadores Internos**: Se añadieron funciones `aModelo()` y `toEntity()` para facilitar la conversión bidireccional entre la base de datos y el dominio, manejando correctamente la serialización JSON de imágenes y categorías.

### 4. Corrección de la UI (`PromoScreen.kt`)
- **Resolución de Referencias**: Se corrigieron todos los errores de "Unresolved reference" (ej: `feedPagingData`, `isDiscoveryMode`, `onRouteChanged`).
- **Navegación Táctica**: Se sincronizó el `LaunchedEffect` con la función real del cerebro (`alCambiarRuta`).

## 🛠️ Verificación Realizada

- **Análisis Estático**: Pasé el analizador sobre `PromoScreen.kt`, confirmando que ya no existen errores de referencia ni de tipos.
- **Compilación de Módulos**: Verifiqué que los cambios en `:core` (Entidades y Repositorios) no rompen la integridad del sistema.

---
> [!TIP]
> Con esta arquitectura, el feed de promociones ahora es robusto, soporta anuncios nativos y sigue estrictamente las leyes Maverick de **Costo Zero** y **Single Source of Truth**.
