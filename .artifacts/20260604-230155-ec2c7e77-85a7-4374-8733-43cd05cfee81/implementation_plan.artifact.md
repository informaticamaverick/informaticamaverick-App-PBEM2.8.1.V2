# Plan de Implementación: Corrección de Sembrado de Categorías (App Prestador)

Se ha detectado que la App del Prestador no muestra categorías al editar el perfil debido a que el `CategorySeeder` solo se ejecuta en la App del Cliente (dentro de `BeBrainViewModel`). La solución consiste en asegurar que el sembrado local se realice en ambas aplicaciones de forma proactiva.

## User Review Required

- **Estrategia de Sembrado**: Se propone ejecutar el `seedIfNeeded()` en el `onCreate` de la `MainActivity` de la App del Prestador para garantizar que el catálogo esté disponible desde el inicio.

## Proposed Changes

---

### Módulo `:core` (Datos y Sincronización)

#### [CategorySeeder.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/seed/CategorySeeder.kt)

- No se requieren cambios en este archivo, se usará el método `seedIfNeeded()` existente.

---

### Módulo `:prestador` (App Prestador)

#### [MainActivity.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/MainActivity.kt)

- Inyectar `CategorySeeder` y llamar a `seedIfNeeded()` dentro de un bloque `lifecycleScope.launch` en el `onCreate`.

#### [EditProfileViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/profile/EditProfileViewModel.kt)

- Asegurar que la recolección de `allCategories` sea robusta ante cambios en Room.

---

## Verification Plan

### Manual Verification
- **Prueba en App Prestador**:
    1. Iniciar la App del Prestador (si es una instalación limpia o tras borrar datos).
    2. Ir a Mi Perfil -> Editar.
    3. Abrir el selector de categorías.
    4. Verificar que la lista ya no aparezca vacía y que se puedan buscar rubros del catálogo.
