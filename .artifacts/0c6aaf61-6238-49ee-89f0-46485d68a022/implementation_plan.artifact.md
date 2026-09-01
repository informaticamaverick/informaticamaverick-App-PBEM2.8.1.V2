# Plan de Implementación - Armador de Presupuestos Elite (M3 & Navigation Rail)

Este plan detalla la reestructuración profunda del Armador de Presupuestos para adoptar una interfaz de "Grandes Ligas" basada en un **Navigation Rail** lateral y secciones colapsables tipo Bento, eliminando el diseño anterior que resultaba confuso.

## User Review Required

> [!IMPORTANT]
> La navegación principal se moverá a una barra lateral (Rail). Se eliminarán las pestañas superiores para maximizar el área de trabajo vertical en el móvil.

## Estructura de Secciones Propuesta

### 1. Sección: IDENTIDAD (Icono: 🪪)
- **Bloque Prestador**: Tarjeta con logo, nombre y matrícula (Lectura/Colapsable).
- **Bloque Cliente**: Datos del destinatario y selector de **Dirección de Obra/Entrega** (Interactivo).

### 2. Sección: PRESUPUESTO (Icono: 🛠️)
- **Bloque Materiales**: Lista de productos/artículos (Sub-sección colapsable).
- **Bloque Mano de Obra**: Lista de servicios (Sub-sección colapsable).
- **Acción**: FAB dinámico para añadir ítems según el contexto.

### 3. Sección: FINANZAS (Icono: 📊)
- **Bloque Aritmética**: Desglose de Subtotales, Impuestos (IVA) y Descuentos.
- **Bloque Observaciones**: Campo de texto para notas legales o de garantía.

---

## Cambios Técnicos

### 1. Interfaz (App Naranja)
#### [MODIFY] [ArmadorPresupuestoScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/presupuesto/ArmadorPresupuestoScreen.kt)
- Implementar `NavigationRail` de Material 3 con animaciones de transición.
- Utilizar `SeccionColapsableBento` para cada bloque de información.
- Refinar la estética con la paleta `PresupuestoMobileTheme` definida anteriormente para un look "Premium Dark".

### 2. Lógica (ViewModel)
#### [MODIFY] [BorradorPresupuestoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/presupuesto/BorradorPresupuestoViewModel.kt)
- Asegurar la carga de identidades completa para mostrar logos y direcciones correctamente.

## Verificación Plan
### Manual
- Navegar entre las tres secciones del Rail y confirmar que las tarjetas colapsables mantienen su estado.
- Seleccionar una dirección del cliente y verificar que se guarda en el borrador de Room.
- Previsualizar el total final en la barra inferior (Bottom Bar persistente).
