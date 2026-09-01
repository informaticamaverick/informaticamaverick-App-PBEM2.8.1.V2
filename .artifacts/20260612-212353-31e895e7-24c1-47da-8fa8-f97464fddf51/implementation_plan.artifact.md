# Plan de Implementación: Sistema de Promociones e Historias (Grandes Ligas)

Este plan detalla los cambios necesarios para estandarizar el flujo de promociones e historias, asegurando paridad visual (estilo Instagram), soporte multiperfil completo y ruteo geográfico mediante FCM Topics.

## Cambios Propuestos

### 1. Núcleo Core (:core)
Actualización del modelo de datos y persistencia para soportar la nueva lógica de interacción y ruteo.

#### [Promotion.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/domain/model/Promotion.kt)
- Añadir campo `zipCode: String?` para ruteo geográfico.
- Añadir campo `isVerified: Boolean` para paridad visual de confianza.
- Añadir campo `isLiked: Boolean` para estado local.

#### [PromotionEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/PromotionEntity.kt)
- Sincronizar campos con el modelo de dominio.
- Añadir índice por `zipCode` para búsquedas locales rápidas.

#### [PromotionRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/PromotionRepository.kt)
- `syncRemotePromotions(zipCode: String?)`: Soporte para filtrado remoto por zona.
- `likePromotion(id: String)`: Implementación de actualización optimista (Instagram Style).
- `sendPromotionNotification(promo: Promotion)`: Lógica para disparar el ruteo vía Topic `promos_{zipCode}`.

---

### 2. App del Prestador (:prestador)
Habilitar la capacidad de publicar historias bajo diferentes identidades.

#### [CreatePromotionScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/promotion/CreatePromotionScreen.kt)
- **Selector de Perfil**: Lista horizontal de avatares (Personal + Empresas).
- **Feedback Visual**: Integración de Toasts y validaciones mejoradas.
- **Herencia de Datos**: La promo toma automáticamente el CP y nombre de la empresa/perfil seleccionado.

#### [CreatePromotionViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/promotion/CreatePromotionViewModel.kt)
- Integrar el campo `zipCode` y disparar la notificación al finalizar la publicación.

---

### 3. App del Usuario (:app)
Optimizar la recepción y el feed para ser 100% local y reactivo.

#### [AppActionCoordinator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/AppActionCoordinator.kt)
- **Automatización de Topics**: Suscripción reactiva a `promos_{CP}` cuando cambia la dirección activa (por GPS, selección manual o cambio de perfil).

#### [PromoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/PromoViewModel.kt)
- Filtrar `stories` y `promotions` basándose en el CP del perfil activo del usuario.

---

### 4. UI Compartida (:ui-shared)
Mejorar los componentes para una experiencia de usuario premium.

#### [TarjetasPromocionesCompartidas.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/TarjetasPromocionesCompartidas.kt)
- `InstagramPromoCard`: Soporte para insignia de verificado, animaciones de like y contador reactivo.
- `StoryItem`: Añadir insignia de verificado en la burbuja de la historia.

## Plan de Verificación

### Pruebas Manuales
1. **Flujo de Publicación (Prestador):**
    - Seleccionar el perfil de una empresa.
    - Publicar una historia.
    - Verificar que en el Logcat aparezca: `[PUBLISH_SUCCESS] Topic: promos_{CP_EMPRESA}`.
2. **Flujo de Recepción (Usuario):**
    - Cambiar de dirección o perfil en la Home.
    - Verificar en Logcat la suscripción: `📡 [SYNC_ZONE] Suscrito a topic de promociones: promos_{NUEVO_CP}`.
    - Navegar a la pantalla "Descubrir" y verificar que solo aparezcan historias de esa zona.
3. **Interacción Social:**
    - Realizar doble tap en una imagen.
    - Verificar que el corazón aparezca y el contador de likes se incremente instantáneamente.
