# 🔍 Protocolo: Búsqueda con Be (Portal de Inteligencia v2026.ELITE)

El protocolo **"Buscar se escribe con Be"** define el flujo soberano para el descubrimiento de información por texto dentro del ecosistema Maverick. Elimina la redundancia y garantiza que la búsqueda sea instantánea, inteligente y universal.

---

## 🏛️ Filosofía: El Triángulo Dorado
La búsqueda no ocurre en un solo lugar; es un proceso fragmentado en tres capas de responsabilidad:
1.  **Be (Entrada)**: Capta la intención física del usuario.
2.  **El Motor (Procesamiento)**: Purifica la intención (Normalización + Debounce).
3.  **El Obrero (Ejecución)**: Filtra los datos reales en la fuente (SQL).

---

## 🛠️ Arquitectura de Archivos

### 1. BeBusquedaMotor (Cerebro en `:core`)
Es un Singleton centralizado que vive en el corazón del sistema.
*   **Misión**: Recibir texto crudo y emitir una señal limpia.
*   **Normalización**: Convierte "Cáñería (Rota)" -> "caneria rota".
*   **Debounce**: Espera **300ms** antes de confirmar la señal para ahorrar recursos.
*   **Exposición**: `consultaNormalizadaDebounced` (StateFlow).

### 2. BeBusquedaViewModel (Infraestructura en `:app`)
Actúa como el orquestador táctico de la barra de búsqueda física.
*   **Misión**: Gestionar el hardware y el foco.
*   **Hardware**: Abre/Cierra el teclado virtual.
*   **Foco**: Gestiona cuándo la barra está activa o en reposo.

### 3. Los Obreros (ViewModels de Pantalla)
Son los que realmente tienen los datos (Chats, Eventos, Presupuestos).
*   **Misión**: Reaccionar a la señal limpia del Motor.
*   **Implementación**: Deben usar `combine` para unir sus filtros manuales con el texto del motor.

---

## 📜 Paso a Paso para Implementar una Búsqueda Profesional

### 1. En el DAO (SQL)
Añade parámetros de búsqueda por coincidencia usando el motor de SQLite:
```kotlin
@Query("SELECT * FROM tabla WHERE titulo Normalizado LIKE '%' || :consulta || '%'")
fun buscarDatos(consulta: String): Flow<List<Entity>>
```

### 2. En el ViewModel (Obrero)
Inyecta el `BeBusquedaMotor` y crea un flujo reactivo:
```kotlin
val resultados = combine(
    filtrosLocales,
    beBusquedaMotor.consultaNormalizadaDebounced
) { filtros, texto ->
    Pair(filtros, texto)
}.flatMapLatest { (f, q) ->
    repository.obtenerDatosFiltrados(f, q)
}.stateIn(...)
```

### 3. En la Pantalla (UI)
Declara la soberanía del contrato HUD indicando la pista de búsqueda:
```kotlin
DisposableEffect(Unit) {
    coordinador.actualizarConfiguracionBe(
        ConfiguracionContextoBe(pistaBusqueda = "BUSCAR EN...")
    )
    onDispose { }
}
```

---

## 🚫 Reglas de Oro (Tribunal Maverick)
*   **❌ Prohibido**: Usar `contains()` de Kotlin en listas de más de 100 elementos. El filtrado es **SQL-First**.
*   **❌ Prohibido**: Implementar normalización local en el ViewModel. Usa siempre el motor.
*   **✅ Obligatorio**: Be debe dejar de hablar (limpiar burbujas) en cuanto el usuario activa el modo búsqueda.

---
**Informática Maverick - Departamento de Inteligencia de Datos (2026)**
