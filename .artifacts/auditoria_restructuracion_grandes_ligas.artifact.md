# Auditoría de Reestructuración: "Grandes Ligas" (v2026.ELITE)

He realizado un análisis profundo del flujo de datos tras la descentralización de los repositorios de sincronización. Este reporte identifica exactamente qué piezas dependían del antiguo motor y qué necesitan para operar bajo el nuevo estándar de independencia.

## 1. Mapa de Dependencias del `MotorSincronizacionMav`

| Archivo | Función Requerida | Estado Actual | Necesidad "Grandes Ligas" |
| :--- | :--- | :--- | :--- |
| **`ChatMavRepository`** (:core) | Resolución de Identidad (Nombre/Foto) para la bandeja de entrada. | **Roto** (Lógica vacía) | Un servicio en `:core` que resuelva UID ➔ Datos Básicos. |
| **`UsUsuarioRepository`** (:app) | Sincronización profunda del perfil del cliente. | **Legacy** | Migrado a `SincUsuarioRepositorio`. |
| **`UsPrestadorRepository`** (:app) | Descarga de perfiles de prestadores ajenos. | **Legacy** | Migrado a `SincUsuarioDwnPrestadorRepositorio`. |
| **`ChatListViewModel`** (:app) | Refresco manual de la lista de chats. | **Inactivo** | Disparar sincronización vía los nuevos repositorios de descarga. |
| **`SincGeneralWorker`** (:core) | Sincronización de fondo automática. | **Deprecated** | Redirigir a `SincUsuarioWorker` y `SincPrestadorWorker`. |

## 2. Análisis del Flujo de Datos "Grandes Ligas"

La arquitectura debe dividirse en dos capas soberanas e independientes:

### A. Capa de Infraestructura (Módulo :core)
*   **Propósito**: Servicios que ambas apps consumen por igual.
*   **Servicio Crítico**: **Resolución de Identidad Base**. El Chat necesita saber quién es "UID_123" para mostrar su foto. Esto no es lógica de negocio, es infraestructura.
*   **Solución**: Crear un `SincronizadorIdentidadGral` que solo maneje el mapeo Shallow (Nombre, Foto, Miniatura).

### B. Capa de Negocio (Módulos :app y :prestador)
*   **Propósito**: Gestión profunda de la jerarquía de cada tipo de usuario.
*   **Soberanía**:
    *   `SincUsuarioRepositorio`: Único dueño de las direcciones y empresas del Cliente.
    *   `SincPrestadorRepositorio`: Único dueño de las sucursales, equipo y horarios del Profesional.
*   **Independencia**: Los Workers de fondo (`SincUsuarioWorker` y `SincPrestadorWorker`) aseguran que la nube esté siempre al día sin interferir con el otro módulo.

## 3. ¿Qué necesitan los archivos para seguir funcionando?

1.  **`ChatMavRepository`**: Necesita un inyectable en `:core` que le permita llamar a `descargarUsuarioBasico(uid)` y `descargarPrestadorBasico(uid)`.
2.  **`SincUsuarioRepositorio`**: Necesita consolidar la lógica de "Subida Deep" para que el nuevo `SincUsuarioWorker` la ejecute eficientemente en background.
3.  **`SincPrestadorRepositorio`**: Necesita asegurar que el índice de búsqueda de Firestore se actualice correctamente tras cada sincronización.

## 4. Próximos Pasos para Excelencia

1.  **Saneamiento de DAOs**: Implementar el guardado por bloques (Batch) para maximizar la velocidad de Room.
2.  **Logs Estratégicos**: Inyectar trazabilidad en español en cada punto de entrada y salida de datos (`📤 [INICIO_PUSH]`, `✅ [EXITO_PULL]`).
3.  **Eliminación de Acoplamiento**: Vaciar `MotorSincronizacionMav` pero mantener su "cáscara" vacía momentáneamente para que Hilt no rompa la compilación mientras movemos los cables.

---
**El sistema está listo para dar el salto a la arquitectura descentralizada definitiva.**
