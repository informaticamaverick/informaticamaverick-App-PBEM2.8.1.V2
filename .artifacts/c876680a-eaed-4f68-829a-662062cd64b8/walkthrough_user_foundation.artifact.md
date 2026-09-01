# Walkthrough: Refuerzo de Cimientos del Cliente (v2026.ELITE)

Se han actualizado los cimientos del sistema para permitir que el Usuario (Cliente) gestione su propio ecosistema jerárquico de Empresas y Sucursales, garantizando la paridad técnica con el Prestador sin comprometer la privacidad.

## Cambios Realizados

### 1. Actualización de la Identidad en Room
- **[IdentidadUsuarioMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/IdentidadUsuarioMavEntity.kt)**:
    - Se añadieron los campos `cuitCuil` y `biografia`.
    - Esto permite que el cliente guarde sus datos fiscales personales y un detalle descriptivo de su perfil.
- **Base de Datos**: Se incrementó la versión a **33** en `AppDatabase.kt`.

### 2. Evolución del Modelo de UI y Mappers
- **[UsuarioUiModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/domain/model/UsuarioUiModel.kt)**: Ahora soporta la visualización de CUIT y biografía.
- **[UsuarioMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/UsuarioMapper.kt)**: Se actualizó el traductor para que estos campos fluyan correctamente desde Room y Firestore hacia la interfaz.

### 3. Repositorio de Soberanía Corporativa
- **[UsUsuarioRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UsUsuarioRepository.kt)**:
    - Se implementaron las funciones `guardarEmpresaUsuario` y `guardarSucursalUsuario`.
    - **Privacidad Blindada**: Estas funciones gestionan el árbol de datos jerárquico pero **no indexan al cliente en la búsqueda pública**, manteniendo su ecosistema totalmente privado.

## Verificación de Integridad

> [!IMPORTANT]
> **Reset de Datos Locales**: Debido al cambio de versión (v33), la base de datos local se ha reiniciado. Se recomienda volver a loguearse para que el sistema descargue la nueva estructura desde la nube.

> [!TIP]
> **Sincronización Transparente**: Al añadir una empresa o sucursal desde la app del usuario, el sistema encolará automáticamente una tarea de fondo para asegurar que los datos lleguen a Firestore en su nueva ubicación jerárquica.

## Resultados
1.  **Paridad Estructural**: El cliente ahora tiene las mismas capacidades de organización que un profesional.
2.  **Higiene de Datos**: Se separó claramente la lógica de "Ubicaciones de entrega/facturación" de los "Puntos de venta" comerciales.
3.  **Preparación para UI**: El backend local está listo para soportar un carrusel de perfiles (Personal/Empresa) también para el cliente.
