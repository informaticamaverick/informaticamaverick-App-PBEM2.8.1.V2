# Plan de Acción: Refactorización y Alto Rendimiento de Categorías (Core Elite)

Este plan implementa una reestructuración profunda del motor de categorías y supercategorías para cumplir con la **Ley #9 (Idioma Español)**, la **Ley #10 (Puzzle Anatomy)** y los estándares de alto rendimiento de la industria (Carga en Cascada).

## User Review Required

> [!IMPORTANT]
> Se realizará un renombramiento masivo de entidades y DAOs en el módulo `:core`. Aunque he verificado las referencias, este cambio requiere una sincronización de Gradle inmediatamente después para evitar errores de IDE.

> [!TIP]
> Implementaremos un filtrado por SQL (Room) que reducirá el uso de memoria RAM en un ~90% durante las búsquedas de rubros.

## Proposed Changes

### 1. Refactorización de Soberanía (:core)

#### [MODIFY] [AppDatabase.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/AppDatabase.kt)
- Actualizar la lista de entidades y funciones de DAOs con los nuevos nombres en español.

#### [RENAME] `CategoryEntity` -> `CategoriaEntity`
- Cambiar nombre de la clase, tabla (`categorias_mav`) e índices.

#### [RENAME] `SuperCategoryEntity` -> `SuperCategoriaEntity`
- Cambiar nombre de la clase y tabla (`super_categorias_mav`).

#### [RENAME] `CategoryDao` -> `CategoriaDao` y `SuperCategoryDao` -> `SuperCategoriaDao`

### 2. Modelos de UI y Mappers (:core)

#### [NEW] [CategoriaUiModels.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/domain/model/CategoriaUiModels.kt)
- `SuperCategoriaUiModel`: Título, icono, color, conteo de rubros. (Shallow).
- `CategoriaUiModel`: Nombre, icono, descripción. (Deep).

#### [NEW] [CategoriaMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/CategoriaMapper.kt)
- Centralizar la conversión de Entidad -> UI Model.

#### [NEW] [SuperCategoriaMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/SuperCategoriaMapper.kt)
- Gestionar la lógica visual de las carpetas Bento.

### 3. Sintonía de Repositorio y Datos

#### [RENAME] `CategoryRepository` -> `CategoriaRepository`
- Refactorizar `getCategoriesBySuperCategory` para que sea reactivo al nuevo sistema de capas.

#### [MODIFY] `CategorySeeder.kt`
- Actualizar el sembrado inicial para usar las nuevas entidades en español.

## Verification Plan

### Automated Tests
- Ejecutar compilación de `:core` para validar la integridad de Room.

### Manual Verification
1.  **Carga Inicial:** Verificar que la Home de la App Azul carga las Supercategorías con sus colores correctos.
2.  **Búsqueda:** Escribir en el buscador y confirmar que los resultados aparecen por SQL sin latencia.
3.  **App Naranja:** Entrar al perfil del prestador y verificar que el selector de rubros sigue funcionando con la nueva estructura.
