# Walkthrough: Saneamiento Relacional de Direcciones (v2026.ELITE)

Se ha implementado una arquitectura relacional pura para mostrar las direcciones de los prestadores en la lista de búsqueda. Este enfoque elimina cálculos pesados en la UI y utiliza la infraestructura de tablas existente para garantizar el máximo rendimiento.

## Cambios Clave Realizados

### 1. Sembrado de Direcciones Parciales (Ley #3)
- **[BusquedaRemoteMediator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/BusquedaRemoteMediator.kt)**:
    - El "Obrero de Datos" ahora tiene una nueva misión: al descargar prestadores de la nube, inserta automáticamente un registro básico en la tabla `direcciones_mav` con la calle y el número.
    - **Resultado**: Room se puebla de forma relacional antes de que el usuario entre al perfil, permitiendo que el sistema de búsqueda use datos estructurados reales.

### 2. Eficiencia via SQL JOIN (Grandes Ligas)
- **[ResultadosBusquedaView.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/ResultadosBusquedaView.kt)**:
    - Se refactorizó la vista de búsqueda para realizar un `LEFT JOIN` entre las tablas de prestadores/sucursales y la tabla de direcciones.
    - **Resultado**: La app ya no calcula el nombre de la dirección; lo lee directamente de la base de datos como un String, reduciendo el uso de CPU a casi cero durante el scroll.

### 3. Sincronización de Identidad Líquida
- **[MotorSincronizacionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/MotorSincronizacionMav.kt)**:
    - Se actualizó para que al crear el índice de búsqueda en Firestore, incluya los campos `calle` y `numero`. Esto permite que la información fluya desde el prestador hasta el usuario final sin escalas pesadas.

## Verificación de Experiencia

> [!TIP]
> **Carga de Texto Instantánea**: Notarás que al buscar, la calle y el número aparecen en la tarjeta en el milisegundo en que se dibuja, ya que vienen directamente del JOIN de Room.

> [!IMPORTANT]
> **Ahorro de Procesador**: Hemos eliminado la necesidad de procesar coordenadas para obtener nombres de calles en la lista. El procesador del móvil ahora solo se encarga del cálculo matemático de distancia (que es ultra-ligero).

## Resultados
1.  **Higiene Estructural**: No se añadieron columnas planas redundantes a las entidades principales.
2.  **Scroll de Mantequilla**: La lista de resultados es ahora mucho más fluida al eliminar lógica de composición de strings en la UI.
3.  **Compilación Perfecta**: Todo el sistema compila y está listo para las pruebas de campo.
