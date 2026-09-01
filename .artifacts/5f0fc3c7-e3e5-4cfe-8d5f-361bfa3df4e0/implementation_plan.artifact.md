# Fix Unresolved Reference 'HonorarioPresupuesto' and CalculadoraPresupuestoMav Mismatch

The project is currently failing to build because `HonorarioPresupuesto` is missing in `core` but referenced in tests and UI components. Additionally, the `CalculadoraPresupuestoMav` implementation has drifted from the expectations of its unit tests (parameter mismatch and missing fields).

## Proposed Changes

### [core]

#### [MODIFY] [PresupuestoComun.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/entidades/PresupuestoComun.kt)
- Add `HonorarioPresupuesto` data class to match its usage in tests and UI.

#### [MODIFY] [CalculadoraPresupuestoMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/motores/CalculadoraPresupuestoMav.kt)
- Update `ResultadoCalculo` to include `baseImponible` and `totalHonorarios`.
- Update `calcularTodo` to:
    - Accept `honorarios: List<HonorarioPresupuesto>` as the 3rd parameter (5 parameters total).
    - Use both `precioUnitario` and `total`/`monto` fields to ensure compatibility with different DTO usages.
    - Implement `baseImponible` logic (excluding items with explicit tax percentages) as expected by the test.
    - Maintain backward compatibility for existing 4-parameter calls by using default values.

#### [MODIFY] [CalculadoraPresupuestoMavTest.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/test/java/com/example/myapplication/core/domain/CalculadoraPresupuestoMavTest.kt)
- Ensure all calls to `calcularTodo` match the updated signature.
- Fix any remaining property access issues (e.g., `baseImponible`).

#### [MODIFY] [SnapshotFinancieroMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/mapeadores/SnapshotFinancieroMapper.kt)
- Update call to `CalculadoraPresupuestoMav.calcularTodo` to match the new signature (using named arguments or passing empty list for honorarios).

#### [MODIFY] [BorradorPresupuestoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/presupuesto/BorradorPresupuestoViewModel.kt)
- Update call to `CalculadoraPresupuestoMav.calcularTodo` to match the new signature.

## Verification Plan

### Automated Tests
- Run the unit tests:
  ```
  ./gradlew :core:testDebugUnitTest --tests "com.example.myapplication.core.dominio.CalculadoraPresupuestoMavTest"
  ```

### Manual Verification
- Verify the project builds successfully:
  ```
  ./gradlew :core:compileDebugUnitTestKotlin
  ```
