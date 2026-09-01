# Plan de Implementación: Sincronización Deep y Verificación Táctica (v2026.ELITE)

Este plan asegura que la App Azul (Cliente) recupere la jerarquía completa de datos del prestador (Reseñas, Staff, Recursos) de forma eficiente, verificando timestamps antes de realizar descargas pesadas.

## User Review Required

> [!IMPORTANT]
> **Sincronización de Reseñas**: Se habilitará la subida y bajada de comentarios certificados en Firestore.
> **Restauración de Infraestructura**: Se completará la lógica para que los clientes puedan ver el equipo de trabajo y los recursos de las sucursales.
> **Verificación de Actualización**: La App Azul solo descargará el perfil "Deep" si detecta que la versión en la nube es más reciente que la local, ahorrando datos y batería (Ley #2).

## Proposed Changes

### [Módulo :core] (Sincronización)

#### [MODIFY] [MotorSincronizacionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/MotorSincronizacionMav.kt)
- **Inyectar `ReviewDao`**: Para manejar la persistencia de opiniones.
- **Implementar `sincronizarReseñas`**: Función para subir/bajar la colección `reseñas` del prestador.
- **Completar `sincronizarInfraestructura`**: Implementar la restauración (download) de Miembros y Recursos desde Firestore a Room.
- **Optimizar `sincronizarIdentidad`**: Añadir un paso de verificación donde se compare el `ultimaSincronizacion` del documento de Firestore con el de Room antes de disparar la sincronización jerárquica.

### [Módulo :app] (App Azul - Recuperación)

#### [MODIFY] [UsPrestadorRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UsPrestadorRepository.kt)
- Asegurar que `descargarPerfilPrestador` invoque la sincronización de reseñas e infraestructura.
- Refinar la lógica de "Frescura" de datos para que coincida con la del Motor central.

### [Módulo :ui-shared] (Visualización)

#### [MODIFY] [HojaReseñasPrestadorMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/parts/HojaReseñasPrestadorMav.kt)
- Verificar que los datos recuperados de Firestore se mapeen correctamente al UI Model de reseñas.

## Verification Plan

### Manual Verification
- **Prueba de Subida (App Naranja)**: Crear un nuevo recurso en una sucursal, subir a la nube. Verificar en la consola de Firebase que los datos estén presentes.
- **Prueba de Bajada (App Azul)**: Entrar al perfil del mismo prestador desde la App Azul. Verificar que aparezcan las reseñas, el staff y los recursos actualizados.
- **Costo Zero**: Entrar dos veces seguidas al mismo perfil en la App Azul. Verificar en Logcat que la segunda vez diga `[SYNC_SKIP]` porque los datos ya están frescos.
- **Integridad Visual**: Confirmar que la cabecera Telegram muestre el conteo real de comentarios recuperados de Firestore.
