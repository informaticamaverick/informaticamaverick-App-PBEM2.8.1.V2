# Relevamiento de Compilación y Estado del Proyecto (App Azul)

Se ha realizado una compilación de prueba del módulo `:app` y un relevamiento exhaustivo de las nuevas ubicaciones y nombres de archivos tras el refactoring del Sistema de Diseño y Modelos.

## 📊 Estado de la Compilación (:app:compileDebugKotlin)

La compilación falló con múltiples errores, principalmente debido a:
1.  **Cambios de Idioma en Modelos**: Los modelos globales (Be, Prestador, Emociones) ahora usan campos en español, rompiendo referencias legacy en inglés.
2.  **Centralización de Estilos**: Referencias a `AppPalette` o colores específicos que no se han actualizado al nuevo hub en `:ui-shared`.
3.  **Renombramiento de Componentes**: Algunos componentes "Elite" o "Premium" fueron renombrados para mayor claridad.
4.  **Mover a Core/Utilidades**: Funciones de utilidad movidas de `core.utils` a `core.utilidades`.

---

## 🗺️ Mapa de Nuevas Ubicaciones y Nombres

### 1. Sistema de Diseño (Colores y Estilos)

| Elemento Legacy | Nueva Ubicación / Nombre | Módulo |
| :--- | :--- | :--- |
| `AppPalette.kt` | `ColoresGeneral.kt` | `:ui-shared` |
| `AppPalette` (Clase) | `MaverickColors` (con `typealias AppPalette`) | `:ui-shared` |
| `CPCyberColors` | `CyberColorsV3` (en `ColoresGeneral.kt`) | `:ui-shared` |
| `BodyText` | `MaverickTypography.BodyText` | `:app` |
| `BentoDarkGlassBackground` | `MaverickColors.BentoDarkGlassBackground` | `:ui-shared` |

### 2. Modelos Tácticos (Be / Asistente)

| Campo Legacy (Inglés) | Nuevo Campo (Español) | Modelo |
| :--- | :--- | :--- |
| `BeState` | `EstadoBe` | `ModelosHUD.kt` |
| `BeSmallActionModel` | `ModeloAccionPequenaBe` | `ModelosHUD.kt` |
| `icon` | `icono` | `MensajeBe` |
| `text` | `texto` | `MensajeBe` |
| `emotion` | `emocion` | `MensajeBe` |
| `SLEEPING` | `DURMIENDO` | `EmocionBe` |
| `HAPPY` | `FELIZ` | `EmocionBe` |
| `SURPRISED` | `SORPRENDIDO` | `EmocionBe` |
| `ANGRY` | `ENOJADO` | `EmocionBe` |
| `SAD` | `TRISTE` | `EmocionBe` |

### 3. Modelos de Negocio (Prestador / Proveedor)

| Campo Legacy (Inglés) | Nuevo Campo (Español) | Modelo |
| :--- | :--- | :--- |
| `ProviderDisplayModel` | `PrestadorUiModel` | `PrestadorUiModel.kt` |
| `isOnline` | `estaOnline` | `PrestadorUiModel` |
| `is24h` | `atiende24h` | `PrestadorUiModel` |
| `isSubscribed` | `estaSuscrito` | `PrestadorUiModel` |
| `isLocal` | `tieneLocalFisico` | `PrestadorUiModel` |
| `distanceKm` | `distanciaKm` | `PrestadorUiModel` |

### 4. ViewModels y Coordinadores

| Nombre Legacy | Nuevo Nombre / Equivalente | Estado |
| :--- | :--- | :--- |
| `hudViewModel` | `BeCerebroViewModel` | Activo |
| `beAssistantViewModel` | `BeAsistenteViewModel` | Activo |
| `UserViewModel` | `ArmadorUsuarioViewModel` | Activo |
| `ProviderViewModel` | `BusquedaPrestadorViewModel`? | Pendiente Verificar |
| `SimulationViewModel` | Lógica movida a Repositorios/Obreros | Pendiente |

---

## 🛠️ Funciones y Utilidades Movidas

- `generateUniqueCode` ➡️ `generateUniqueMAVCode` (en `QRUtils.kt`).
- `core.utils.*` ➡️ `core.utilidades.*` (Refactor de paquetes en Core).
- `matchesSmart` ➡️ `core.utilidades.matchesSmart`.

## 🚨 Próximos Pasos Sugeridos (Sin Aplicar Cambios)

1.  **Actualizar Imports**: Masivamente cambiar `com.example.myapplication.ui.estilos.AppPalette` por `com.example.myapplication.uishared.estilos.MaverickColors`.
2.  **Mapear Modelos en UI**: Actualizar `ChatScreen.kt`, `FastScreen.kt` y `HomeScreenClienteV4.kt` para usar los campos en español de `MensajeBe` y `PrestadorUiModel`.
3.  **Corregir Enums de Be**: Cambiar los estados de emoción de inglés a español en todos los constructores de mensajes.
4.  **Sincronizar AppNavigation**: El archivo `AppNavigation.kt` requiere una limpieza profunda para usar los nuevos nombres de estado de `BeAsistenteViewModel.uiState`.
