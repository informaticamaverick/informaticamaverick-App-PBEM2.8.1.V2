# 🔍 Auditoría Técnica: Flujo de Licitaciones y Presupuestos (v2026)

## 📋 Estado Actual (Hallazgos)

1.  **Imágenes de Licitación**: El `BudgetDataMapper` mapea correctamente `imageUrls`, pero si la sincronización remota no se dispara antes de abrir el `ModalBottomSheet`, las imágenes no aparecen (Ley #3: Shallow loading mal implementado).
2.  **Selección de Cliente**: La pantalla `CrearPresupuestoPrestadorScreen` ignora el contexto de la licitación y obliga al prestador a buscar un cliente manualmente, rompiendo la fluidez del "Mercado".
3.  **Envío Simulado**: La lógica de envío actual solo dispara una notificación local y cambia un estado en Room, pero no persiste nada en la nube (Firestore).
4.  **Saturación de Chat**: Existe el riesgo de que al responder licitaciones se generen cientos de hilos de chat innecesarios.

---

## 🚀 Soluciones Factibles y Estrategia "Grandes Ligas" (2026)

### 1. Sincronización Eficiente (Leyes Maverick #2 y #3)
*   **Problema**: Cargar 100 presupuestos completos consume datos y RAM.
*   **Solución 2026**: Implementar **Carga en Cascada**. El cliente solo descarga un "resumen" de la licitación. Al abrir los detalles, descarga la lista de IDs de presupuestos. Solo al tocar uno, descarga el `BudgetEntity` completo (Deep Loading).

### 2. Envío Directo vs Topics (Seguridad y Costo)
*   **Topic (FCM)**: **SOLO** para difusión de la licitación (1-N). No apto para respuestas privadas (N-1).
*   **Firestore (SSOT)**: Los presupuestos deben ir a una colección global `presupuestos` con el tag `tenderId`.
*   **Costo Zero (Ley #8)**: Al enviar un presupuesto, este viaja a Firestore. Una vez que el Cliente lo descarga a su Room local, el documento en la nube se marca para limpieza efímera (TTL o Cloud Function).

### 3. Aislamiento de Mensajería
*   **Estrategia**: Los presupuestos de licitación **no son mensajes de chat**. Se guardan en la tabla `budgets`. La UI del chat los ignora. Solo si el Cliente "Acepta" o "Inicia Consulta", se crea el primer `MessageEntity` de tipo texto para abrir el canal de comunicación.

---

## 🛠️ Plan de Acción Detallado

| Acción | Componente | Impacto |
| :--- | :--- | :--- |
| **Auto-llenado Táctico** | `:prestador` | Elimina la búsqueda redundante de cliente. |
| **Firestore Bridge** | `:core` | Implementa el envío real de `BudgetEntity` a la nube. |
| **Fix Multimedia** | `:prestador` | Asegura que las imágenes se carguen desde Firebase Storage URLs. |
| **Filtrado de Chat** | `:app` | Evita que los presupuestos de licitación aparezcan en la lista de mensajes. |
