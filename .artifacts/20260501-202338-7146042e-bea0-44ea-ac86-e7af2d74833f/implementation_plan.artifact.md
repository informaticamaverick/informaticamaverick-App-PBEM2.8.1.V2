# Solución: Estabilidad Be Assistant, Bloqueo de Fondo y Animaciones

Este plan resuelve la desaparición de herramientas en Chat/Presupuestos, bloquea las interacciones con la pantalla de atrás cuando se abren herramientas tácticas y añade efectos de animación premium. También aclara el flujo de datos de presupuestos (Zero Cost).

## User Review Required

> [!IMPORTANT]
> - **Zero Cost Policy**: Se confirma que los presupuestos se guardan **únicamente en Room** (`BudgetDao`) para uso offline. No consumen cuota de lectura/escritura en Firebase Firestore de forma constante.
> - **Bloqueo de Fondo**: Las pantallas de "Nueva Licitación" y "Presupuestos de Chat" ahora consumirán todos los eventos táctiles, evitando que se toquen iconos de la pantalla anterior.

## Proposed Changes

### [Capa de Presentación - ViewModels]

#### [BeBrainViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/BeBrainViewModel.kt)
- Refinar `onRouteChanged` y `setHUDContext` para que la transición de herramientas sea atómica y no dependa de re-composición tardía.
- Asegurar que `HUDContext.CHAT` sea persistente y no se limpie por avisos de ruta genéricos.

---

### [Componentes de UI y Pantallas]

#### [AppNavigation.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/AppNavigation.kt)
- Añadir `slideInVertically` y `slideOutVertically` a las transiciones de `CrearLicitacion` y `ChatPresupuestosRecibidos` para que aparezcan desde abajo.

#### [CrearLicScreen.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/CrearLicScreen.kt)
- Añadir `.pointerInput(Unit) { detectTapGestures { } }` al contenedor principal para bloquear clics hacia atrás.

#### [ChatPresupuestoRecibidosScreen.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/ChatPresupuestoRecibidosScreen.kt)
- Aplicar bloqueo de interacciones de fondo mediante modificador de entrada de puntero.

---

## Verification Plan

### Manual Verification
1. **Persistencia de Herramientas**:
    - Entrar a Chat. Verificar que "Presupuestos" aparezca y no se borre tras 2 segundos.
    - Entrar a Presupuestos. Verificar que "Nueva Lic" permanezca estable.
2. **Animaciones**:
    - Tocar "Nueva Lic". Verificar que la pantalla se deslice desde el borde inferior del celular.
3. **Bloqueo de Fondo**:
    - Con la pantalla de "Nueva Licitación" abierta, intentar tocar botones de la Home que se ven en los bordes. Verificar que no ocurra nada.
