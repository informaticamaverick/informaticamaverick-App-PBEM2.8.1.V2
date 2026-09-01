# 🏷️ PROTOCOLO DE NOMENCLATURA MAVERICK (Ley #17)

## Propósito
Este protocolo define el estándar de bautizo de archivos para el ecosistema **Informática Maverick**. Su objetivo es eliminar la ambigüedad, facilitar la navegación y asegurar que la arquitectura sea evidente solo con leer el nombre de un archivo.

## Tabla de Estándares

| Capa | Sufijo / Convención | Ejemplo | Propósito |
| :--- | :--- | :--- | :--- |
| **Entidad BD** | `Entity` | `DireccionEntity` | Estructura pura de Room (la tabla). |
| **Relaciones BD** | `RelacionesBD` | `UsuarioConDireccionesRelacionesBD` | Clases `@Relation` para Room. |
| **DAO** | `Dao` | `DireccionDao` | Interfaz de acceso a datos. |
| **Repositorio** | `Repositorio` | `DireccionRepositorio` | Lógica de sincronización. |
| **Dominio (Modelo)**| `MDominio` | `UsuarioMDominio` | Objeto puro de lógica de negocio. |
| **Dominio (Motor)** | `Motor` | `MotorDescubrimientoMav` | Lógica centralizada de procesos. |
| **Dominio (Filtro)** | `Filtro` | `BusquedaFiltro` | Definición de filtros de datos. |
| **Mappers** | `Mapper` | `DireccionMapper` | Conversor entre capas. |
| **Servicio** | `Servicio` | `NotificadorServicio` | Tareas en background. |
| **ViewModel** | `ViewModel` | `DireccionViewModel` | Orquestador de lógica de UI. |
| **DI (Módulos)** | `ModuloDI` | `CoreModuloDI` | Configuración de Hilt. |
| **Pantalla** | `Screen` | `DireccionScreen` | Interfaz (Jetpack Compose). |

## Reglas de Oro
1. **Idioma**: Todo nombre de archivo debe estar en español (Ley #9).
2. **Atomicidad**: Si un archivo cumple dos roles (ej: mapea y consulta), es un indicador de que debe ser dividido (Ley #13).
3. **Cero Ambigüedad**: Prohibido el uso de sufijos genéricos como `UiModel`, `Utils` o `Helper`.
