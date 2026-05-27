# 🔥 MÓDULO: PROMOCIONES Y OFERTAS

Este módulo centraliza la orquestación de publicidad, anuncios patrocinados y destacados mediante una lógica de mezcla inteligente.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Screens & Carousels)
*   [`PromoScreen.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/PromoScreen.kt): Feed principal estilo Instagram para ofertas.
*   [`ComponentesReutilizables.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/ComponentesReutilizables.kt): Implementa `PremiumLensCarouselV3` y `BannerItemV3`.

### Lógica (Obreros & Modelos)
*   [`PromoViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/PromoViewModel.kt): **El Orquestador**. Inyecta publicidad y mezcla contenidos.
*   [`AccordionBanner.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/ComponentesReutilizables.kt): Modelo unificado para tarjetas de carrusel.

---

## 🔄 2. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Orquestación de Banners (PromoViewModel)
| Función | Recibe | Entrega | Acción |
| :--- | :--- | :--- | :--- |
| `generateHomeBanners()` | `List<Category>, List<Service>` | `List<AccordionBanner>` | Filtra novedades y promos -> Inyecta Ads -> Devuelve mezcla. |
| `inyectarPublicidad()` | `List<AccordionBanner>` | `List<AccordionBanner>` | **Algoritmo:** Inserta 1 anuncio cada 2 contenidos orgánicos. |
| `updateFilters()` | `Set<String>` | `Unit` | Actualiza `_activeFilters` (Flow). Dispara re-calculo de banners. |

### B. Lógica de Random Estable
*   **Problema:** Los banners cambian de posición aleatoriamente en cada refresco de UI.
*   **Solución Maverick:** Se usa el Hash del ID del servicio como semilla:
    ```kotlin
    val stableSeed = Random(service.id.hashCode().toLong())
    val discount = (10..50).random(stableSeed)
    ```

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo añadir un nuevo tipo de Banner
1.  **Enum:** Añadir el tipo en `BannerType.kt` (ej: `FLASH_SALE`).
2.  **Mapeo:** En `generateHomeBanners()`, crear un nuevo bloque de filtrado para capturar la data del repositorio.
3.  **Visual:** En `PremiumLensCarouselV3`, añadir una rama en el `when(item.type)` para asignar el diseño correspondiente.

### Procedimiento para Promociones Exclusivas
1.  Los prestadores deben tener el flag `isSubscribed = true` en Firestore.
2.  El Obrero de Promociones mapea estos servicios y les asigna un color distintivo (`Color(0xFFE91E63)`).
3.  Al tocar un banner, se debe usar `navController.navigate("perfil_prestador/${banner.service.id}")`.

---

## 💾 4. RELACIÓN CON FIREBASE Y ROOM

*   **Firebase Storage:** Almacena los `imageUrl` de los anuncios patrocinados.
*   **Room:** El Obrero lee las categorías con `isNew = true` para llenar los banners de "Novedades".
*   **Costo Cero:** La lógica de mezcla se hace enteramente en memoria (Kotlin) sin realizar peticiones extra a Firestore.
