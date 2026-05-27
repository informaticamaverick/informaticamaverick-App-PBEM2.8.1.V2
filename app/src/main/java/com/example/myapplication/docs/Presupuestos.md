# 💰 MÓDULO: PRESUPUESTOS Y LICITACIONES

Este módulo gestiona la creación de concursos públicos y la recepción de ofertas técnicas mediante un flujo optimizado de red.

---

## 📂 1. ARCHIVOS CLAVE Y RESPONSABILIDADES

### UI (Screens & Sheets)
*   [`PresupuestosScreen.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/budget/PresupuestosScreen.kt): Lista principal de licitaciones y presupuestos directos.
*   [`LicitacionCrearNuevaSheet.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/budget/LicitacionCrearNuevaSheet.kt): Formulario de creación con soporte multimedia.
*   [`LicitacionResultadoScreen.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/LicitacionResultadoScreen.kt): Comparador visual de presupuestos.

### Lógica (Obreros & Datos)
*   [`BudgetViewModel.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/budget/PresupuestoViewModel.kt): **El Obrero**. Gestiona el estado de selección, filtrado y envío.
*   [`BudgetRepository.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/BudgetRepository.kt): Gestiona Firestore, Storage y FCM.
*   [`BudgetDao.kt`](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/local/dao/BudgetDao.kt): Consultas Room especializadas.

---

## 🛰️ 2. ESTRATEGIA DE COMUNICACIÓN (FCM TOPICS)

El flujo de licitaciones no busca prestadores uno a uno. Utiliza un sistema de **Broadcasting Selectivo** basado en temas (Topics) para optimizar el consumo de recursos y la velocidad de respuesta.

### A. Estándar de Nomenclatura (SSOT)
Todo Topic debe seguir el formato prolijo y normalizado:
`tender_{cp}_{categoria}`

*   **Ejemplo Real:** Una licitación de "Plomería" en el CP "T4000" genera el topic: `tender_t4000_plomeria`.
*   **Normalización:** Se deben eliminar espacios, tildes y caracteres especiales antes de generar la clave (vía `normalizeForTopic()`).

### B. Flujo Técnico de Notificación
1.  **Obrero (`BudgetViewModel`):** Calcula el `matchKey` usando los datos de ubicación (Bento Location) y el rubro seleccionado.
2.  **Repositorio (`BudgetRepository`):** Llama a `sendTopicNotification()`.
3.  **Firebase (FCM):** Google actúa como el mediador masivo, entregando la notificación solo a los prestadores que tienen ese Topic en su lista de suscripciones activas.

---

## 🔄 3. FUNCIONES Y FLUJO DE DATOS (Kotlin Deep Dive)

### A. Ciclo de Creación (Licitación)
| Función | Parámetros Clave | Entrega | Acción |
| :--- | :--- | :--- | :--- |
| `createTender()` | `title, desc, category, images, location` | `Job` | Procesa el envío masivo. |
| `uploadTenderImage()` | `tenderId, bytes` | `String?` | Sube a Firebase Storage (WebP). |
| `sendTopicNotification()` | `topic, title, body` | `Unit` | Dispara FCM a prestadores del rubro/zona. |

### B. Gestión de Ofertas
*   **Función:** `acceptBudget(budget: BudgetEntity)`
*   **Lógica:**
    1.  Llama a `repository.updateBudgetStatus(id, BudgetStatus.ACEPTADO)`.
    2.  Actualiza la licitación a `status = "ADJUDICADA"`.
    3.  Llama a `repository.removeFromCloud(tenderId)` (Elimina de Firestore para ahorrar lecturas).

---

## 🛠️ 3. PROCEDIMIENTOS TÉCNICOS

### Cómo crear una Licitación (Paso a Paso)
1.  **Captura UI:** `CrearLicitacionContent` captura los datos y los pasa al ViewModel.
2.  **Preparación de Datos:** El ViewModel genera un `tenderId` único (`UUID.randomUUID().toString().take(8)`).
3.  **Multimedia:** Se comprime la imagen a WebP vía `ImageUtils.compressImageToWebP`.
4.  **Match Key (FCM):** Se genera la clave del canal de comunicación:
    ```kotlin
    val matchKey = "tender_${postalCode}_${category.normalize()}"
    ```
5.  **Persistencia Dual:**
    *   `budgetDao.insertTender(tender)` -> Inmediato.
    *   `firestore.collection("LicitacionesAbiertas").document(tenderId).set(tender)` -> Nube.

### Cómo modificar una Sheet relacionada
1.  **Molde:** Las sheets deben usar `SheetEmergenteVertical`.
2.  **Navegación:** `PresupuestosScreen` controla la visibilidad mediante un flag `remember { mutableStateOf(false) }`.
3.  **Contexto Be:** Al abrir la sheet, se debe llamar a `beViewModel.onRouteChanged("crear_licitacion")` para que el asistente cambie su HUD.

---

## 💾 4. RELACIÓN CON FIREBASE Y ROOM

*   **Firestore:** Solo para **Licitaciones ABIERTAS**. Permite que los prestadores vean el trabajo disponible.
*   **Room:** Fuente de verdad para el **Historial Completo**.
*   **Firebase Storage:** Almacena imágenes en la carpeta `tenders/{tenderId}/`.
*   **FCM (Legacy API):** Usado para notificar a los prestadores suscritos a un "Topic" zonal.
