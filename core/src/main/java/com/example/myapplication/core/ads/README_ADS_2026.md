# 🚀 Protocolo de Publicidad Maverick Elite 2026

Este documento detalla la arquitectura de monetización premium integrada en el ecosistema Maverick (App Usuario y App Prestador).

## 📊 Estrategia de Monetización y Costos (Proyectados)

En 2026, la app utiliza un modelo híbrido basado en **eCPM (Costo por cada 1000 impresiones)** de alta retención.

| Formato | Ubicación Táctica | eCPM Est. | Paga más por... |
| :--- | :--- | :--- | :--- |
| **Native Advanced (Instagram Style)** | PromoScreen (Feed) | $4.50 - $12.00 | Integración orgánica y clicks de alta calidad. |
| **Rewarded Interstitial (Video)** | FastScreen / Licitaciones | $25.00 - $45.00 | "Value Exchange": El usuario ve el video para obtener un beneficio. |
| **Native Carousel** | HomeScreen (Marquesinas) | $3.00 - $7.00 | Visibilidad constante en la pantalla de inicio. |
| **Adaptive Banners** | Footers de Listas | $0.50 - $2.00 | Volumen de impresiones. |

## 🏗️ Arquitectura Técnica

La implementación está centralizada en el módulo `:ui-shared` para garantizar consistencia visual.

### 1. Componentes de UI (`:ui-shared`)
*   **`InstagramNativeAdCard.kt`**: Reclama un `NativeAd` y lo renderiza con el diseño exacto de las promociones de la app. Incluye soporte para `MediaView` (Video Auto-play).
*   **`NativeCarouselAdCard.kt`**: Versión optimizada para el carrusel horizontal de la Home.
*   **`RewardedInterstitialVideoAd.kt`**: Componente de "Espera Activa". Muestra un video mientras la app realiza tareas en segundo plano (Búsqueda de prestadores o Publicación de licitaciones).
*   **`GoogleMorphingLoader.kt`**: Loader premium que se muestra antes y después de los anuncios para suavizar las transiciones.

### 2. Gestión de Datos (`:core`)
*   **`BeAdsManager.kt`**: Único punto de inicialización. Contiene el **Kill Switch** global (`isAdSystemEnabled`) para desactivar anuncios en caso de errores o auditorías.

## 🛠️ Archivos Clave para Mantenimiento
- **Diseño de Tarjetas**: `D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/AdMobComponents.kt`
- **Integración Home**: `D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/CarruselPromocionesV3.kt`
- **Lógica de Recompensas (Fast)**: `D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/FastScreen.kt`

## 📈 Mejores Prácticas 2026
1.  **No usar Banners simples**: Google penaliza estéticamente las apps premium que usan banners intrusivos.
2.  **Native-First**: Siempre preferir `NativeAd` sobre `AdView`.
3.  **Video Ads**: Los anuncios de video deben ser el 70% de la carga publicitaria en pantallas críticas para maximizar el ROI.
