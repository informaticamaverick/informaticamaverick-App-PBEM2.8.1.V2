# Plan de Optimización para Compilación Release (v2026.ELITE)

Este plan tiene como objetivo garantizar que tanto la **App Azul (Cliente)** como la **App Naranja (Prestador)**, junto con los módulos **Core** y **UI-Shared**, estén completamente preparados para una compilación de producción (Release). Se enfoca en la robustez de las reglas de ProGuard/R8, la preservación de modelos de datos críticos y la validación integral del código.

## User Review Required

> [!IMPORTANT]
> - **Firma de la App**: Actualmente, las configuraciones de `release` en `build.gradle.kts` están usando el `debug.keystore`. Esto es ideal para probar en smartphones reales sin complicaciones de firmas, pero para una publicación en Play Store se requerirá un Keystore oficial.
> - **AdMob**: Se mantiene el ID de aplicación de prueba en el Manifest para evitar penalizaciones por anuncios reales durante el testeo.

## Proposed Changes

### 1. Módulo Core: Robustez Global

#### [MODIFY] [core/proguard-rules.pro](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/proguard-rules.pro)
- Añadir reglas para preservar las interfaces de Retrofit (`GeorefApiService`, `WeatherApiService`).
- Asegurar que los modelos de respuesta de APIs externas (OpenMeteo, Georef) no sean ofuscados ni eliminados.
- Fortalecer la preservación de `@Keep` en todas las entidades.

### 2. App Azul: Especialización de Reglas

#### [MODIFY] [app/proguard-rules.pro](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/proguard-rules.pro)
- Incluir explícitamente la preservación de `ResultadoBusquedaPrestadorSQLView` y `RelacionBusquedaEntity`, que son específicos de este módulo.
- Asegurar que todos los ViewModels del paquete `com.example.myapplication.viewmodel.**` estén protegidos (ya existente, pero validaremos cobertura).

### 3. App Naranja (Prestador): Creación de Reglas

#### [MODIFY] [prestador/proguard-rules.pro](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/proguard-rules.pro)
- Implementar reglas de preservación para los ViewModels específicos del Prestador (`com.example.myapplication.prestador.viewmodel.**`).
- Añadir soporte para Google Play Billing (Membresía Elite).
- Replicar reglas de higiene (eliminación de Logs) y soporte para Firebase/Play Services.

### 4. Módulo UI-Shared: Consistencia

#### [MODIFY] [ui-shared/proguard-rules.pro](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/proguard-rules.pro)
- Asegurar que los componentes de UI personalizados no pierdan sus firmas genéricas ni anotaciones necesarias para la recomposición de Compose en release.

---

## Verification Plan

### Automated Tests
- Ejecutar `./gradlew assembleRelease` para ambas apps (`:app:assembleRelease` y `:prestador:assembleRelease`). Esto validará:
    - Que el código compila sin errores.
    - Que R8 puede procesar todas las reglas sin conflictos.
    - Que no hay dependencias faltantes en el grafo de compilación.

### Manual Verification
- Revisar los logs de compilación en busca de warnings de R8 que indiquen clases faltantes o reglas redundantes.
- Confirmar que los archivos `.apk` generados tienen un tamaño optimizado (indicativo de que `isMinifyEnabled` y `isShrinkResources` funcionaron).
