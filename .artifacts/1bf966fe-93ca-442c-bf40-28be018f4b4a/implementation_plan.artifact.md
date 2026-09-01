# Plan de Saneamiento de Inmersión y UI - App Naranja (Prestador)

Se ha detectado que la App Naranja no aprovecha correctamente la inmersión total (Edge-to-Edge), presentando huecos negros en la parte superior y comportamientos inconsistentes en las cabeceras. Se propone una reingeniería de la estructura de las pantallas para replicar el estándar de éxito de la App Azul.

## User Review Required

> [!IMPORTANT]
> Se rediseñarán las cabeceras de todas las pantallas principales del Prestador (Inicio, Mercado, Chat) para que sean 100% inmersivas, eliminando los saltos visuales y los huecos en la barra de estado.

## Propuesta de Cambios

### 1. Inmersión Total (Edge-to-Edge) en Pantallas

#### [MODIFY] [PrestadorDashboardScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/dashboard/PrestadorDashboardScreen.kt)
- **Corrección de Scaffold**: Aplicar `contentWindowInsets = WindowInsets(0, 0, 0, 0)` al `Scaffold` principal para permitir que el contenido fluya detrás de las barras del sistema.
- **Ajuste de BottomBar**: Refinar los paddings de `BandaNavegacionPrestadorMav` para un look más compacto y moderno.

### 2. Unificación de Cabeceras (v2026.ELITE)

#### [MODIFY] [InicioComponents.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/dashboard/componentes/InicioComponents.kt)
- **InicioTopBar**: Reestructurar para que sea un componente inmersivo que gestione sus propios insets (`statusBarsPadding`) y elimine la sensación de "caja" separada de la parte superior.
- **Sección de Bienvenida**: Ajustar la altura y gradientes para que se integren con la cabecera.

#### [MODIFY] [ChatListScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/chat/ChatListScreen.kt)
- **Refinamiento de DynamicTgHeader**: Implementar la lógica de interpolación matemática (`lerp`) perfeccionada en la App Azul para eliminar errores de cálculo durante el scroll.
- **Saneamiento de Z-Index**: Asegurar que los folders de identidad se deslicen correctamente bajo la cabecera sin solapamientos extraños.

#### [MODIFY] [MercadoConcursosScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/market/MercadoConcursosScreen.kt)
- **Eliminación de TopAppBar Estándar**: Sustituir el `TopAppBar` de Material 3 por una cabecera personalizada inmersiva que siga la línea visual de Maverick Elite.

### 3. Saneamiento de Código y Redundancias
- Eliminar imports en desuso y variables hardcoded en las pantallas de `:prestador`.
- Corregir el flujo de datos del clima para asegurar que use el `WeatherRepository` del Core de forma eficiente.

## Plan de Verificación

### Verificación Visual
- Abrir la App Naranja y confirmar que el color de fondo de las cabeceras llega hasta el borde superior de la pantalla.
- Verificar que no hay un bloque negro entre la barra de estado y el contenido de la app.

### Verificación de Interactividad
- Realizar scroll en la lista de chats y mercado, comprobando que el colapso de la cabecera es suave y sin parpadeos.
- Comprobar que los botones de la barra inferior son accesibles y no se solapan con la barra de gestos de Android.
