# Plan de Unificación y Optimización del Sistema de Promociones

Este plan detalla la reestructuración del sistema de promociones para cumplir con los estándares de nomenclatura del proyecto (Mav en Español), optimizar el rendimiento mediante el uso correcto de `RemoteMediator` y mejorar la inyección de publicidad en el feed.

## User Review Required

> [!IMPORTANT]
> Se realizará un renombrado masivo de `Promotion` a `Promocion`. Esto afectará a gran parte de los módulos `:app`, `:core`, `:prestador` y `:ui-shared`. Se recomienda realizar un backup o commit previo si hay cambios locales sin guardar.

> [!NOTE]
> La lógica de paginado cambiará de una carga manual en memoria (`PromoPagingSource`) a una carga reactiva desde base de datos con sincronización remota (`RemoteMediator`). Esto mejorará drásticamente el consumo de RAM y la fluidez del scroll.

## Proposed Changes

### 1. Estandarización de Nomenclatura (EN -> ES)

Se renombrarán los archivos y símbolos de "Promotion" a "Promocion" para seguir la **Ley #9** del proyecto.

#### [MODIFY] [Promocion.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/modelos/Promocion.kt) (Renombrado desde Promotion.kt)
#### [MODIFY] [PromocionDao.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/dao/PromocionDao.kt) (Renombrado desde PromotionDao.kt)
#### [MODIFY] [PromocionEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/entidades/PromocionEntity.kt) (Renombrado desde PromotionEntity.kt)
#### [MODIFY] [PromocionLikeEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/entidades/PromocionLikeEntity.kt) (Renombrado desde PromotionLikeEntity.kt)

---

### 2. Optimización del Flujo de Datos (Core)

#### [MODIFY] [PromocionRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/repositorios/PromocionRepository.kt)
- Centralizar la lógica de consulta para que coincida exactamente con lo que el UI necesita.
- Asegurar que `obtenerPromocionesPaginadas` exponga las entidades correctamente para el ViewModel.

#### [MODIFY] [PromocionRemoteMediator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/repositorios/PromocionRemoteMediator.kt)
- Refinar la lógica de inserción para evitar duplicados y manejar correctamente la expiración.

---

### 3. Orquestación del Feed y Publicidad (App Azul)

#### [MODIFY] [PromoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/home/PromoViewModel.kt)
- **Eliminar `PromoPagingSource`**: Reemplazar por el flujo de `PromocionRepository`.
- **Inyección de Ads**: Utilizar `PagingData.insertSeparators` o transformaciones similares para inyectar los anuncios de Google Ads cada N promociones de forma eficiente.
- **Limpieza de Pool de Ads**: Optimizar la pre-carga para no saturar el hilo principal.

---

### 4. Limpieza de Componentes UI

#### [MODIFY] [TarjetasPromocionesCompartidas.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/ui/components/TarjetasPromocionesCompartidas.kt)
- Eliminar `AppBannerCard` (Obsoleto).
- Asegurar que `InstagramPromoCard` use el nuevo modelo unificado.

## Verification Plan

### Automated Tests
- Ejecutar compilación del proyecto para asegurar que no queden referencias rotas tras el renombrado.
- Verificar logs de `PROMO_CASCADA` y `PROMO_FIRESTORE` para confirmar que la carga paginada funciona.

### Manual Verification
- Abrir la App Azul y navegar al feed de promociones.
- Confirmar que aparecen anuncios entre las promociones.
- Verificar el carrusel del Home para asegurar que las promociones y anuncios se muestran correctamente.
