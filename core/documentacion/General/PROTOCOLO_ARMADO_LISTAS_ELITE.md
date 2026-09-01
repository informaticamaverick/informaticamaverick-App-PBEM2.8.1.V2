# 📜 Protocolo de Armado de Listas Elite (v2026.FINAL)

Este protocolo define las leyes técnicas para la construcción, comportamiento y optimización de listas dentro del ecosistema Maverick, basándose en las recomendaciones oficiales de Google y los estándares de alto rendimiento de la arquitectura Elite.

---

## ⚖️ Leyes de la Cascada (Soberanía de Scroll)

### 1. Motor de Renderizado (UI)
Queda estrictamente prohibido el uso de contenedores de scroll tradicionales (`ScrollView`, `Column` con verticalScroll) para listas dinámicas. 
*   **Compose**: Uso obligatorio de **`LazyColumn`** o **`LazyVerticalGrid`**.
*   **Views (Interoperabilidad)**: Uso obligatorio de **`RecyclerView`** con **`ListAdapter`** y **`DiffUtil`**.
*   **Razón**: El sistema solo debe procesar los elementos visibles en el Viewport para garantizar 60 FPS constantes.

### 2. Claves de Identidad (Keys)
Cada elemento de la lista debe poseer un identificador único y estable.
*   **Implementación**: `items(items = lista, key = { it.id })`.
*   **Impacto**: Evita recomposiciones innecesarias de toda la lista cuando solo cambia un ítem o se reordenan. Sin `key`, Compose pierde la trazabilidad del estado interno del componente.

### 3. Carga Multimedia Inteligente (Coil)
La carga de imágenes en listas no debe comprometer la fluidez del scroll.
*   **Librería**: Uso exclusivo de **Coil**.
*   **Higiene**: Las imágenes deben ser redimensionadas (`size`) y formateadas adecuadamente antes de entrar en la memoria caché para evitar el desbordamiento de la RAM.

### 4. Flujo de Datos (UDF & SSOT)
Las listas deben ser emitidas desde el ViewModel como estados inmutables.
*   **Mecanismo**: Uso de **`StateFlow<List<T>>`** o **`PagingData<T>`**.
*   **Higiene**: Nunca pasar listas mutables a la UI. El estado debe ser una fotografía atómica de la Verdad Local (Room).

---

## 🚦 Veredicto: ¿Paging 3 o Lista Plana?

La elección del motor de datos depende de la volumetría y la dinámica del flujo:

### 🟢 Usar Lista Plana (`StateFlow<List<T>>`)
*   **Volumen**: Menos de 100-200 ítems.
*   **Estabilidad**: Los datos son acotados y se consultan en una sola transacción rápida a Room.
*   **Operaciones**: Requieres filtrado instantáneo en memoria, reordenamiento complejo o búsquedas locales ultra-rápidas.

### 🔵 Usar Paging 3 (`collectAsLazyPagingItems`)
*   **Volumen**: Listas que crecen indefinidamente o superan los cientos de registros.
*   **Origen**: Los datos provienen de una API paginada o de un catálogo global dinámico.
*   **Estado**: Necesitas mantener la posición del scroll de forma persistente y combinar datos locales con red mediante `RemoteMediator`.

---

## 🛡️ Tabla de Auditoría de Listas

| Aspecto | Práctica Elite (Mandatoria) | Prohibido |
| :--- | :--- | :--- |
| **Renderizado** | `LazyColumn` / `RecyclerView` | `ScrollView` + `LinearLayout` |
| **Identidad** | Definir `key` única | No definir `key` (Posicional) |
| **Imágenes** | Coil + Redimensionado | Carga manual de Bitmaps |
| **Estado** | `StateFlow` Inmutable | `ArrayList` mutable en Compose |
| **Carga Red** | Paginación (Paging 3) | Descarga masiva (>1MB) en frío |

---

> [!IMPORTANT]
> El incumplimiento de la Ley de Identidad (Keys) es considerado un fallo crítico de arquitectura, ya que destruye la eficiencia de Jetpack Compose en pantallas de alta densidad.
