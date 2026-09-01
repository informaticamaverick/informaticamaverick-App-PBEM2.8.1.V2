# Plan de Refuerzo de Arquitectura de Datos (V2026.FINAL)

Este plan aborda la unificación de modelos, consistencia de entidades y el cumplimiento estricto de las **10 LEYES MAVERICK**, con foco en la **Ley #8 (Tránsito Efímero)** y la **Ley #9 (Núcleo Atómico)**.

## User Review Required

- **Flujo de Licitaciones**: Las licitaciones son públicas (Firestore), pero las **respuestas (Presupuestos)** viajan por **RTDB**.
- **Iniciación de Chat**: Un presupuesto en una licitación no abre un canal de chat automáticamente. El canal solo se crea si el cliente responde a dicho presupuesto (Ley de Tránsito Efímero).
- **Protocolo de Limpieza**: El receptor elimina el presupuesto de RTDB una vez persistido en Room.

## Proposed Changes

### [Core Module] Pilares y Bases Legales

#### [BudgetMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/BudgetMavRepository.kt)
- **Refactor**: Integrar `FirebaseDatabase` para el transporte de presupuestos.
- **Ley #8**: `enviarPresupuesto(budget: BudgetEntity)` escribe en `transito_presupuestos/{idPresupuesto}`.
- **Ley #8**: `escucharPresupuestosEntrantes(idUsuario: String)` para recibir y persistir en Room, seguido de la eliminación en RTDB.

#### [BudgetDataMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/BudgetDataMapper.kt)
- **Adaptación**: Función `fromRTDBBudget(DataSnapshot)` para la conversión desde Realtime Database.
- **Mantenimiento**: `fromFirestoreTender` sigue operando con Firestore.

#### [PresupuestosMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/PresupuestosMav.kt)
- **Centralización**: Función `BudgetEntity.aModeloUi(iconoCategoria: String?)` para evitar lógica de mapeo en ViewModels.

#### [IdentidadUsuarioMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/IdentidadUsuarioMavEntity.kt)
#### [IdentidadPrestadorMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/IdentidadPrestadorMavEntity.kt)
#### [CuentaMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/CuentaMavEntity.kt)
- **Firestore Fix**: Añadir `= ""` al `@PrimaryKey val id` para constructor vacío.

#### [BudgetTemplateEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/BudgetTemplateEntity.kt)
- **Consistencia**: Cambiar campos JSON por `List<BudgetItem>`, `List<BudgetService>`, `List<BudgetProfessionalFee>`.

---

### [App Module] UI y Experiencia de Usuario

#### [DELETE] [BudgetSummaryUiModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/models/BudgetSummaryUiModel.kt)
- Eliminar duplicado.

#### [PresupuestoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/budget/PresupuestoViewModel.kt)
- **Refactor**: Usar `aModeloUi` centralizado.
- **Flujo**: Asegurar que al responder a un presupuesto se inicie la creación del chat si no existe.

#### [Varios Archivos de UI]
- Actualizar importaciones de `BudgetSummaryUiModel` (ahora siempre desde `core`).

---

## Verification Plan

### Automated Tests
- `gradlew :core:assembleDebug`: Verificar que el cambio de tipos en `BudgetTemplateEntity` no rompe los `Converters`.
- `gradlew :app:assembleDebug`: Verificar integridad de importaciones.

### Manual Verification
1. **Tránsito Efímero**: Verificar mediante el Debugger o logs que el presupuesto aparece en RTDB y desaparece tras el guardado local.
2. **Identidad**: Comprobar que `doc.toObject(IdentidadUsuarioMavEntity::class.java)` no lanza excepciones al sincronizar desde Firestore.
3. **Mapeo**: Verificar que los presupuestos en la lista muestran el icono de su categoría correctamente.
