# Procedimientos de Presupuestos y Licitaciones ⚖️

Este documento detalla el funcionamiento técnico, la arquitectura de datos y los pasos para operar (crear, editar, eliminar) presupuestos y licitaciones dentro del ecosistema Maverick.

## 1. Estructura de Datos (Reglas de Oro)

El sistema se divide en dos entidades principales:
*   **Licitaciones (`TenderEntity`):** Peticiones públicas creadas por el cliente para buscar profesionales.
*   **Presupuestos (`BudgetEntity`):** Ofertas técnicas y económicas enviadas por los prestadores en respuesta a una licitación o por chat directo.

### Flujo de Verdad Única (SSOT)
1.  **Room (Local):** Es la fuente de verdad para la UI. Toda lista que ve el usuario viene de Room.
2.  **Firebase Firestore:** Se usa solo para sincronizar licitaciones **ABIERTAS**. Una vez adjudicada o cerrada, se elimina de la nube para mantener la política **Zero Cost**.
3.  **Firebase Topics:** Se usa para notificaciones masivas sin costo mediante la clave `tender_{CP}_{CATEGORIA}`.

---

## 2. Operaciones Técnicas

### A. Crear una Licitación
**¿Dónde?** `BudgetViewModel.createTender()`
**Pasos Internos:**
1.  **Normalización de Ubicación:** Extrae el Código Postal y Localidad de la dirección seleccionada en el `Coordinator`.
2.  **Procesamiento de Imágenes:** Las fotos se comprimen a WebP antes de subirse a Storage.
3.  **Generación de Match Key:** Se crea el tópico (ej: `tender_t4000_plomeria`).
4.  **Persistencia Dual:** Se guarda en Room e inmediatamente se sube a la colección `LicitacionesAbiertas` de Firestore.
5.  **Notificación Push:** Se envía un mensaje al tópico generado para avisar a todos los prestadores del rubro en esa zona.

### B. Recibir y Visualizar Presupuestos
**¿Dónde?** `BudgetRepository.receiveBudgetFromChat()`
**Funcionamiento:**
*   Cuando un prestador envía un presupuesto, este llega con un `tenderId` (si es por concurso) o `null` (si es chat directo).
*   El Repositorio lo guarda en Room.
*   `BudgetViewModel` observa este cambio y actualiza `filteredTenders` o `filteredDirectBudgets`.

### C. Adjudicar o Cerrar
**¿Dónde?** `BudgetViewModel.updateTenderStatus()`
**Lógica:**
*   Si el estado cambia a `ADJUDICADA`, `CANCELADA` o `CERRADA`:
    *   Se actualiza el estado local en Room.
    *   Se llama a `repository.removeFromCloud(tenderId)` para borrarla de Firestore.
    *   **Resultado:** Los prestadores ya no la ven en su lista de "Nuevas Licitaciones", pero el cliente conserva el historial offline.

---

## 3. Cómo agregar o modificar funciones

### Para agregar un nuevo filtro:
1.  **Coordinator:** Agrega el ID del filtro en `AppActionCoordinator`.
2.  **Obrero (BudgetViewModel):** Modifica el `combine` en `filteredTenders` para incluir la lógica del nuevo filtro.
3.  **Cerebro (BeBrain):** Registra el filtro en `availableFilters` para que aparezca en el menú del HUD.

### Para modificar la estructura del presupuesto:
1.  **Models:** Edita `BudgetModels.kt` (recuerda que si cambias `Entity`, debes subir la versión de la base de datos Room).
2.  **Dao:** Agrega las queries necesarias en `BudgetDao.kt`.
3.  **UI:** Actualiza `PresupuestosScreen.kt` para "pintar" el nuevo campo.

---

## 4. Auditoría de Cumplimiento

| Regla de Oro | Estado | Observación |
| :--- | :---: | :--- |
| **¿A quién llamar?** | ✅ | Se usan ViewModels especializados para cada tarea. |
| **UI Tonta** | ✅ | `PresupuestosScreen` solo observa estados y envía órdenes al ViewModel/Coordinator. |
| **Maestro de Intenciones** | ✅ | Todas las búsquedas y filtros globales pasan por `AppActionCoordinator`. |
| **Contexto de Be** | ✅ | Sincronizado mediante `onRouteChanged` en los Composables. |
| **Política Zero Cost** | ✅ | Implementada mediante eliminación proactiva en Firestore y uso de Topics. |

> [!IMPORTANT]
> **Prohibición:** Nunca realices un `.filter` de la lista de licitaciones dentro de un Composable. Hazlo siempre en el ViewModel usando `Flow.combine`.
