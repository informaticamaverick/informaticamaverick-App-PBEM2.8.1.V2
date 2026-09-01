# Plan de Refinamiento: Sistema de Notificaciones Elite (App Cliente)

Este plan corrige los fallos críticos en las notificaciones de la App del Cliente, implementa la gestión de canales (silenciar secciones) y añade indicadores visuales (badges) en la navegación, siguiendo los estándares de Google 2026 y las **Leyes Maverick**.

## Auditoría Técnica: Fallos Identificados

1.  **Ausencia de Service FCM**: La app no tiene un `FirebaseMessagingService` implementado, lo que impide recibir notificaciones cuando la app está cerrada o en segundo plano.
2.  **Gestión de Canales Inexistente**: No hay una forma de activar/desactivar notificaciones por categoría (Chat, Calendario, Licitaciones).
3.  **Fuga de Estado en Badges**: La barra de navegación solo detecta notificaciones de chat, ignorando presupuestos nuevos y eventos de agenda.
4.  **Badges Invisibles**: Los puntos de notificación no se actualizan en tiempo real en todas las pestañas.

## Cambios Propuestos

### 1. Núcleo de Datos (`:core`)

#### [BudgetDao.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/dao/BudgetDao.kt)
- Añadir consulta para contar presupuestos no leídos.

```kotlin
@Query("SELECT COUNT(*) FROM budgets WHERE isRead = 0")
fun getTotalUnreadBudgets(): Flow<Int>
```

---

### 2. Módulo de Aplicación (`app`)

#### [NEW] [UserSettingsRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UserSettingsRepository.kt)
- Implementar almacenamiento persistente (DataStore) para preferencias de notificación:
    - `notifChat`: Habilitar/Deshabilitar mensajes.
    - `notifCalendar`: Recordatorios y citas.
    - `notifTenders`: Actualizaciones de licitaciones y nuevos presupuestos.
    - `notifPromos`: Publicaciones de prestadores.

#### [NEW] [MaverickFirebaseMessagingService.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/MaverickFirebaseMessagingService.kt)
- Implementar el servicio para procesar mensajes entrantes de FCM.
- **Ley #2 (Costo Zero)**: Sincronizar Room en segundo plano antes de mostrar la notificación.
- Filtrar notificaciones basadas en las preferencias del `UserSettingsRepository`.

#### [BeBrainViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/BeBrainViewModel.kt)
- Añadir `hasBudgetNotifications` y `hasCalendarNotifications` (observando DAOs).
- Consolidar estados para la barra de navegación.

#### [AppNavigation.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/AppNavigation.kt)
- Actualizar `AppBottomNavigationBar` para mostrar badges en las pestañas correspondientes (Dinero -> Presupuestos, Calendario -> Agenda).

#### [NEW] [NotificacionesConfigScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/NotificacionesConfigScreen.kt)
- Crear interfaz premium para que el usuario gestione sus alertas.

---

## Estrategia de "Grandes Ligas" 2026

*   **Sincronización Silenciosa**: Al recibir un presupuesto vía FCM, la app actualiza Room antes de avisar al usuario. Si el usuario abre la app, el dato ya está ahí (Cero carga).
*   **Prioridad Adaptativa**: Uso de `NotificationManager.IMPORTANCE_HIGH` solo para mensajes y citas confirmadas. Notificaciones de marketing (Promos) irán con prioridad baja para no molestar.
*   **Deep Linking**: Al tocar una notificación de presupuesto, la app abrirá directamente el detalle de la licitación afectada.

## Plan de Verificación

### Pruebas de Integridad
1.  **Background Check**: Cerrar la app totalmente -> Enviar mensaje desde App Prestador -> Verificar llegada de notificación Android.
2.  **Silencio Táctico**: Desactivar "Presupuestos" en ajustes -> Enviar presupuesto -> Verificar que Room se actualiza pero NO suena ni aparece notificación.
3.  **Real-time Badges**: Recibir un mensaje con la app abierta en la pestaña Home -> Verificar que aparece el punto azul en el icono de Chat instantáneamente.
