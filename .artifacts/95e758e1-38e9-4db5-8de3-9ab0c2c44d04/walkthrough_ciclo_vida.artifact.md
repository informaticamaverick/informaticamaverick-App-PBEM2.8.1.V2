# Walkthrough: Ciclo de Vida de Red y Tópicos (v2026.ELITE)

Se ha completado la orquestación final que garantiza que las suscripciones y los índices de red se mantengan sincronizados automáticamente con el perfil del usuario.

## Mejoras de Orquestación

### 1. Higiene de Red Proactiva (Differential Sync)
Los coordinadores ahora calculan el diferencial entre los tópicos deseados y los activos:
- **App Azul (Cliente)**: En [CoordinadorAccionesMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/CoordinadorAccionesMav.kt), el método `sincronizarEcosistemaRed` realiza el `UNSUBSCRIBE` automático de zonas viejas al cambiar la dirección.
- **App Naranja (Prestador)**: En [CoordinadorPrestadorMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/global/CoordinadorPrestadorMav.kt), se implementó la **Matriz de Suscripción Profesional**, vinculando todos los rubros con todos los puntos de atención.

### 2. Sincronización al Guardar (Trigger Inmediato)
Se han reforzado los repositorios para que el índice de búsqueda en la nube se actualice al instante ante cualquier cambio:
- **Reputación y Membresía**: El `PrestadorPerfilRepository` dispara el recálculo del índice al guardar el perfil, asegurando que el flag de `estaSuscrito` impacte en la visibilidad del cliente de inmediato.

### 3. Trazabilidad Táctica (Logcat)
Se han inyectado logs estandarizados para monitorear la red sin necesidad de debugear línea a línea:
- `[RED_SYNC_CLI]`: Tópicos del cliente (Z_, O_, C_).
- `[RED_SYNC_PRO]`: Tópicos del profesional (Matriz de rubros).
- `[HUELLA_MAESTRA]`: Generación de llaves en el motor atómico.

### 4. Persistencia en Room (Higiene de Datos)
Todas las suscripciones exitosas se registran en la tabla `suscripciones_topic_mav`, permitiendo que el sistema "recuerde" su estado tras un reinicio, evitando saturar los servicios de Firebase (Ley de Costo Zero).

> [!TIP]
> **Eficiencia Máxima**: El sistema ahora solo envía señales a los dispositivos que realmente están interesados en la zona y rubro actual, reduciendo el ruido visual y optimizando el consumo de batería.

## Verificación
- [x] Al cambiar la dirección en la App Azul, los logs muestran la salida de los tópicos anteriores.
- [x] Al añadir un rubro en la App Naranja, el sistema se une automáticamente a los nuevos canales de licitación (`C_`).
- [x] Los índices en Firestore reflejan los cambios de perfil en tiempo real.
- [x] Room persiste la lista de suscripciones activas.
