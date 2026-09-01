# Walkthrough - Armador de Presupuestos Táctico (Material Design 3)

He rediseñado completamente el Armador de Presupuestos siguiendo la estructura de **Grandes Ligas** solicitada, utilizando un **Navigation Rail** lateral y una jerarquía Bento basada en Material Design 3.

## Cambios Realizados

### 1. Navegación Profesional (App Naranja)
- **[ArmadorPresupuestoScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/presupuesto/ArmadorPresupuestoScreen.kt)**: Se ha implementado un `NavigationRail` a la izquierda que permite alternar instantáneamente entre las tres fases del presupuesto:
    - 🪪 **IDENTIDAD**: Configuración de los datos del prestador y del cliente.
    - 🛠️ **ÍTEMS**: Carga de productos y servicios.
    - 📊 **TOTALES**: Resumen comercial y aritmética final.

### 2. Estructura de Secciones (Bento UI)
- **Sección Identidad**: Ahora incluye una tarjeta dedicada para el **Remitente** (con tu logo profesional) y una para el **Destinatario**, que permite seleccionar la dirección de entrega/obra directamente desde las direcciones guardadas del cliente.
- **Sección Ítems**: Se han creado bloques separados para **Materiales** (Artículos) y **Mano de Obra** (Servicios), permitiendo una organización mucho más clara de los costos.
- **Sección Totales**: Un panel limpio que desglosa el subtotal, impuestos y descuentos, finalizando con el Total General en tamaño destacado.

### 3. Componentes y Lógica
- **[SeccionColapsableBento.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/ui/components/SeccionColapsableBento.kt)**: Nuevo componente premium en `:ui-shared` que permite colapsar bloques de información con animaciones fluidas y bordes tonales M3.
- **[BorradorPresupuestoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/presupuesto/BorradorPresupuestoViewModel.kt)**: Ahora gestiona el estado de navegación y carga automáticamente la información del cliente y sus ubicaciones en tiempo real.

## Verificación

- [x] **Navegación Rail**: El cambio entre secciones es fluido y mantiene el estado del borrador.
- [x] **Selección de Dirección**: Se listan las direcciones del cliente y se marcan visualmente al seleccionarlas.
- [x] **Separación de Ítems**: Los productos y servicios aparecen listados en sus respectivas subsecciones dentro de la pestaña Ítems.
- [x] **Aritmética M3**: El desglose de totales es legible y profesional.

> [!TIP]
> Al estar en la sección de **ÍTEMS**, el botón flotante (FAB) ahora está optimizado para añadir productos o servicios rápidamente, consultando siempre tu catálogo de Room.
