# Corrección de BudgetPreviewPDFDialog.kt (v2026.101)

Este plan soluciona los errores de compilación introducidos por la estandarización de `IdentidadMavEntity` y limpia el código obsoleto en el visor de presupuestos.

## Proposed Changes

### [UI Shared Component]

#### [BudgetPreviewPDFDialog.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/BudgetPreviewPDFDialog.kt)
- Reemplazar `listaEmpresas` por `empresas` en la resolución de datos del prestador.
- Reemplazar `listaSucursales` por `sucursales` para obtener la dirección del punto de venta.
- Eliminar importaciones no utilizadas (`android.os.Build`).
- Implementar o marcar como pendientes los parámetros de captura de imagen (`onCapturePng`) para evitar warnings de parámetros no usados.
- Corregir la cadena de nulabilidad en `direccionCompletaPrestador`.

## Verification Plan

### Manual Verification
1. **Visualización de Presupuesto**: Abrir el diálogo de vista previa de un presupuesto y verificar que el encabezado muestra correctamente el nombre de la empresa y la dirección de la sucursal (si existe).
2. **Sin Empresa**: Verificar que si el prestador no tiene empresa configurada, el presupuesto muestra sus datos personales (nombre y dirección base) sin errores.
