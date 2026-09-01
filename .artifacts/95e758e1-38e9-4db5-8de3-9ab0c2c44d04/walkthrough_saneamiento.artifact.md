# Walkthrough: Saneamiento de Infraestructura y Topics (v2026.ELITE)

Se ha completado la unificación del sistema de descubrimiento y notificaciones, elevando las Promociones y Concursos al nivel de eficiencia de la Búsqueda de Prestadores.

## Cambios Realizados

### 1. Unificación del Motor Atómico (Ley #9)
El **[MotorDescubrimientoMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/domain/engine/MotorDescubrimientoMav.kt)** ha sido rediseñado en **Español**:
- **Estandarización de Llaves**: Ahora todas las huellas usan guiones bajos (`_`), eliminando la inconsistencia con FCM.
- **Prefijos Atómicos**:
    - `Z_` (Zona)
    - `P_` (Prestador)
    - `O_` (Oferta/Promoción)
    - `C_` (Concurso/Licitación)
- **Generadores Jerárquicos**: Unificados para crear etiquetas consistentes desde el CP hasta la categoría específica.

### 2. Carga Dual (RemoteMediator)
Se han implementado y refactorizado los mediadores para asegurar que Room sea siempre el SSOT (Single Source of Truth):
- **[PromocionRemoteMediator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/PromocionRemoteMediator.kt)**: Nueva carga paginada de ofertas.
- **[ConcursoRemoteMediator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/ConcursoRemoteMediator.kt)**: Refactorizado para usar `whereArrayContainsAny`, permitiendo descubrir concursos por múltiples huellas (Zona y Categoría) en una sola consulta.

### 3. Repositorios en Español
- Se renombró `PromotionRepository.kt` a **[PromocionRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/PromocionRepository.kt)**.
- Se actualizaron todas las referencias en `:app` y `:prestador`.
- Se eliminó la manipulación manual de strings en los flujos de publicación.

### 4. Sincronización de Red
- Los coordinadores **[CoordinadorAccionesMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/CoordinadorAccionesMav.kt)** y **[CoordinadorPrestadorMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/global/CoordinadorPrestadorMav.kt)** ahora suscriben al usuario a los tópicos estandarizados.

> [!IMPORTANT]
> **Compatibilidad 100%**: Al usar `normalizeForTopic` en la base de datos, garantizamos que el `topic` de Firebase sea **exactamente igual** a la etiqueta de búsqueda. Esto soluciona los problemas de notificaciones perdidas.

### 5. Estandarización de Colecciones y Blindaje Shallow
Se han unificado los nombres de las colecciones en Firebase y se ha garantizado la integridad de los datos de contacto:
- **Colecciones Legales**:
    - `indice_busqueda`: Descubrimiento de profesionales.
    - `indice_concursos`: Mercado de licitaciones (Anteriormente `indice_licitaciones`).
    - `indice_promociones`: Feed de ofertas y anuncios.
- **Datos de Comunicación (Shallow)**:
    - Se verificó que todos los índices incluyan el **UID del emisor**, **Nombre** y **Miniatura/Foto**. Esto permite que el receptor pueda iniciar un chat o ver el perfil del emisor instantáneamente sin realizar consultas adicionales (Ley #2 Costo Zero).
- **Limpieza de "Deprecados"**: Se comentaron todas las funciones antiguas en `MotorDescubrimientoMav` para evitar el uso accidental de normalizaciones inconsistentes.

### 6. Persistencia de Tópicos (Higiene de Red)
Se ha implementado el estándar de Grandes Ligas para la memoria de red:
- **Nueva Infraestructura Room**: Se crearon [SuscripcionTopicEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/SuscripcionTopicEntity.kt) y [SuscripcionTopicDao.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/dao/SuscripcionTopicDao.kt).
- **Control de Señales**: Los coordinadores ahora registran cada suscripción exitosa en Room. Al reiniciar la app, el sistema sabe exactamente qué canales está escuchando sin depender de Firebase o la RAM.
- **Higiene de Raíz**: Todas las funciones obsoletas de normalización han sido **comentadas totalmente** en el código fuente. Esto garantiza que no existan fallos por referencias ambiguas y obliga a usar el motor unificado.

> [!IMPORTANT]
> **Sincronización Perfecta**: Al usar la nueva tabla de Room, evitamos el bucle de re-suscripción infinita, mejorando el uso de batería y datos del usuario.

> [!TIP]
> Al comentar el código obsoleto en lugar de solo marcarlo como `@Deprecated`, hemos cortado de raíz cualquier posibilidad de "parchear" errores antiguos, forzando una arquitectura limpia y moderna.
