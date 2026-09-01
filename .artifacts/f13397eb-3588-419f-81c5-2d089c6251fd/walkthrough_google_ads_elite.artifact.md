# Walkthrough - Integración Elite de Google Ads

He optimizado el sistema de publicidad nativa de Google (AdMob) para que se integre de forma fluida y profesional en ambas aplicaciones, siguiendo los estándares de las "Grandes Ligas" como Instagram.

## Cambios Realizados

### 💎 Estrategia de Monetización Orgánica
- **Carrusel de Inicio (Home)**: Se modificó el generador de banners en [PromoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/PromoViewModel.kt) para inyectar automáticamente un anuncio nativo entre las promociones reales.
    - El anuncio aparece de forma estratégica para maximizar la visibilidad sin interrumpir la experiencia del usuario.
- **Feed de Promociones**: La inyección de anuncios cada 4 publicaciones en el feed infinito ahora es más robusta, utilizando el componente `InstagramNativeAdCard`.

### 🎨 Refinamiento Visual AdMob
- **Estética Instagram**: Se actualizó [AdMobComponents.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/AdMobComponents.kt) para que los anuncios nativos coincidan con el diseño Cyber de Maverick:
    - **Botón CTA**: Ahora es un botón con borde neón y fondo transparente, mucho más integrado que el diseño estándar de Google.
    - **Insignia AD**: Se rediseñó la etiqueta "Patrocinado" para que sea discreta pero legible.
- **Carrusel Adaptativo**: Se eliminaron alturas fijas en las tarjetas de anuncios del carrusel para que se adapten perfectamente al contenedor de 160.dp del [CarruselPromocionesV3.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/CarruselPromocionesV3.kt).

### 🛠️ Infraestructura Core
- **Helper de Configuración**: Se añadió `buildNativeAdOptions` en [BeAdsManager.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/ads/BeAdsManager.kt) para centralizar la configuración de AdChoices y carga de imágenes.
- **Limpieza de Código**: Se eliminaron parámetros redundantes y se unificó la lógica de carga para evitar parpadeos visuales (Jank) durante el scroll.

## Verificación de Resultados

### Pruebas Técnicas
- **Compilación**: Ambas aplicaciones (`:app` y `:prestador`) compilan correctamente.
- **Rendimiento**: Se implementó `beyondViewportPageCount = 0` en el Pager del carrusel para liberar RAM de anuncios que no están en pantalla.

## Recomendación de Uso
> [!TIP]
> Durante el desarrollo, el sistema utiliza los **IDs de prueba oficiales** de Google. Esto evita que tu cuenta sea suspendida por clicks accidentales. Al pasar a producción, solo deberás cambiar los IDs en el `BeAdsManager.kt`.
