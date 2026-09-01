# 💼 Manual Técnico: Concursos Públicos (v2026.ELITE)

Este manual detalla el funcionamiento, persistencia y visualización de los **Concursos Públicos** (Licitaciones) dentro del ecosistema Maverick, aplicando la segregación semántica entre proyectos y ofertas.

---

## 🏛️ 1. Arquitectura de Datos

Los Concursos Públicos se gestionan de forma independiente a los presupuestos directos para garantizar la soberanía del proyecto del cliente.

### Entidades (Core)
*   **`ConcursoPublicoEntity`**: Almacena los metadatos del proyecto (título, descripción, cláusulas, ubicación).
*   **`ConcursoPublicoFtsEntity`**: Tabla virtual de búsqueda de texto completo (FTS4) que indexa `titulo` y `descripcion` para búsquedas instantáneas.

### Relación Carpeta/Archivo
Se utiliza el POJO **`ConcursoPublicoConPresupuestos`** con la anotación `@Relation` de Room. 
*   **Funcionamiento**: Room realiza un JOIN atómico en SQLite para devolver un concurso junto con todas sus ofertas vinculadas (`idConcurso` como clave foránea).
*   **Uso**: Exclusivo para la App Azul (Cliente) en la gestión de carpetas de proyectos.

---

## 🌪️ 2. El Embudo de Búsqueda (Ley #14 & #15)

El descubrimiento de concursos implementa el estándar de "Grandes Ligas" mediante el filtrado en la fuente.

### Flujo de Filtrado
1.  **Entrada**: El asistente Be captura el texto en la barra táctica.
2.  **Motor**: El `BeBusquedaMotor` normaliza la cadena (quita acentos/mayúsculas) y aplica un debounce de 300ms.
3.  **Consulta SQL**: El `ConcursoPublicoDao` ejecuta una consulta `MATCH` sobre la tabla FTS:
    ```sql
    SELECT cp.* FROM concursos_publicos_mav cp
    JOIN concursos_publicos_fts fts ON cp.idConcurso = fts.rowid
    WHERE fts.titulo MATCH :consulta
    ```

---

## 📡 3. Ciclo de Vida y Persistencia

### Publicación (Remote Sync)
1.  El cliente crea el concurso localmente (**SSOT**).
2.  El repositorio genera las **Huellas de Descubrimiento** (Tópicos) mediante el `MotorDescubrimientoMav`.
3.  Se sincroniza con Firestore en la colección `indice_concursos` para que los prestadores de la zona reciban la notificación en tiempo real.

### Visualización en Pantalla
*   **Bandeja Principal**: Muestra "folders" de concursos. Utiliza `conteoPresupuestos` de la entidad para mostrar actividad sin cargar los detalles de las ofertas.
*   **Vista de Detalle**: Utiliza la relación `@Relation` para listar todas las propuestas económicas recibidas para ese proyecto específico.

---

## 🛠️ Implementación Profesional (ViewModel)

El `ConcursoPublicoViewModel` unifica los filtros manuales (botones de estado) con la búsqueda de Be:

```kotlin
val todosLosConcursos = combine(
    filtrosActivos,
    beBusquedaMotor.consultaNormalizadaDebounced
) { manual, query ->
    concursoRepository.buscarPropiosFts(userId, manual.copy(consulta = query))
}
```

---
> [!IMPORTANT]
> **Privacidad Maverick**: Los prestadores (App Naranja) nunca tienen acceso a la entidad de relación `ConcursoPublicoConPresupuestos`. Ellos solo ven el concurso público como una oportunidad de mercado y su propia oferta individual.
