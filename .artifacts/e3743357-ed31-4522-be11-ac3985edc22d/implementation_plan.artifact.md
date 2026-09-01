# Optimización e Interactividad de Tarjetas de Presupuesto

Este plan aborda la falta de interactividad en el área del prestador de la tarjeta de presupuesto y corrige problemas visuales relacionados con la carga de fotos y la visualización de categorías.

## Proposed Changes

### UI Shared - Componentes

#### [MODIFY] [TarjetaPresupuesto.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/ui/components/TarjetaPresupuesto.kt)
- **Interactividad**: Añadir un modificador `.clickable` a la fila que contiene el avatar y el nombre del prestador para disparar `alHacerClickChat()`.
- **Saneamiento de Imagen**: Refinar la resolución de `fotoFinal`. Si la fuente procesada es una cadena que no apunta a un recurso válido, asegurar que `MoldeBurbujaPerfilV3` pueda caer de nuevo (fallback) a las iniciales.
- **Saneamiento de Categoría**: Ajustar la lógica del badge para asegurar que se muestre el emoji y el nombre legible, evitando IDs técnicos.
- **A4 Layout**: Ajustar el padding inferior para asegurar que los elementos sean clicables sin colisiones.

### App - ViewModels

#### [MODIFY] [ArchiveroPresupuestoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/chat/ArchiveroPresupuestoViewModel.kt)
- Asegurar que el mapeo de categorías sea robusto y que `categoriaNombre` siempre devuelva un valor amigable.

## Verification Plan

### Manual Verification
1. **Archivero (App Azul)**:
   - Tocar el cuerpo de la tarjeta -> Debe abrir el visor de presupuesto.
   - Tocar el nombre o avatar del prestador -> Debe abrir el chat con ese prestador.
   - Tocar el icono de sobre -> Debe abrir el chat.
2. **Visual**:
   - Verificar que el avatar del prestador muestre sus iniciales si la foto no carga.
   - Verificar que el badge de categoría diga "Sistemas" (u otro rubro) con su respectivo emoji, y no "SISTEMAS_GENERAL".
