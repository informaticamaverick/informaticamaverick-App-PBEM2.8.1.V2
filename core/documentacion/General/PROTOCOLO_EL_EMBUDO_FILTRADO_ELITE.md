# 🌪️ Protocolo: El Embudo (Filtrado en la Fuente v2026.ELITE)

El protocolo **"El Embudo"** establece el estándar de oro para el manejo de grandes volúmenes de datos en el ecosistema Maverick. Su objetivo es garantizar que la aplicación sea instantánea, ahorre batería y nunca sufra bloqueos de memoria (OOM), independientemente de si el usuario tiene 10 o 10,000 elementos.

---

## 🏛️ La Filosofía: SQL-First
En Maverick, la memoria RAM es un recurso sagrado. Por lo tanto, prohibimos el filtrado manual usando `.filter { }` de Kotlin en los ViewModels para cualquier lista que pueda crecer indefinidamente. El filtrado **DEBE** ocurrir en la fuente: el motor de base de datos (Room).

---

## 🛠️ Las 4 Etapas del Embudo

### 1. Captura de Intención (UI)
La UI (usando `BarraFiltrosV3`) recolecta los deseos del usuario. Cada toque en un chip o búsqueda genera un **Intent**.
*   **Responsabilidad**: `BarraFiltrosV3` envía un evento al ViewModel.
*   **Portavoz**: Be (HUD) solo dibuja lo que se le pide; no conoce la lógica.

### 2. El Comandante de la Consulta (ViewModel)
El ViewModel mantiene una instancia del **Contrato de Filtro** (data class estructurada).
*   **Herramienta**: `MutableStateFlow<FiltrosSoberanos>`.
*   **Acción**: Al recibir un intent, actualiza la data class de forma atómica.
*   **Mecanismo**: Usa `combine` o `flatMapLatest` para observar cambios en los filtros y disparar la re-ejecución del flujo de datos.

### 3. El Traductor de Negocio (Repository)
El repositorio recibe la data class de filtros y decide cómo pedirle los datos al sistema.
*   **Decisión**: Si el filtro es simple, llama a un método específico del DAO. Si es complejo, construye una consulta dinámica o usa un `DatabaseView`.

### 4. La Ejecución Atómica (Data Source / SQL)
El motor de base de datos ejecuta la consulta y devuelve únicamente los resultados necesarios.
*   **Técnica**: Uso de **Paging 3** para carga por demanda.
*   **Optimización de Menús**: Para obtener los rubros o metadatos dinámicos del filtro (ej: "¿Qué categorías tienen concursos activos?"), es obligatorio usar un **`SELECT DISTINCT`** en SQL.
*   **Resultado**: El dispositivo solo procesa ~20 elementos a la vez y los menús de filtros son instantáneos, aunque existan miles en el disco.

---

## 📜 Paso a Paso para Implementar un Filtro "Grandes Ligas"

1.  **Definir el Contrato**: Crear una `data class` en `ModeloFiltrosEstructurados.kt` (Core) que represente los criterios (ej: `soloVerificados`, `rangoFecha`).
2.  **Preparar el DAO**: Añadir una consulta `@Query` con parámetros opcionales.
    *   *Tip*: Usa `(:param IS NULL OR columna = :param)`.
3.  **Configurar el Flow reactivo**:
    ```kotlin
    val resultados = filtros.flatMapLatest { criteria ->
        repository.obtenerDatos(criteria)
    }.cachedIn(viewModelScope)
    ```
4.  **Mapear visualmente**: Usar el `ArmadorFiltrosV3` para convertir los booleanos de la data class en burbujas con iconos del `BeDictionary`.

---

## 🚫 Prácticas Prohibidas (Tribunal Maverick)

1.  **❌ Filtrar en el Hilo Principal**: Nunca uses `.filter` sobre una lista gigante en la UI.
2.  **❌ Be Inteligente**: No permitas que el Asistente Be tome decisiones sobre qué datos mostrar. Él solo sostiene el micrófono.
3.  **❌ Filtros Fantasma**: Los filtros deben ser volátiles y morir con la pantalla, a menos que el usuario los guarde como "Favorito".

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
