# Walkthrough: Licitaciones con Notificaciones Inteligentes (Costo Cero)

Se ha implementado el flujo completo de creación de licitaciones con notificaciones segmentadas por Código Postal y Categoría, utilizando una arquitectura de costo cero y alta eficiencia.

## Cambios Realizados

### 1. Modelo de Datos y Persistencia
- **[BudgetModels.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/local/BudgetModels.kt)**: Se actualizó `TenderEntity` para incluir campos críticos como `locationPostalCode`, `matchKey` (para topics), `expiresAt` (para TTL), y datos detallados del emisor (Cliente o Empresa).

### 2. Lógica de Negocio (App Cliente)
- **[BudgetRepository.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/BudgetRepository.kt)**:
    - Se integró **Firestore** (`LicitacionesAbiertas`) y **Firebase Storage**.
    - Se implementó `uploadTenderImage` para subir imágenes comprimidas.
    - Se añadió `removeFromCloud` para eliminar licitaciones de la nube al adjudicarse o cancelarse (ahorro de costos).
    - Se implementó `sendTopicNotification` para avisar masivamente a los prestadores vía FCM Topics.
- **[BudgetViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/BudgetViewModel.kt)**:
    - `createTender` ahora comprime imágenes a **WebP** usando `ImageUtils`.
    - Genera automáticamente el `matchKey` (ej: `tender_T4000_Gasista`).
    - Sincroniza con la nube y envía la notificación push al tema correspondiente.

### 3. App Prestador (Recepción)
- **[EditProfileViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/EditProfileViewModel.kt)**:
    - Se implementó `syncTopics` que suscribe automáticamente al prestador a los temas que coinciden con su **Código Postal** y **Categorías de Servicio**.
    - La suscripción solo se activa si el prestador es **Premium** (`isSubscribed == true`).

## Verificación
1. **Compresión**: Las imágenes se convierten a WebP antes de subir, minimizando el uso de Storage.
2. **Ciclo de Vida**: Las licitaciones se eliminan de Firestore cuando cambian a `ADJUDICADA`, `CANCELADA` o `CERRADA`, manteniendo la copia en el **Room** local del cliente.
3. **Escalabilidad**: El uso de **FCM Topics** permite notificar a miles de prestadores en todo el país con una sola operación de red desde el cliente.

## Notas Técnicas
- Se utilizó la colección en español `LicitacionesAbiertas` como se solicitó.
- El sistema es compatible con el modo offline, asegurando que la licitación se guarde localmente incluso sin conexión.
