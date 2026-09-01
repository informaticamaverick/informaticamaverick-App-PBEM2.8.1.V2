# Auditoría Completa: Módulo de Locación y Geolocalización (v2026.ELITE)

Esta auditoría detalla el estado actual del sistema de ubicaciones, direcciones y GPS a través de todos los módulos del proyecto (`:app`, `:prestador`, `:core`, `:ui-shared`).

## 1. Arquitectura de Datos y Modelos

El sistema ha migrado (o está en proceso de migrar) hacia un estándar unificado en español, reemplazando modelos legacy como `AddressUnico`.

### Componentes Core
- **Modelo de Dominio**: [DireccionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/domain/model/DireccionMav.kt) - SSOT (Single Source of Truth) para direcciones físicas.
- **Entidad de Persistencia**: [DireccionMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/DireccionMavEntity.kt) - Almacenada en la tabla `direcciones_mav`.
- **Utilidades Geográficas**: [GeoUtils.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/utils/GeoUtils.kt) - Centraliza Geohash (Base32), Haversine (distancia), y Geocoding (Geocoder de Android).

## 2. Hallazgos Críticos: Inconsistencias Detectadas

> [!WARNING]
> **Redundancia de Datos y Espejamiento Incompleto**
> Se detectó que las entidades principales (`IdentidadPrestadorMavEntity` y `SucursalMavEntity`) mantienen campos "espejo" para búsqueda rápida, pero estos no se sincronizan correctamente en todos los flujos.

### A. Fallo de Sincronización en Creación de Sucursales
En los componentes de registro de empresa y sucursal, solo se transfiere el `codigoPostal` a la entidad de la sucursal, ignorando las coordenadas y el geohash.
- **Archivo Afectado**: [PrestadorPerfilParteHojas.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/parts/PrestadorPerfilParteHojas.kt)
- **Impacto**: Las nuevas sucursales no aparecerán en las búsquedas por radio/proximidad hasta que se fuerce una actualización manual, ya que `latitud` y `longitud` quedan en `0.0`.

### B. Desconexión en los Gestores de Borrador
Los gestores de borrador (`BorradorPerfilPrestadorGestor` y `BorradorPerfilUsuarioGestor`) actualizan la lista de direcciones físicas, pero no propagan los cambios de coordenadas de vuelta a la identidad "dueña".
- **Impacto**: Si un prestador cambia su dirección principal, su "punto" en el mapa de búsqueda (que lee de `prestadores_mav`) no se moverá hasta la próxima sincronización masiva o recarga de perfil.

### C. Campos Redundantes en Base de Datos
Existe una duplicidad deliberada para la [LEY #4] (Inmediatez), pero carece de un disparador (Trigger) o lógica de DAO que garantice la integridad.
- **Tabla `prestadores_mav`**: Tiene `latitud`, `longitud`, `codigoPostal`, `geohash`.
- **Tabla `sucursales_mav`**: Tiene `latitud`, `longitud`, `codigoPostal`, `geohash`.
- **Tabla `direcciones_mav`**: Tiene los mismos campos + calle, número, etc.

## 3. Infraestructura de GPS y API

### Detección de Ubicación
- **Gestor Centralizado**: [GestorUbicacionMav.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/GestorUbicacionMav.kt) - Utiliza `FusedLocationProviderClient` con prioridad de alta precisión.
- **Validación SUV (Sistema de Ubicación Verificada)**: `GeoUtils.verificarUbicacionGps` compara la ubicación real con la declarada (margen de 100m).

### Normalización Externa (Georef)
- Se utiliza la API de Georeferenciación de Argentina.
- **Limitación**: El repositorio [GeorefMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/GeorefMavRepository.kt) tiene métodos marcados como "Implementación atómica de búsqueda" que devuelven listas vacías.

## 4. Auditoría de Archivos por Módulo

| Módulo | Archivos Clave | Función | Estado |
| :--- | :--- | :--- | :--- |
| **:core** | `DireccionMavEntity`, `GeoUtils`, `GestorUbicacionMav` | Infraestructura y Datos | **Sólido** |
| **:ui-shared** | `FormularioDireccionMav`, `HojaEditorDireccionMav` | UI de Captura y Edición | **Sólido** |
| **:prestador** | `ArmadorPrestadorViewModel`, `BorradorPerfilPrestadorGestor` | Gestión de Negocio | **Inconsistente** (Espejamiento fallido) |
| **:app** | `ArmadorUsuarioViewModel`, `BorradorPerfilUsuarioGestor` | Gestión de Cliente | **Inconsistente** (Espejamiento fallido) |

## 5. Recomendaciones de Mejora

1.  **Unificar el Guardado**: Modificar los Gestores de Borrador para que `actualizarDireccion` también actualice los campos espejo en la `Identidad` o `Sucursal` correspondiente.
2.  **Completar `GeorefMavRepository`**: Implementar la búsqueda de localidades para mejorar el auto-completado en el [FormularioDireccionMav](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/parts/FormularioDireccionMav.kt).
3.  **Limpieza de Legacy**: Eliminar definitivamente cualquier referencia a `AddressUnico` (si queda alguna activa) para evitar confusiones de tipos de archivos.
4.  **Validación de Geohash**: Asegurar que cada guardado de dirección ejecute `GeoUtils.computeGeohash` para no perder la capacidad de búsqueda espacial.

---
**Auditoría finalizada el 26 de Julio de 2026.**
