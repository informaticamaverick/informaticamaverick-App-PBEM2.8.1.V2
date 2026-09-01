# Plan de Acción: Nuevo Archivero de Presupuestos (Chat-Specific)

Se creará una solución dedicada para el historial de presupuestos dentro del chat, permitiendo filtrar y reutilizar presupuestos previos de manera eficiente.

## Proposed Changes

### [Core / Data]
#### [NEW] [ArchiveroPresupuestosChatViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/chat/ArchiveroPresupuestosChatViewModel.kt)
- **Estado de UI**: `consultaBusqueda`, `idCategoriaSeleccionada`, `ordenReciente` (Boolean).
- **Flujo de Datos**: Consumir `PresupuestoMavRepository.todosLosPresupuestos` y filtrar por `idConcurso == null`.
- **Lógica de Negocio**: Aplicar filtros por rubro y ordenamiento por fecha de recepción.

### [UI Components]
#### [NEW] [TarjetaPresupuesto.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/TarjetaPresupuesto.kt)
- Extraer y refinar `TarjetaPresupuestoA4Document` de `TarjetaPresupuestosConcursos.kt`.
- Adaptar para mostrar información clave: Monto, Rubro (con emoji), Nombre del Prestador, y Miniatura visual.

#### [NEW] [ArchiveroPresupuestosChatSheet.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/chat/componentes/ArchiveroPresupuestosChatSheet.kt)
- Utilizar `ArchiveroMoldeSheet` (v2026.ELITE).
- Integrar `BarraFiltrosEliteV3` para el manejo de Rubros y Ordenamiento.
- Mostrar una `LazyVerticalGrid` con las nuevas `TarjetaPresupuesto`.

### [Integration]
#### [MODIFY] [ChatScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/chat/ChatScreen.kt)
- Instanciar `ArchiveroPresupuestosChatViewModel`.
- Añadir el estado `showArchiveroChatSheet`.
- Conectar el evento del Asistente Be (ID: `archivo_chat`) para abrir esta nueva sheet.

#### [MODIFY] [BeCerebroViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/vm/BeCerebroViewModel.kt) (o Coordinador)
- Asegurar que la acción `archivo_chat` en el contexto `CHAT_CONVERSACION` dispare el evento necesario para abrir la sheet.

## Recomendación Técnica
- **Filtros**: Los rubros deben obtenerse dinámicamente de las categorías de los presupuestos disponibles en el historial.
- **Asistente**: Debemos interceptar el evento `archivo_chat` en el `ChatScreen` mediante el recolector de eventos del coordinador Be.

## Verification Plan
### Manual Verification
- Abrir un chat y tocar el botón "Archivo" del asistente.
- Verificar que se abra la nueva sheet `ArchiveroPresupuestosChatSheet`.
- Filtrar por una categoría específica y comprobar que solo aparezcan esos presupuestos.
- Cambiar el orden de fecha y validar la posición de los elementos.
- Seleccionar un presupuesto y verificar que el flujo de envío se inicie correctamente.
