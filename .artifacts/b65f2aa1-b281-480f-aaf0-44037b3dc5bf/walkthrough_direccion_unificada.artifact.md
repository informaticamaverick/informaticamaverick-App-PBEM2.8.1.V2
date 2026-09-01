# Walkthrough - Unificación de Gestión de Direcciones

He unificado la forma en que se gestionan y añaden direcciones en ambas aplicaciones (Cliente y Prestador), utilizando un formulario táctico compartido en `:ui-shared` que incluye detección por GPS.

## Cambios Realizados

### 1. Nuevo Formulario Compartido
He creado **[FormularioDireccionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/parts/FormularioDireccionMav.kt)** en `:ui-shared`.
- **Estructura Táctica**: Desglosa la dirección en Calle, Altura, Piso/Depto, C.P., Localidad y Provincia.
- **Detección GPS**: Incluye un botón "DETECTAR" que utiliza los servicios de ubicación del dispositivo para completar el formulario automáticamente.
- **Estilo Maverick**: Utiliza campos `OutlinedTextField` con esquinas redondeadas y acentos en azul Maverick, manteniendo la coherencia con el diseño ROG Dark.

### 2. Actualización de Hojas (Bottom Sheets)
He refactorizado **[PrestadorPerfilParteHojas.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/parts/PrestadorPerfilParteHojas.kt)**.
- `HojaEditorDireccionMav` ahora integra el nuevo formulario, permitiendo una edición profunda de la ubicación tanto para prestadores (sucursales) como para clientes (domicilios).

### 3. Integración en el Flujo de Usuario
- **App Cliente**: En `PerfilUsuarioScreen.kt`, ahora se inyecta la lógica de `UbicacionClimaViewModel` para que el botón "DETECTAR" del formulario funcione con el motor de geocodificación de la app.
- **App Prestador**: En `ProfileScreen.kt`, se ha vinculado la detección de ubicación con `PrestadorPerfilViewModel`, asegurando que el prestador tenga la misma facilidad para registrar sus puntos de venta.

### 4. Refactorización de Firmas
Se han actualizado las firmas de `PrestadorPerfilScreen`, `UsuarioPerfilScreen` y sus lienzos estructurales para soportar el paso de callbacks de GPS (`alDetectarGps`) y estados de carga (`estaDetectandoGps`).

## Resultados de la Verificación

### Compilación
- **:app**: ✅ ÉXITO
- **:prestador**: ✅ ÉXITO

### Funcionalidad
- El usuario ahora puede añadir o editar direcciones con un desglose completo de campos.
- La experiencia de "Detección GPS" es ahora idéntica en ambas aplicaciones, cumpliendo con la paridad funcional solicitada.
- Se mantiene el **Modo Lectura Estricto** para el prestador cuando ve a un cliente, ya que el formulario solo se dispara desde acciones de edición protegidas por el flag `esMiPropioPerfil`.
