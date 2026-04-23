# Plan de Acción: Licitaciones con Notificaciones por Temas (Costo Cero)

Este plan utiliza **FCM Topics** para notificar a cientos de prestadores de forma instantánea y gratuita, y solo crea chats cuando hay un interés real (envío de presupuesto).

## User Review Required

> [!IMPORTANT]
> **Estrategia de Chats**: El cliente NO abrirá chats con todos. El chat se creará automáticamente solo cuando un prestador envíe su presupuesto. Así, el cliente solo verá conversaciones con interesados reales.
>
> **FCM Topics**: La App del Prestador debe suscribirse a temas basados en su CP y Categoría (ej: `tender_T4000_Gasista`). Esto permite que el cliente envíe **una sola notificación** y Firebase la distribuya a todos.
>
> **Ciclo de Vida (TTL)**: Las licitaciones en Firestore tendrán un campo de expiración para borrado automático. El cliente conservará su copia en Room.

## Proposed Changes

### 1. App Prestador: Suscripción y Visualización
#### [ProfileViewModel / Login]
- Al guardar el perfil o iniciar sesión, suscribirse a los temas correspondientes:
  `FirebaseMessaging.getInstance().subscribeToTopic("tender_${cp}_${categoria}")`.

#### [NEW] [DetalleLicitacionScreen (Prestador)]
- Pantalla que lee los datos (incluyendo URLs de imágenes) desde Firestore usando el `tenderId` de la notificación.
- Botón "POSTULARSE" que abre el formulario de presupuesto.

---

### 2. App Cliente: Emisión de Licitación
#### [BudgetViewModel](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/BudgetViewModel.kt)
- `createTender`:
    1. Comprime imágenes a WebP y las sube a Firebase Storage.
    2. Guarda en Room local (Persistencia permanente).
    3. Sube a Firestore (`tenders_active`) con un campo `expiresAt` para TTL.
    4. Envía una notificación FCM al tema: `topic = "tender_${cp}_${categoria}"`.

---

### 3. Flujo de Postulación (Prestador -> Cliente)
#### [ChatRepository](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/data/repository/ChatRepository.kt)
- Al enviar el presupuesto:
    1. Crea la conversación en Firestore/RTDB si no existe.
    2. Envía el presupuesto como un mensaje de tipo `BUDGET` (JSON comprimido).

---

### 4. App Cliente: Visualización
#### [PresupuestosScreen](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/PresupuestosScreen.kt)
- La tarjeta de licitación mostrará el contador de presupuestos recibidos.
- Al abrir la licitación, se ven los presupuestos que llegaron vía chat asociados a ese `tenderId`.

## Verification Plan

### Manual Verification
1. **TTL**: Verificar en la consola de Firebase que los documentos de licitación tienen configurado el borrado automático.
2. **Storage**: Confirmar que las imágenes se suben como WebP y son legibles desde la App del Prestador.
3. **Notificación**: Verificar la recepción de la push masiva al publicar una licitación.
