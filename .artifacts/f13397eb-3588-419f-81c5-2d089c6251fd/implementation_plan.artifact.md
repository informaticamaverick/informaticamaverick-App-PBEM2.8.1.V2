# Plan de Implementación: Caja de Herramientas Premium y Mejoras de Navegación

Este plan detalla el rediseño de la sección "Caja de Herramientas" en la Home del prestador para darle un estilo premium y la mejora del botón flotante en la gestión de publicaciones.

## User Review Required

> [!IMPORTANT]
> **Simplificación de Herramientas**: Se reducirá el número de botones en la Home a los 4 esenciales: Catálogo, Nueva Promo, Nuevo Presupuesto y Métricas. Esto centraliza las acciones más importantes para el prestador.
> **Estética M3 Expressive**: Se aplicará un diseño más moderno con mejores contrastes y jerarquía visual.

## Proposed Changes

### Módulo Prestador (UI y Experiencia)

#### [MODIFY] [InicioComponents.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/dashboard/components/InicioComponents.kt)
- **Refactor `ToolboxCardSection`**:
    - Actualizar botones:
        - 📦 **Catálogo** (mantiene icono `Inventory2`).
        - 📣 **Nueva Promo** (icono `Campaign`, abre el sheet de creación).
        - 📝 **Nuevo Presupuesto** (icono `PostAdd`, antes "Nuevo Pres.").
        - 📊 **Métricas** (nuevo botón, icono `BarChart`).
    - Mejorar diseño de `ToolButton`: más espaciado, mejores esquinas y efectos de elevación tonal.
- **Actualizar `InicioScreen`**:
    - Incluir `onNavigateToMetrics` en la firma de la función.
    - Pasar los callbacks correctos a `ToolboxCardSection`.

#### [MODIFY] [PrestadorDashboardScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/dashboard/PrestadorDashboardScreen.kt)
- Pasar un lambda vacío para `onNavigateToMetrics` al instanciar `InicioScreen`.

#### [MODIFY] [PromotionListScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/promotion/PromotionListScreen.kt)
- Refinar el estilo de la etiqueta "Nueva promo" a la izquierda del FAB para que se vea más integrada y profesional.

## Verification Plan

### Manual Verification
1. **Home**: Verificar que la Caja de Herramientas muestra solo los 4 botones solicitados con el nuevo diseño.
2. **Nueva Promo**: Tocar "Nueva Promo" en la Home y verificar que abre el panel inferior de creación.
3. **Métricas**: Verificar que el botón de Métricas existe pero no realiza ninguna acción por ahora.
4. **FAB**: En "Mis Publicaciones", verificar que la etiqueta "Nueva promo" aparece correctamente alineada a la izquierda del botón "+".
