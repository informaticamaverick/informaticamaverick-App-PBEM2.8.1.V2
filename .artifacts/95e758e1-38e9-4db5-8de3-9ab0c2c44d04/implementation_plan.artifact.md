# Plan Maestro: Ciclo de Vida de Tópicos e Índices (v2026.ELITE)

Este plan detalla la orquestación final para la creación, actualización y limpieza de tópicos e índices en ambas aplicaciones, asegurando que la red siempre refleje el estado actual del perfil del usuario.

## Auditoría de Ciclo de Vida

### 1. Inicio y Creación de Cuenta
- **Acción**: Al registrarse o iniciar sesión por primera vez, el sistema debe realizar un "Warm-up" total.
- **Logica**: Los Coordinadores recolectarán el estado inicial (CP y Categorías) y realizarán la primera suscripción masiva, persistiendo todo en `suscripciones_topic_mav`.

### 2. Edición de Perfil (Higiene Proactiva)
- **Acción**: Al modificar categorías, rubros o direcciones.
- **Lógica**:
    - **App Naranja**: Recalcular la Matriz de Búsqueda y actualizar Firestore (`indice_busqueda`). Sincronizar hilos de red para suscribir a nuevos rubros/zonas y desvincular de los eliminados.
    - **App Azul**: Actualizar suscripciones a zonas y rubros de interés basados en el perfil del cliente y sus shortcuts.

### 3. Interacción y Búsqueda (App Azul)
- **Acción**: Al buscar un nuevo rubro en una zona distinta.
- **Lógica**: Suscribir temporalmente al tópico de esa zona/rubro para recibir "Alertas de Ofertas" relevantes, registrando la intención en Room.

## Modificaciones Técnicas

### [Módulo :core]

#### [MODIFY] [MotorDescubrimientoMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/domain/engine/MotorDescubrimientoMav.kt)
- Añadir logs estandarizados `[HUELLA_GEN]` para trazabilidad.

### [App Azul - Cliente]

#### [MODIFY] [CoordinadorAccionesMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/CoordinadorAccionesMav.kt)
- **Nueva Función**: `sincronizarEcosistemaRed(cuenta, direccionActiva)`
    - Realiza el diferencial de tópicos (Nuevos vs Antiguos).
    - Suscribe a `Z_`, `O_` y `C_` según el perfil del cliente.

#### [MODIFY] [BeCerebroViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/BeCerebroViewModel.kt)
- Observar cambios en `estadoCuenta` y `direccionActiva` para disparar la sincronización del coordinador.

### [App Naranja - Prestador]

#### [MODIFY] [CoordinadorPrestadorMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/global/CoordinadorPrestadorMav.kt)
- Reforzar la higiene de red para eliminar tópicos de sucursales o rubros borrados.
- Añadir logs `[RED_SYNC_PRO]`.

#### [MODIFY] [PrestadorPerfilRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/data/repository/PrestadorPerfilRepository.kt)
- Asegurar que cualquier guardado de perfil o sucursal dispare la actualización del índice de búsqueda.

## Plan de Verificación

### Trazabilidad (Logcat)
- Filtrar por `[TOPIC_SYNC]` y verificar que al cambiar una dirección en el perfil, se disparen los logs de `UNSUBSCRIBE` del CP viejo y `SUBSCRIBE` del nuevo.

### Integridad en Firebase
- Verificar que el `indice_busqueda` se actualice inmediatamente después de guardar el perfil en la App Naranja.
