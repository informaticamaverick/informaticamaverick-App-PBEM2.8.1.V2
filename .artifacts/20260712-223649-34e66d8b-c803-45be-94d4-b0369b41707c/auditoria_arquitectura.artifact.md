# Auditoría de Arquitectura: Pilares y Bases (V2026.FINAL)

Esta auditoría revisa la integridad y coherencia de la capa de datos y dominio del proyecto Informática Maverick.

## 📊 Resumen Ejecutivo
El proyecto presenta una base sólida basada en Room para persistencia local y Firestore para la nube, con una clara separación de responsabilidades. Sin embargo, se han detectado inconsistencias críticas en la duplicidad de modelos y en la serialización de datos que podrían causar errores en tiempo de ejecución.

---

## 🏛️ 1. Entidades (Room Entities)

### Puntos Fuertes:
- **Indexación Táctica**: Las entidades como `TenderEntity` y `CategoryEntity` tienen índices bien definidos para optimizar búsquedas.
- **Atribución Clara**: Uso correcto de `@PrimaryKey` y nombres de tabla legibles.

### Hallazgos de Auditoría:
- ⚠️ **Inconsistencia en `BudgetTemplateEntity`**: Utiliza campos `itemsJson: String`, mientras que `BudgetEntity` utiliza `List<BudgetItem>`. Esto rompe la uniformidad del sistema de tipos y desaprovecha los `Converters` existentes.
- ⚠️ **Riesgo en Firestore (`toObject`)**: `IdentidadUsuarioMavEntity` y `CuentaMavEntity` no tienen valores por defecto para el campo `id`. Esto puede causar que `doc.toObject()` falle al intentar instanciar la clase sin argumentos.

---

## 🛠️ 2. DAOs (Data Access Objects)

### Puntos Fuertes:
- **Reactividad Nativa**: Uso extensivo de `Flow<List<T>>` para garantizar que la UI se actualice automáticamente ante cambios en la DB.
- **Operaciones Atómicas**: Los DAOs están bien segmentados por funcionalidad.

### Recomendaciones:
- Mantener la consistencia en el uso de `OnConflictStrategy.REPLACE`.

---

## 🔄 3. Converters (Room TypeConverters)

### Puntos Fuertes:
- **Centralización**: `Converters.kt` centraliza toda la lógica de Gson para tipos complejos y Enums.
- **Seguridad**: Implementación de `try-catch` en conversores de Enums para evitar crashes por valores inesperados.

### Hallazgos:
- La inconsistencia mencionada en `BudgetTemplateEntity` hace que este use lógica manual en lugar de aprovechar estos conversores.

---

## 🗺️ 4. Mappers (Data Transformation)

### Hallazgos de Auditoría:
- 🔴 **Duplicidad de Modelos Crítica**: El modelo `BudgetSummaryUiModel` existe de forma idéntica en:
    1. `com.example.myapplication.core.domain.model`
    2. `com.example.myapplication.uishared.models`
  Esto causa confusión en las importaciones y posibles errores de casting en el futuro.
- **Lógica en ViewModel**: Parte del mapeo de `BudgetSummaryUiModel` ocurre directamente en `PresupuestoViewModel.kt`. Se recomienda mover esta lógica a una función de extensión en `BudgetEntity`.

---

## 🧬 5. Modelos de Dominio

### Puntos Fuertes:
- **Ley #3 (Shallow)**: Los modelos UI son ligeros, evitando cargar listas pesadas en memoria durante el scroll.
- **Legacy Bridge**: Buen uso de aliases para mantener compatibilidad con código antiguo.

---

## 🚀 Plan de Acción Recomendado

1. **Unificación de Modelos**: Eliminar `uishared.models.BudgetSummaryUiModel` y centralizar todo en `core.domain.model`.
2. **Corrección de `BudgetTemplateEntity`**: Cambiar los campos String JSON por Listas tipadas para usar los `Converters`.
3. **Robustez en Firestore**: Añadir `= ""` como valor por defecto a los IDs de las identidades.
4. **Refactorización de Mappers**: Mover la lógica de combinación de categorías y presupuestos a un mapper especializado o a la entidad.

---
> [!IMPORTANT]
> Se recomienda proceder con la unificación de modelos inmediatamente para evitar problemas de compilación cruzada entre módulos.
