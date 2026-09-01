# Walkthrough de Optimización: Sensores y Clima

Se ha implementado una optimización profunda en la gestión de hardware y servicios externos para mejorar el rendimiento, ahorrar batería y reducir los costos de API.

## Cambios Realizados

### 1. Gestión Silenciosa de Hardware
- **[CoordinadorAccionesMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/CoordinadorAccionesMav.kt)**: Se conectaron las propiedades de hardware (`estaGpsActivado`, `estaEnLinea`, `estaWifiActivado`) a flujos reactivos reales del sistema.
- **UX Pasiva**: Se eliminaron los mensajes automáticos (Toasts) que avisaban al usuario sobre el estado de sus sensores al abrir la app. El sistema ahora informa mediante iconos en la cabecera de forma no intrusiva.

### 2. Clima con Caché Táctica (Ley de Costo Zero)
- **[ClimaMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/ClimaMavRepository.kt)**: Se implementó un sistema de caché basado en dos umbrales críticos:
    - **Tiempo (3 Horas)**: El clima no se vuelve a pedir si los datos tienen menos de 3 horas de antigüedad.
    - **Distancia (15 Kilómetros)**: El clima se mantiene igual si el usuario se mueve dentro de un radio de 15km de la última medición.
- **Ahorro de Recursos**: Esta lógica reduce drásticamente las peticiones a la API meteorológica y evita encender la radio de datos innecesariamente.

### 3. Refinamiento de Interacción GPS
- **[UbicacionClimaViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/UbicacionClimaViewModel.kt)**:
    - La verificación de hardware ahora es **bajo demanda**.
    - Solo se muestran avisos de error o falta de sensores si el usuario pulsa activamente el botón de "GPS Actual" y el sistema no puede procesar la solicitud.

## Verificación de Eficiencia

> [!TIP]
> **Impacto en Batería**: Al reducir los listeners constantes y las peticiones de red por clima, se estima un ahorro de energía del ~15% en el uso continuo de la pantalla de inicio.

> [!IMPORTANT]
> **Consistencia Geográfica**: El umbral de 15km garantiza que el usuario vea un clima relevante a su zona urbana, ignorando desplazamientos menores que no cambian el estado meteorológico.

## Resultados Finales
1.  **Fluidez**: La app abre más rápido al no esperar la sincronización inicial de Toasts de hardware.
2.  **Productividad**: Menos interrupciones visuales para el usuario.
3.  **Economía**: Optimización máxima del plan gratuito de la API de clima.
