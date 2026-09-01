# Task: Refuerzo de Arquitectura y Alineación con Leyes Maverick

## Status
- [ ] Implementar Ley #8 (Tránsito Efímero) para Presupuestos y Licitaciones (RTDB)
- [ ] Refactorizar `BudgetMavRepository` (RTDB para transporte de presupuestos)
- [ ] Implementar compatibilidad Firestore en Entidades de Identidad
- [ ] Refactorizar `BudgetTemplateEntity` (Tipado fuerte)
- [ ] Unificar `BudgetSummaryUiModel` y limpiar duplicados
- [ ] Centralizar lógica de mapeo en `BudgetEntity`

## Subtasks

### 1. Tránsito Efímero y RTDB
- [ ] Modificar `BudgetMavRepository.kt` para usar RTDB como transporte de presupuestos (Licitaciones y Directos)
- [ ] Implementar eliminación automática en RTDB tras el guardado local en Room
- [ ] Adaptar `BudgetDataMapper.kt` para soportar RTDB

### 2. Estructura y Compatibilidad
- [ ] Modificar `IdentidadUsuarioMavEntity.kt`, `IdentidadPrestadorMavEntity.kt`, `CuentaMavEntity.kt` (id = "")
- [ ] Modificar `BudgetTemplateEntity.kt` (Refactor a listas tipadas)

### 3. Unificación y Limpieza
- [ ] Eliminar `ui-shared/.../BudgetSummaryUiModel.kt`
- [ ] Añadir función `aModeloUi` en `PresupuestosMav.kt`
- [ ] Refactorizar `PresupuestoViewModel.kt` y actualizar importaciones de UI

### 4. Verificación
- [ ] Build `:core` y `:app`
- [ ] Verificación de flujo de datos (Logs de creación/eliminación en RTDB)
