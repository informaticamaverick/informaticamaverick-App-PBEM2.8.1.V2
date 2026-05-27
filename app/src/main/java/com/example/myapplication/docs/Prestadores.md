# 👥 MÓDULO: PRESTADORES (PROVIDERS)

Gestión unificada de perfiles técnicos, búsqueda por cercanía y algoritmo de relevancia táctica.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Screens & Cards)
*   [`ResultBusquedaCategoriaScreen.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/ResultBusquedaCategoriaScreen.kt): Lista reactiva de prestadores filtrados.
*   [`PerfilPrestadorCliente.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/PerfilPrestadorCliente.kt): Vista detallada del profesional con soporte multi-empresa.

### Lógica (Obreros & Modelos)
*   [`ProviderViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/ProviderViewModel.kt): **El Obrero**. Procesa la geolocalización, filtrado regional y transformación de modelos.
*   [`ProviderRepository.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/ProviderRepository.kt): Sincronización entre Room y Firestore.

---

## 🔄 2. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Sincronización Remota (Firebase -> Room)
| Función | Recibe | Entrega | Acción |
| :--- | :--- | :--- | :--- |
| `searchAndSyncProviders()` | `zipCode, category` | `Unit` | Consulta Firestore -> Mapea a Entity -> Inserta en Room. |
| `fetchAndSyncProviderDetail()`| `providerId` | `Unit` | Descarga Perfil + Empresas + Sucursales + Empleados (Perfil Profundo). |

### B. Algoritmo de Cercanía (Haversine)
El cálculo de distancia se realiza en el Obrero para no penalizar el renderizado de la UI.
*   **Función:** `calculateDistance(lat1, lon1, lat2, lon2): Double`
*   **Mapeo de Rangos Visuales:**
    ```kotlin
    val ranges = listOf(
        Triple(0.0..1.0, "En un radio de 1 km", "🚶"),
        Triple(1.0..3.0, "En un radio de 3 km", "🛵"),
        Triple(3.0..Double.MAX_VALUE, "Más lejos", "🚗")
    )
    ```

### C. Transformación Unificada (Mapper)
*   **Función:** `transformToUnified(Provider, UserLocation?): ServiceDisplayModel`
*   **Lógica:** Normaliza perfiles individuales y corporativos en un solo modelo de UI. Prioriza el nombre de la empresa si el profesional así lo configuró (`priorizarEmpresa`).

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo buscar prestadores por zona (CP)
1.  **Detección:** El `AppActionCoordinator` informa la ubicación activa (vía GPS o Perfil).
2.  **Consulta Firestore:** `repository.searchAndSyncProviders` realiza una query filtrada por `ubicacion.codigoPostal` y `servicios`.
3.  **Fallback:** Si no hay coincidencias exactas por CP, se realiza una búsqueda amplia solo por categoría.
4.  **Local First:** Los resultados se guardan en Room. El Obrero observa `allProviders` de Room para mostrar la data instantáneamente.

### Cómo actualizar el estado de Favorito
1.  **Acción:** El usuario toca el corazón en `PrestadorCardV3`.
2.  **Kotlin:** `viewModel.toggleFavorite(id, currentStatus)`.
3.  **Local:** `repository.updateFavoriteStatus(id, !status)` actualiza Room.
4.  **Remoto:** La sincronización con Firestore es gestionada por el Obrero del Perfil (Usuario).

---

## 💾 4. RELACIÓN CON FIREBASE Y ROOM

*   **Firestore:** Colección `providers`. Contiene el perfil maestro. Subcolecciones `companies` -> `branches` para estructuras jerárquicas complejas.
*   **Room:** Tabla `providers`. El campo `companies` se almacena como una lista convertida vía TypeConverters.
*   **Ahorro de Costos:** No se descarga el perfil profundo (empleados, galerías) hasta que el usuario hace clic en el prestador.
