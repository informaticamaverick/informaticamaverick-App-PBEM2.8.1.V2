# Plan de Implementación: Prioridad Absoluta de Selección v2026

Este plan corrige el conflicto de visibilidad entre el modo búsqueda y el modo multiselección, garantizando que Be priorice siempre las herramientas de edición y coordine el cierre automático de la búsqueda.

## User Review Required

> [!IMPORTANT]
> Se ha detectado que el modo búsqueda bloqueaba la aparición de las herramientas de edición. Se aplicará una regla donde la **Multiselección desactiva automáticamente la búsqueda** para limpiar el HUD y permitir la edición masiva.

## Proposed Changes

### 1. Lógica del Asistente (Prioridades)
Implementación de la jerarquía MD3.

#### [MODIFY] [BeCuerpoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/vm/BeCuerpoViewModel.kt)
*   **Válvula de Prioridad**: Modificar la construcción de `idsEdicionFinal` para que dependa únicamente de `multi`, ignorando `buscando`.
*   **Reaction Sync**: Añadir lógica en el `init` para llamar a `beBusquedaMotor.establecerEstaBusquedaActiva(false)` cuando `estaMultiseleccionActiva` pase a true.

---

### 2. Saneamiento de Infraestructura (Higiene)
Limpieza de parámetros y firmas.

#### [MODIFY] [PiezasHerramientasBe.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/herramientas/PiezasHerramientasBe.kt)
*   **Firma Limpia**: Eliminar el parámetro `colorBorde` de `IslaHerramientasSupreme` ya que el contenedor es ahora puramente estructural (Box).

#### [MODIFY] [ArmadorHerramientasLienzo.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/herramientas/ArmadorHerramientasLienzo.kt)
*   **Llamadas Sincronizadas**: Actualizar todas las instancias de `IslaHerramientasSupreme` para remover el argumento de color.

## Verification Plan

### Manual Verification
- **Prueba de Flujo**: Abrir Archivero (Búsqueda activa) -> Iniciar selección -> Verificar que la barra de búsqueda se cierre sola y Be muestre "Cancelar | Todo | Borrar".
- **Prueba de Retorno**: Pulsar "Cancelar" -> Verificar que Be regrese al estado de búsqueda o herramientas primarias según el contexto.
