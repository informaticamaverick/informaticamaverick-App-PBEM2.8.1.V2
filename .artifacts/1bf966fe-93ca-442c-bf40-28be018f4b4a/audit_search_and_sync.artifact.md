# Auditoría de Sistema de Búsqueda y Sincronización (v2026.ELITE)

Se ha realizado una auditoría profunda sobre el flujo de descubrimiento de prestadores y el comportamiento del asistente Be. Se han identificado redundancias lógicas y estructurales que deben ser saneadas.

## 1. Auditoría del Asistente Be (HUD)

### Hallazgos
- **Redundancia de Visibilidad**: Tanto `BeAsistenteViewModel` como `AppNavigation` realizan cálculos de visibilidad. Esto duplica el esfuerzo de recomposición.
- **Sincronización Inteligente**: El término puede ser redundante si el ViewModel ya posee el contexto HUD.

### Propuesta de Saneamiento
- Centralizar la decisión final de `mostrarBe` exclusivamente en el `BeAsistenteViewModel`.
- Eliminar comprobaciones manuales en `AppNavigation`, dejándolo como un mero contenedor reactivo.

---

## 2. Auditoría de Búsqueda vs Sincronización

### Componentes Analizados
1.  **`BusquedaPrestadorViewModel`**: Orquestador táctico de la pantalla de resultados. Correcto.
2.  **`BusquedaRemoteMediator`** (en `:core`): Descarga resultados de 'indice_busqueda'. Contiene lógica de impacto en Room para Identidades, Cuentas y Direcciones.
3.  **`SincUsuarioDwnPrestadorRepositorio`** (en `:app`): Diseñado para descargar perfiles detallados. Contiene lógica de impacto idéntica para Identidades y Cuentas.

### Redundancia Detectada (Crítica)
Existe una **duplicidad de código de persistencia** entre el `RemoteMediator` y el `SincRepository`. Ambos saben cómo guardar un "Shallow Provider" en Room, pero lo hacen de forma independiente. Esto rompe la regla de **Fuente Única de Verdad (SSOT)**.

### Inconsistencia Estructural
El servicio de descarga (`SincUsuarioDwnPrestadorRepositorio`) está en `:app`, lo que impide que el motor de búsqueda en `:core` lo utilice.

---

## 3. Plan de Saneamiento Técnico

1.  **Migración a Core**: Mover `SincUsuarioDwnPrestadorRepositorio` a `:core`.
2.  **Delegación de Impacto**: Refactorizar `BusquedaRemoteMediator` para que delegue la tarea de "Guardar en Room" al `SincRepository`.
3.  **Hidratación Unificada**: Asegurar que `BusquedaPrestadorViewModel` use el `SincRepository` si detecta que un resultado local necesita una actualización rápida antes de mostrarse.

## Respuesta a Pregunta del Usuario
> "¿el BusquedaPrestadorViewmodel está utilizando nuestras nuevas descargas sinc?"

**Respuesta**: No de forma directa. Actualmente utiliza el `RemoteMediator` que tiene su propia lógica de guardado. Con este plan, pasará a utilizar la infraestructura unificada de `SincDownPrestador`.
