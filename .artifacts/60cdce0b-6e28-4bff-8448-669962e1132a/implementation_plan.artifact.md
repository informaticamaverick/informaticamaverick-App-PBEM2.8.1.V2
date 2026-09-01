# Plan de Integración de Vista Previa Planilla A4 Executive

Este plan detalla la integración de la nueva pantalla de presupuestos `PlanillaPresupuestoA4` en la aplicación de Prestadores (App Naranja). Se reemplazará el antiguo sistema de vista previa por el nuevo diseño profesional A4.

## User Review Required

> [!IMPORTANT]
> Se actualizará la función de vista previa en toda la aplicación del prestador para utilizar el nuevo formato `PlanillaPresupuestoA4`. Esto incluye el Armador de Presupuestos, el Presupuesto Rápido y el Mercado de Concursos.

## Proposed Changes

### [ui-shared]

#### [MODIFY] [PlanillaPresupuestoA4.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/ui/components/PlanillaPresupuestoA4.kt)
- Corregir el mapeo para incluir `honorarios` de `PresupuestoEntity` en la categoría de "Mano de Obra".
- Implementar `PlanillaPresupuestoA4Viewer`: Un contenedor con soporte para zoom y desplazamiento (pinch-to-zoom) adaptado a las dimensiones A4 Executive (595x842 dp).
- Implementar `PlanillaPresupuestoA4Dialog`: Un diálogo a pantalla completa que encapsula el viewer y proporciona acciones como "Cerrar" y "Enviar" (con captura de imagen en Base64).

### [prestador]

#### [MODIFY] [ArmadorPresupuestoScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/presupuesto/ArmadorPresupuestoScreen.kt)
- Reemplazar el uso de `BudgetPreviewPDFDialog` por `PlanillaPresupuestoA4Dialog`.
- Asegurar que se pasen correctamente los datos de la empresa del cliente y la dirección (ya sea manual o seleccionada).

#### [MODIFY] [CrearPresupuestoRapidoSheet.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/dashboard/componentes/CrearPresupuestoRapidoSheet.kt)
- Reemplazar `BudgetPreviewPDFDialog` por `PlanillaPresupuestoA4Dialog` para mantener la consistencia visual en toda la "app naranja".

#### [MODIFY] [MercadoConcursosScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/market/MercadoConcursosScreen.kt)
- Reemplazar `BudgetPreviewPDFDialog` por `PlanillaPresupuestoA4Dialog`.

## Verification Plan

### Manual Verification
- Abrir el Armador de Presupuestos en un emulador/dispositivo.
- Cargar algunos ítems, servicios y gastos.
- Tocar el botón "Vista Previa".
- Verificar que se muestra el nuevo diseño A4 con zoom funcional.
- Probar el botón "Enviar" y verificar que se genera la captura correctamente.
- Repetir la prueba desde el "Presupuesto Rápido" en el Dashboard.
