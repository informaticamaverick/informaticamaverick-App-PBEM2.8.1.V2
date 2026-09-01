# Plan de Implementación: Unificación de Modelos y Saneamiento Blue App

Este plan sustituye el modelo legacy `ProviderDisplayModel` por el centralizado `PrestadorUiModel` en la App Azul y restaura los motores analíticos necesarios para corregir la compilación.

## 🏛️ Centralización de Modelos (Ley #9)

| Modelo Legacy | Modelo Core (Elite) | Estado |
| :--- | :--- | :--- |
| `ProviderDisplayModel` | `PrestadorUiModel` | 🔄 Reemplazar en toda la App Azul. |
| `BeSmallActionModel` | `ModeloAccionPequenaBe` | 🔄 Estandarizar en `ModelosHUD.kt`. |

## Proposed Changes

### [Módulo :core] Motores y Lógica de Negocio
Se restauran los componentes analíticos que faltan para la comparación de presupuestos.

#### [NEW] [MotorAnaliticasMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/motores/MotorAnaliticasMav.kt)
- Definición de `ModeloPresupuestoAnalitico`.
- Definición de `PresupuestoClasificado`.
- Implementación de `MotorAnaliticas` con el método `calcularInteligenciaMercado`.
- Definición de `EstadoAnaliticaMercado` y `ElementoGraficoPresupuesto`.

### [Módulo :app] Saneamiento y Corrección de Referencias
Limpieza de archivos legacy y actualización de imports.

#### [DELETE] [ProviderDisplayModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/datos/modelos/ProviderDisplayModel.kt)
- Eliminación definitiva del archivo comentado.

#### [MODIFY] [HomeScreenClienteV4.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/home/HomeScreenClienteV4.kt)
- Reemplazar `ProviderDisplayModel` por `PrestadorUiModel`.
- Actualizar el renderizado para usar los nombres de campos en español (`titulo`, `urlFoto`, `distanciaKm`).

#### [MODIFY] [AppNavigation.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/home/AppNavigation.kt)
- Corregir imports de ViewModels.
- Reemplazar `ProviderDisplayModel` por `PrestadorUiModel`.
- Sincronizar con los nuevos nombres de `BeAsistenteViewModel`.

#### [MODIFY] [HomeScreenViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/home/HomeScreenViewModel.kt)
- Corregir referencias internas y simplificar la orquestación.

### [Módulo :ui-shared] Componentes Reutilizables
#### [MODIFY] [AdMobComponents.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/ui/components/AdMobComponents.kt)
- Asegurar que no dependa de modelos de la App Azul.

## Verification Plan

### Automated Tests
- Ejecutar `./gradlew :app:compileDebugKotlin`.
- Verificar que se resuelvan los 200+ errores de referencias actuales.

### Manual Verification
- Comprobar que la pantalla de Inicio (Blue) cargue correctamente los prestadores usando el modelo de Core.
- Verificar que la pantalla de Analíticas de Presupuestos funcione con el nuevo motor restaurado.
