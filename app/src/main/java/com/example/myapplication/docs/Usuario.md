# 👤 MÓDULO: PERFIL DE USUARIO Y EMPRESAS

Este módulo centraliza la identidad del cliente, sus lugares guardados y la gestión de entidades corporativas asociadas.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Screens & Forms)
*   [`PerfilUsuarioScreen.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/PerfilUsuarioScreen.kt): Orquestador Stateful. Maneja el Pager entre perfil Personal y Business.
*   [`TarjetasEdicionPerfilUser.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/TarjetasEdicionPerfilUser.kt): Formularios de entrada de datos.

### Lógica (Obreros & Mediadores)
*   [`ProfileViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/ProfileViewModel.kt): **El Obrero**. Gestiona el `ProfileUiState` y la lógica de validación.
*   [`UserRepository.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UserRepository.kt): Sincronización jerárquica con Firestore y Storage.

---

## 🔄 2. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Persistencia y Sincronización (ProfileViewModel)
| Función | Recibe | Entrega | Acción |
| :--- | :--- | :--- | :--- |
| `saveProfile()` | N/A | `Unit` | Valida inputs -> Construye `UserEntity` -> Llama a `userRepository.syncUserWithFirebase`. |
| `updateProfilePhoto()` | `Uri` | `Unit` | Comprime -> Sube a Storage -> Actualiza `photoUrl` en Room y Firestore. |
| `loadUserProfileIntoUiState()` | `Flow<User>` | `Unit` | **Init:** Observa Room y mapea los datos al estado de edición temporal. |

### B. Gestión de Direcciones
*   **Función:** `fetchAddress(lat, lon)`
*   **Ubicación:** `ProfileViewModel.kt`
*   **Detalle:** Usa `Geocoder` de Google Maps para obtener calle, número y **Código Postal**. Este último es crítico para el filtrado zonal en toda la app.

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo añadir una nueva Dirección
1.  **Captura:** El usuario usa `getCurrentLocation()` (GPS) o completa el formulario manual.
2.  **Validación:** Maverick exige que el campo `codigoPostal` no sea nulo.
3.  **Persistencia:**
    *   `updatePersonalAddresses(newList)`: Actualiza el estado local.
    *   `savePersonalAddressesToFirebase()`: Actualiza la subcolección `personalAddresses` en Firestore.

### Cómo agregar una Empresa asociada
1.  **Activación:** El usuario marca el switch `isEmpresa`.
2.  **Kotlin:** Se añade un nuevo objeto `CompanyClient` a la lista `uiState.companies`.
3.  **Jerarquía:** Las empresas contienen `branches` (sucursales), y estas contienen `representatives`. Todo el árbol se guarda en un solo JSON en Room, pero se atomiza en Firestore.

---

## 💾 4. RELACIÓN CON FIREBASE Y ROOM

*   **Firestore:** Colección principal `usuarios/{uid}`. Almacena el perfil maestro.
*   **Room:** Tabla `users`. La UI observa `userProfile: Flow<UserEntity?>` para reaccionar a cambios externos (como una sincronización de fondo).
*   **Firebase Storage:** Almacena fotos bajo la ruta `users/{uid}/avatar.webp` y `users/{uid}/banner.webp` con compresión automática.

---

## 🤖 5. INTEGRACIÓN CON HUD
*   **Acciones Be:** El Obrero define los botones "EDITAR", "GUARDAR" y "CANCELAR" a través del flujo `beActions: StateFlow<List<BeSmallActionModel>>`.
*   **Mediador:** Al guardar una dirección como principal, el Obrero debe notificar al `AppActionCoordinator` para refrescar los resultados de búsqueda en la Home.
