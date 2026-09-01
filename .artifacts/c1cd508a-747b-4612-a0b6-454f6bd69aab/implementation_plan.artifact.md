# Plan de Refactorización: Armador de Menús Maverick v3

Este plan centraliza la construcción de menús tácticos (Perfil, Filtros, Orden, Rubros) en un nuevo componente especializado, mejorando el rendimiento mediante la eliminación de código redundante y refinando la física de las animaciones.

## User Review Required

> [!IMPORTANT]
> Se conservará el efecto de "cola" (flecha) mediante el componente `BubbleShapeV3`. La lógica de la forma no se alterará, solo se optimizará su punto de origen para la animación.

> [!NOTE]
> Se crearán funciones de alto nivel para que cualquier pantalla pueda invocar un menú completo (ej: `MenuPerfil`) sin preocuparse por la composición interna.

## Proposed Changes

### [Módulo: App / Menús]

#### [NEW] [ArmadorMenuV3.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/sistema/menu/v3/ArmadorMenuV3.kt)
- Centralizar la lógica de armado de menús.
- Funciones: `MenuPerfilContenido`, `MenuFiltrosContenido`, `MenuOrdenContenido`, `MenuRubrosContenido`.
- Uso de `MoldeBurbujaPerfilV3` para items de identidad.

#### [MODIFY] [MoldeMenuArmador.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/sistema/menu/v3/MoldeMenuArmador.kt)
- Optimizar el cálculo de `transformOrigin` para que dependa del `arrowOffset`.
- Mejorar especificaciones de `spring` para una entrada/salida más fluida y profesional.

#### [MODIFY] [BarraFiltrosV3.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/sistema/contexto/BarraFiltrosV3.kt)
- Limpiar el código manual y delegar el contenido de los menús al `ArmadorMenuV3`.

#### [MODIFY] [MoldeListaV3Cabecera.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/sistema/lista/MoldeListaV3Cabecera.kt)
- Usar `MenuPerfilContenido` en el slot de perfil.

## Verification Plan

### Automated Tests
- `app:assembleDebug`: Asegurar que el cableado de perfiles e identidades es correcto.

### Manual Verification
- Tocar los botones de Filtros/Ordenar y verificar que el menú "brota" de la flecha con efecto spring.
- Cambiar de perfil y confirmar que el menú usa el nuevo armador centralizado.
- Verificar que la "cola" sigue perfectamente pegada al menú.
