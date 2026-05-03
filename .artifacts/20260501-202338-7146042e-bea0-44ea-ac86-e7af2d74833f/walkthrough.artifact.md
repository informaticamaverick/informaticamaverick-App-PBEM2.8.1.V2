# Walkthrough: Mejora de Registro Google y Popup de Dirección

Se ha optimizado el flujo de entrada para nuevos usuarios, pasando de un sistema de redirección forzada a uno de sugerencia inteligente mediante un popup moderno, y enriqueciendo la captura de datos desde Google.

## Cambios Realizados

### 1. Extracción Enriquecida de Google
En [AuthRepository.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/AuthRepository.kt), el método `signInWithGoogle` ahora recupera el perfil adicional del usuario. Esto permite que en [UserRepository.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UserRepository.kt) podamos separar correctamente el nombre y el apellido:

```kotlin
// UserRepository.kt
val givenName = googleProfile?.get("given_name") as? String
val familyName = googleProfile?.get("family_name") as? String
```

### 2. Navegación Fluida
En [LoginViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/auth/LoginViewModel.kt), se modificó la lógica de decisión:
- **Antes**: Si no había dirección, se enviaba a `perfil_cliente_edit`.
- **Ahora**: Siempre se envía a `main_screen`, priorizando que el usuario vea la pantalla principal de inmediato.

### 3. Popup Moderno Maverick
Se implementó `ModernAddressPopup` en [HomeScreenCliente3.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/HomeScreenCliente3.kt) con las siguientes características:
- **Estilo**: Glassmorphism con bordes en degradado `GeminiBrush`.
- **Lógica**: Solo aparece si `userState` no tiene direcciones y si el flag en `TokenManager` indica que es la primera vez que se muestra.
- **Persistencia**: Al cerrar el popup o ir al perfil, se llama a `beViewModel.dismissAddressPopup()` que marca el estado como "mostrado" permanentemente.

## Verificación Realizada

- **Análisis Estático**: Se verificaron los archivos modificados con `analyze_file` para asegurar que no hubiera errores de sintaxis o referencias rotas (especialmente en los imports de Compose en la Home).
- **Lógica de Negocio**: Se revisó el flujo de datos desde `AuthRepository` -> `LoginViewModel` -> `UserRepository` para asegurar la creación del documento en Firestore.
- **UI**: Se aseguró que el componente `ModernAddressPopup` respete la paleta de colores de `MaverickColors`.
