# Walkthrough - Sembrado de Datos "Big League" (Atómico)

He implementado un sistema de sembrado de datos de alto nivel que garantiza que el catálogo de 500+ rubros esté presente en la base de datos desde el momento en que se instala la aplicación, eliminando delays y asegurando la integridad entre ambas apps.

## ¿Qué lo hace "Big League"?

### 1. Inicialización Atómica vía Room Callback
He movido la lógica de sembrado de la capa de UI (`MainActivity`) a la capa de datos (`CoreDataModule`).
- **Callback `onOpen`**: Ahora, cada vez que la base de datos se abre, el motor de Room verifica automáticamente si el catálogo necesita ser sembrado o actualizado.
- **Atomicidad**: No hay riesgo de ver el buscador vacío; la base de datos garantiza la presencia de los datos antes de permitir consultas pesadas.

### 2. Tabla de Metadatos Interna (SSOT)
He eliminado la dependencia de `SharedPreferences` para rastrear la versión del catálogo.
- **Nueva Entidad**: `AppMetadataEntity` en `:core`.
- **Sincronización Física**: La versión del catálogo ahora vive dentro de la misma base de datos (`app.db` o `prestador.db`). Si borras los datos de la app, el marcador de versión desaparece y se vuelve a sembrar de forma limpia y automática.

### 3. Unificación Total (:core)
Ambas aplicaciones (`:app` y `:prestador`) ahora comparten exactamente el mismo motor de inicio:
- Se inyecta un `Provider<CategorySeeder>` en el builder de Room.
- Se utiliza el mismo archivo `seed_data.json` de los assets del Core.

## Cambios Técnicos Realizados

- **`AppDatabase.kt`**: Añadida la tabla `app_metadata` y subida la versión a 31.
- **`CategorySeeder.kt`**: Refactorizado para usar `AppMetadataDao` y la estrategia de versionado interno.
- **`CoreDataModule.kt`**: Configurado el `RoomDatabase.Callback` con inyección diferida (Lazy) para evitar dependencias circulares.
- **Limpieza**: Se han eliminado los disparadores manuales en `GestorInicioMav` y `PrestadorStartupManager`.

## Resultados
- **Rendimiento**: El primer inicio es igual de fluido, pero los datos están disponibles mucho antes.
- **Fiabilidad**: Es imposible que las categorías se "pierdan" mientras la base de datos exista.
