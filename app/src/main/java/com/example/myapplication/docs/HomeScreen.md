# 🏠 MÓDULO: HOMESCREEN CLIENTE (Orquestación Bento)

Este documento detalla el funcionamiento técnico de la pantalla principal, sus flujos de datos y procedimientos de modificación.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Screens & Components)
*   [`HomeScreenCliente3.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/HomeScreenCliente3.kt): Orquestador Stateful. Recolecta flujos de Obreros y Cerebro.
*   [`TarjetaCategoria2.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/TarjetaCategoria2.kt): Implementa `CompactCategoryCard` y `BentoSuperCategoryCard`.

### Lógica (Obreros & Mediadores)
*   [`CategoryViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/CategoryViewModel.kt): **El Obrero**. Procesa categorías.
*   [`AppActionCoordinator.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/AppActionCoordinator.kt): **El Mediador**. SSOT de búsqueda y ubicación.
*   [`BeBrainViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/BeBrainViewModel.kt): **El Cerebro**. Gestiona el HUD y visibilidad de paneles.

---

## 🔄 2. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Gestión de Categorías (CategoryViewModel)
| Función | Recibe | Entrega | Acción |
| :--- | :--- | :--- | :--- |
| `syncCategoriesWithFirebase()` | N/A | `Job` | Llama a `repository.syncWithFirebase()`. Descarga de Firestore a Room. |
| `toggleSortFilter(id: String)` | `filterId` | `Unit` | Actualiza `_activeSortFilters` (StateFlow). Cambia de vista Bento a Lista. |
| `selectSuperCategoryForDetail(t)`| `title?` | `Unit` | Activa la carga perezosa de los items de una supercategoría. |
| `toggleCategoryFavorite(cat)` | `CategoryEntity` | `Unit` | Actualiza Room (`isFavorite = !isFavorite`). |
| `updateSearchQuery(query)` | `String` | `Unit` | Notifica al `Mediador`. Dispara búsqueda delegada en SQL. |

### B. Flujo de Datos Optimizado (Lazy Flow)
La Home utiliza una arquitectura de carga en dos niveles para maximizar el rendimiento:

1.  **Metadatos (Light):** Se usa `CategoryDao.getSuperCategoryMetadata()` para obtener una lista de `SuperCategoryLight`. SQLite realiza el `GROUP BY` y `COUNT(*)` internamente.
2.  **Entidades (Full):** Cuando se abre el `SuperCategoryDetailsPanel`, el ViewModel reacciona al cambio de título y ejecuta `getCategoriesBySuperCategory(title)`, trayendo los objetos `CategoryEntity` completos solo para esa sección.

### B. Identidad Visual
*   **Función:** `CategoryVisuals.getColorFor(superCategory: String?): Long`
*   **Ubicación:** `CategoryViewModel.kt`
*   **Detalle:** Retorna un color hexadecimal (Mate/Pastel) basado en el nombre de la supercategoría.

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo crear/modificar una Supercategoría
1.  **En Firestore:** Añadir un nuevo valor en el campo `superCategory` de los documentos en la colección `"Servicios"`.
2.  **En Kotlin (`CategoryVisuals`):** Agregar el nombre exacto de la supercategoría y su color asignado en el mapa `superCategoryColors`.
3.  **En la UI:** La función `BentoSuperCategoryCard` se renderizará automáticamente al detectar el nuevo grupo tras la sincronización.

### Cómo cambiar el modo de vista (Bento vs Individual)
1.  **Función:** `toggleSortFilter("view_grid")` o `toggleSortFilter("view_bento")`.
2.  **Efecto:** Cambia la cantidad de columnas en el `LazyVerticalGrid` de `HomeScreenContent`.
3.  **Lógica:**
    ```kotlin
    columns = GridCells.Fixed(if (isSuperCategoryView) 2 else 3)
    ```

---

## 💾 4. PERSISTENCIA Y RED

### Estrategia "Costo Cero"
1.  **Lectura Ligera:** La Home observa metadatos optimizados. Se evita el agrupamiento en memoria de Kotlin.
2.  **Búsqueda Delegada:** El filtrado de texto se delega a Room mediante `LIKE %query%`, evitando el uso de `.filter {}` en listas grandes.
3.  **Escritura:** `syncCategoriesWithFirebase` usa una corrutina en `Dispatchers.IO` para no bloquear la UI.
3.  **Room DAO:** `CategoryDao.getAllCategories()` devuelve un `Flow`, lo que garantiza que cualquier cambio en la DB (local o por sync) actualice la pantalla instantáneamente.

---

## 🤖 5. INTEGRACIÓN CON ASISTENTE BE
*   **Búsqueda Inteligente:** Be Assistant llama a `beViewModel.updateSearchQuery(it)`. 
*   **Reacción:** El Obrero de Categorías reacciona mediante `coordinator.globalSearchQuery.collect`. 
*   **Resultados:** Si no hay matches, el Obrero pone `_hasMatches.value = false`, y Be Assistant cambia su expresión a `SAD` o `THINKING`.
