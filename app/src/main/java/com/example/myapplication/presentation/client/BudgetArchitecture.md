# Arquitectura de la Sección de Presupuestos (Maverick Premium)

Este documento resume cómo funciona el flujo de datos, persistencia y cumplimiento de las "Reglas de Oro" en la sección de gestión de presupuestos y licitaciones.

## 1. Flujo de Datos y Persistencia (Room First)

La sección sigue una política estricta de **"Offline-First"** y **"Costo Cero"**:

- **Room (SSOT Local)**: Todo presupuesto (recibido por chat) o licitación (creada por el usuario) se guarda inmediatamente en Room (`BudgetDao`). Las pantallas observan `Flows` de la base de datos local para una respuesta instantánea.
- **Sincronización Silenciosa**:
    - Al crear una licitación, se inserta en Room con rutas locales de fotos.
    - Un `repositoryScope` persistente en `BudgetRepository` maneja la compresión, subida a Firebase Storage y actualización de Firestore en segundo plano.
    - Esto permite que el usuario vea su licitación "al instante" sin esperar a la red.
- **Eficiencia en Firebase**: Se usan **FCM Topics** (ej: `tender_t4000_pintura`) para notificar a los prestadores sin que estos tengan que realizar consultas constantes a la nube.

## 2. Cumplimiento de las Reglas de Oro

### ¿A quién llamar?
- **BeBrainViewModel**: Controla el HUD, el asistente Be y la multiselección. Sincroniza el "contexto táctico" para que Be sepa qué herramientas mostrar (ej: "Comparar", "Borrar").
- **AppActionCoordinator**: Es el **Maestro de Intenciones**. La búsqueda global de Be y los cambios de ubicación fluyen por aquí. Los ViewModels observan este coordinador para filtrar sus listas automáticamente.
- **BudgetViewModel (Obrero)**: Realiza el trabajo sucio de filtrado local, cálculo de analíticas base y orquestación de estados (Licitación Abierta -> Adjudicada).

### Estructura de Pantallas (Pantallas Tontas)
- `PresupuestosScreen` y `ChatPresupuestoRecibidosScreen` no filtran datos. Se suscriben a `filteredTenders` o `filteredDirectBudgets` procesados por el ViewModel.
- Usan `collectAsStateWithLifecycle()` para eficiencia de memoria.
- Envían todas las órdenes de usuario al asistente a través de `beBrainViewModel.triggerAction(id)`.

## 3. Integración de Analíticas (BudgetAnalyticsScreen)
El botón de **Comparar (⚖️)** en la Toolbox de Be dispara un overlay moderno que:
- Calcula el **Maverick Score** (balance Precio/Calidad/Experiencia).
- Genera rankings interactivos y comparativas de costos desglosados (Materiales vs Mano de Obra).
- Funciona tanto para licitaciones completas como para selecciones manuales de presupuestos directos.
