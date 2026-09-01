# 🗄️ Protocolo: Persistencia Elite Room (v2026.ELITE)

El protocolo **"Tabla Tablita Tablón"** establece el estándar de oro para el diseño de bases de datos locales en el ecosistema Maverick. Su objetivo es garantizar una persistencia robusta, escalable y de alto rendimiento, siguiendo las mejores prácticas de Google y SQLite.

---

## 🏛️ Filosofía: Normalización y Soberanía

En Maverick, la base de datos no es solo un almacén de datos; es el **Corazón de la Verdad (SSOT)**. Un diseño pobre en la base de datos degrada toda la experiencia del usuario.

### 1. Responsabilidad Única de Tabla
Cada `@Entity` debe estar focalizada en un único dominio de datos.
*   **Correcto**: `ConcursoPublicoEntity`, `PresupuestoEntity`, `UserEntity`.
*   **Incorrecto**: `ComercialMonolitoEntity` (que mezcla proyectos y ofertas).

### 2. Adiós a los Monolitos (Tablas Tablón)
Se prohíbe la creación de tablas con decenas de columnas redundantes. 
*   **Normalización**: Divide entidades gigantes en relaciones de 1:1, 1:N o N:N mediante `@ForeignKey` o `@Relation`.
*   **Soberanía de Identidad**: Si una tabla necesita datos de otra (ej: el nombre del prestador en un presupuesto), guarda solo el **ID** y un **Snapshot Visual Mínimo** (nombre/foto) para rendimiento offline.

---

## 🌪️ Inteligencia de Búsqueda: FTS5 vs LIKE

Para búsquedas profesionales (estilo Google Drive o Gmail), el operador `LIKE` es obsoleto para textos largos.

### El Motor FTS5 (Full-Text Search)
Crea una tabla virtual con un índice invertido que permite búsquedas instantáneas y coincidencias parciales.

#### Pasos para implementar FTS5:
1.  **Entidad Principal**: Tu tabla física estándar con claves primarias y tipos complejos.
2.  **Entidad FTS**: Una tabla virtual marcada con `@Fts4` (o `@Fts5`) que apunta a la principal vía `contentEntity`. Solo incluye columnas de texto.
3.  **Consulta Táctica**: Usa la palabra clave `MATCH` en el DAO en lugar de `LIKE`.

> [!TIP]
> Usa `@Index` estándar para búsquedas por coincidencia exacta (ej: `id`, `email`, `categoria_id`). Usa **FTS** para buscadores donde el usuario escribe palabras libres.

---

## 📐 Reglas de Oro de Estructura

1.  **Índices Tácticos**: Define `indices = [Index(value = ["columna"])]` en campos usados frecuentemente en `WHERE`, `JOIN` o `ORDER BY`.
2.  **Higiene de Blobs**: **NUNCA** guardes imágenes o audios (ByteArray) en Room. Guarda el archivo en el almacenamiento interno y almacena solo la **Ruta (String)**.
3.  **Separación de Capas**: Nunca expongas tus `@Entity` a la UI. El Repositorio debe mapearlas a `UiModels`.
4.  **Asincronía Obligatoria**: Consultas siempre vía `Flow`, `Coroutines` o `Paging`. Prohibido el acceso en el Main Thread.

---

## 🛠️ Mantenimiento y Evolución

*   **Export Schema**: Mantén `exportSchema = true` para tener un historial JSON de la evolución de tus tablas.
*   **Migraciones Explícitas**: Cada cambio en una entidad debe ir acompañado de una clase `Migration(X, Y)` para proteger los datos de los usuarios en producción.
*   **Type Converters**: Usa conversores para tipos que SQLite no entiende de forma nativa (Enums, Lists, Dates).

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
