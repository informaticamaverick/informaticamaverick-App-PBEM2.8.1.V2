# Evolución de Creación de Licitaciones (Concursos Públicos)

Este plan detalla la transformación de `ConcursoCrearNuevoSheet.kt` para convertirlo en un formulario profesional de licitaciones, cumpliendo con estándares de mercado y utilizando los componentes `TextosElasticosMav` para mantener la consistencia visual.

## User Review Required

> [!IMPORTANT]
> **Subida de Imágenes**: Se implementará un selector de imágenes local. Las imágenes se enviarán como una lista de URIs (o base64/URLs tras subida) al ViewModel.
> **Categorías**: Se utilizará el listado oficial de categorías del sistema para asegurar que la licitación llegue a los prestadores correctos.

## Proposed Changes

### [app]

#### [MODIFY] [ConcursoCrearNuevoSheet.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/ConcursoCrearNuevoSheet.kt)
- **UI de Textos**: Integrar `TextCompacto` y `TextCompactoAutoFit` para etiquetas y descripciones.
- **Selector de Categoría**: Implementar un selector visual (Chips o Dropdown M3) que utilice el listado real de categorías.
- **Selector de Imágenes**: Añadir un componente para adjuntar fotos del proyecto usando el picker de Android.
- **Cláusulas Tácticas**: Añadir interruptores (Switches) o chips de filtro para:
    - Exigir visita previa.
    - Exigir garantía.
    - Exigir método de pago definido.
    - Exigir documentación del prestador.
- **Plazo de Cierre**: Añadir un selector de duración (3, 7, 14, 30 días).
- **Conexión**: Vincular todos los estados al método `crearConcurso` del `PresupuestoViewModel`.

### [core]

#### [MODIFY] [PresupuestoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/budget/PresupuestoViewModel.kt)
- Ajustar la lógica de `crearConcurso` si es necesario para asegurar que el cálculo de `fechaFin` y la gestión de `urlImagenes` sea óptima.

## Verification Plan

### Manual Verification
1. Abrir la sección de Presupuestos en la App Cliente.
2. Tocar el botón "+" del asistente Be para abrir "Nueva Licitación".
3. Verificar que aparezcan los nuevos campos:
    - Selector de imágenes.
    - Switches de cláusulas tácticas.
    - Selector de duración.
    - Selector de rubro (Categoría).
4. Publicar una licitación de prueba y verificar (en Logcat o en la lista) que los datos se persistan correctamente con los nuevos valores.
5. (Opcional) Abrir la App Prestador y verificar que la licitación aparezca en el "Mercado" con todos sus requisitos y fotos.
