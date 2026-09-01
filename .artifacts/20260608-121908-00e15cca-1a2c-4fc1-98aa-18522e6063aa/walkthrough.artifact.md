# Walkthrough: Sistema Topik - Licitaciones y Concursos

Se ha implementado el flujo completo para que los prestadores reciban, visualicen y respondan a licitaciones (concursos públicos) enviadas desde la app de clientes a través de Topics de Firebase (Topik).

## Cambios Realizados

### ⚙️ Core (:core)
- **[BudgetRepository.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/BudgetRepository.kt)**: Se añadió `syncTenderFromRemote(tenderId)` para asegurar que el prestador tenga los detalles de la licitación en su base de datos local (Room) al hacer click en una notificación.

### 📱 App Prestador (:prestador)
- **[NotificacionesViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/dashboard/NotificacionesViewModel.kt)**: Se integró la lógica para cargar detalles de una licitación seleccionada.
- **[LicitacionTopikPresupuesto.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/dashboard/components/LicitacionTopikPresupuesto.kt)**: Nueva Sheet emergente (Elite M3) que muestra:
    - Título y Descripción.
    - Evidencia Multimedia (Fotos).
    - Ubicación (CP).
    - Cláusulas requeridas (Visita, Garantía, etc.).
    - Botón de acción para elaborar presupuesto.
- **[NotificacionesScreen.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/notifications/NotificacionesScreen.kt)**: Se actualizó la tarjeta de notificación para manejar el tipo `LICITACION` y disparar la visualización del concurso.
- **[MainActivity.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/MainActivity.kt)**: Implementación de ruteo profundo (Deep Linking). Al pulsar una notificación con `tenderId`, la app abre directamente en la pestaña de alertas con el concurso desplegado.
- **Navegación**: Se actualizaron las rutas y los grafos para permitir pasar el `tenderId` al formulario de creación de presupuestos, asegurando que el presupuesto final quede vinculado al concurso original.

## Cambios Realizados (App Usuario)

- **[BudgetViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/budget/PresupuestoViewModel.kt)**: Se optimizó el flujo de datos para que, al abrir una licitación desde la lista, se dispare automáticamente `syncBudgetsForTender(tenderId)`. Esto asegura que el cliente vea las ofertas de los prestadores en tiempo real (Ley de Inmediatez).
- **[BudgetRepository.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/BudgetRepository.kt)**: Implementación de `syncBudgetsForTender(tenderId)` en el core para centralizar la lógica de red local-first.
- **Limpieza de Código**: Se identificaron y marcaron como redundantes archivos en el paquete `presentation/client` que han sido superados por la arquitectura de `features/`.

## Configuración de Firebase
- Se ha generado un documento de guía: [firebase_config.artifact.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/.artifacts/20260608-121908-00e15cca-1a2c-4fc1-98aa-18522e6063aa/firebase_config.artifact.md) con las reglas de Firestore y la estructura de Topics necesaria.

## Identidad Profunda y Sync (Fix v3.0)
Se han solucionado los problemas críticos de suscripción detectados en los logs:
- **Sync de Direcciones**: El sistema ahora recolecta los CPs no solo del perfil personal, sino de **todas las sucursales de tus empresas**. Esto garantiza que si estás en "Modo Empresa", sigas recibiendo licitaciones en las zonas donde operan tus sedes.
- **Edición Desbloqueada**: Se eliminó el bloqueo de edición en el perfil personal cuando el "Modo Empresa" está activo. Ahora puedes añadir rubros y servicios a tu perfil personal sin restricciones, asegurando que tus datos estén listos si decides desactivar el modo empresa.
- **Expansión de Rubros**: Se aumentó el límite de categorías permitidas de 5 a 10, permitiendo una mayor cobertura de servicios en el Mercado.

## Auditoría Táctica (Ley #7)
Se han añadido logs estandarizados bajo el tag `[TOPIK_FLOW]` para auditar el ciclo de vida del dato:
- **App Usuario**: `[TENDER_CREATED]` al publicar un concurso en un Topic.
- **App Prestador**: `[NOTIFICATION_RECEIVED]` al capturar el mensaje de Firebase.

## Mercado de Licitaciones (App Prestador)
Se ha expandido la interfaz principal para incluir una nueva sección de exploración:
- **Nueva Pestaña "Mercado"**: Acceso desde la barra de navegación (Icono de Gavel/Martillo).
- **MercadoLicitacionesScreen.kt**: Pantalla dedicada a listar concursos públicos activos.
- **Navegación**: Se actualizó el ruteo de notificaciones al índice 5 (Alertas) para mantener la consistencia con la nueva pestaña de Mercado (índice 4).

## Cumplimiento de Leyes Maverick (README_CORE.md)
1. **Pantallas Tontas (Law #1)**: Toda la lógica de carga de datos reside en el ViewModel.
2. **Costo Zero (Law #2)**: Sincronización proactiva y local-first.
3. **Trazabilidad (Law #7)**: Logs estandarizados para auditoría de ruteo.

## Verificación

### Manual
1. **Flujo de Notificación**: Al recibir un intent con `tenderId`, la app navega a la pestaña 4 y abre la `LicitacionTopikPresupuesto`.
2. **Visualización de Concurso**: Los datos se cargan correctamente desde Room (o Firebase si es nuevo).
3. **Conversión a Presupuesto**: Al pulsar "ELABORAR PRESUPUESTO", se abre el formulario con el `tenderId` inyectado, permitiendo una respuesta inmediata.

## Sincronización y Mercado (Fix v2.0)
Se han detectado y corregido los fallos de sincronización que impedían ver datos en el Mercado:
- **Sincronización Activa**: El Mercado ya no espera pasivamente a una notificación. Al entrar o pulsar "Refrescar", el sistema consulta Firestore usando el `matchKey` generado de las categorías y CP del prestador (Ley #2).
- **Integridad de Match**: Se verificó que la generación de la clave de ruteo (`tender_t4000_plomero`) sea idéntica en el emisor (Usuario) y receptor (Prestador).
- **Logs de Depuración**: Se expandieron los logs `[TOPIK_FLOW]` para incluir el estado de las consultas a Firestore y el filtrado local.

## Notificaciones Reales
- **Importante**: Las notificaciones Push (Topic) requieren una **Cloud Function** en Firebase para ejecutarse en el servidor de forma segura. El código de la App Usuario actualmente dispara el log `[TENDER_CREATED]`. Para que la App Prestador reciba el push físico, debes implementar el trigger en Firebase (ver `firebase_config.artifact.md`).

---
**Informática Maverick - Departamento de Arquitectura de Software (2024)**
