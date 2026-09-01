# Walkthrough: Restauración de Jerarquía y Soberanía (v2026.ELITE)

Se ha corregido la arquitectura de sincronización para restaurar el uso de **Subcolecciones** en Firebase, eliminando la contaminación de datos entre Clientes y Prestadores y asegurando un ruteo determinista.

## Cambios Clave Realizados

### 1. Saneamiento del Motor Central (Core)
- **[MotorSincronizacionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/MotorSincronizacionMav.kt)**:
    - Se eliminó la función `actualizarIndiceUsuarioShallow`. Los clientes ya no se indexan en la búsqueda global.
    - El motor ahora es **Táctico**: solo maneja la sincronización bidireccional de perfiles base basándose en marcas de tiempo.
    - Se eliminaron las constantes de colecciones raíz erróneas (`direcciones`, `empresas`, `sucursales`).

### 2. Soberanía del Cliente (Subcolecciones)
- **[UsUsuarioRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UsUsuarioRepository.kt)**:
    - Se restauró la jerarquía natural: `clientes/{uid}/direcciones`.
    - Al subir datos, se utiliza un `WriteBatch` atómico que garantiza que el perfil y sus direcciones se creen simultáneamente en su lugar correcto.

### 3. Soberanía del Prestador (Ecosistema Profundo)
- **[PrestadorPerfilRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/data/repository/PrestadorPerfilRepository.kt)**:
    - Se reconstruyó la lógica de subida para soportar el ecosistema de **5 Pilares**:
        - `proveedores/{uid}`
        - `proveedores/{uid}/direcciones`
        - `proveedores/{uid}/empresas/{id}/sucursales/...`
    - Se restauraron las funciones de descarga bajo demanda de perfiles de clientes para el chat.

## Verificación de Integridad

> [!IMPORTANT]
> **Limpieza de Nube Requerida**: Se recomienda borrar las colecciones raíz `direcciones`, `empresas` y `sucursales` en la consola de Firebase, ya que el sistema ahora las creará correctamente dentro de los documentos de sus respectivos dueños.

> [!TIP]
> **Privacidad del Cliente**: Con esta restauración, los clientes han dejado de aparecer en los resultados de búsqueda, devolviendo el `indice_busqueda` a su propósito original: el descubrimiento de profesionales.

## Resultados Finales
1.  **Orden en Consola**: Los datos son ahora fáciles de auditar visualmente en Firebase.
2.  **Seguridad Robusta**: Las reglas de seguridad pueden ser más restrictivas al heredar permisos del documento padre.
3.  **Rendimiento Atómico**: El uso de `WriteBatch` asegura que no existan estados parciales durante el registro inicial.
