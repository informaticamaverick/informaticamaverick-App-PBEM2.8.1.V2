# Auditoría de Perfiles y Relevamiento UI-Shared (V2026)

Esta auditoría analiza la implementación de los perfiles de Usuario y Prestador bajo el protocolo de compartición de componentes entre la App del Cliente y la App del Prestador.

## 📊 Estado Actual: "Funcional pero Fragmentado"

El proyecto ya cuenta con una base sólida en `ui-shared`, pero existe una "fuga de lógica" en el mapeo de datos que ocurre de forma independiente en ambas aplicaciones.

### Componente Central: `PrestadorPerfilScreen`
Ubicación: `ui-shared/src/main/java/.../profile/PrestadorPerfilScreen.kt`
- **Fortaleza**: Es un orquestador potente que implementa la **Ley #10 (Rompecabezas)**.
- **Capacidad**: Soporta el sistema de **Multi-Identidad** (Personal, Empresas y Sucursales) mediante un `HorizontalPager`.
- **Diseño**: Sigue estrictamente la estética **Maverick Elite / ROG Dark**.

---

## 🏛️ 1. App del Cliente (Usuario)
**Pantalla**: [PerfilUsuarioScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/PerfilUsuarioScreen.kt)

### Funcionamiento:
- Consume `PrestadorPerfilScreen` de `ui-shared`.
- **Problema de Mapeo**: Realiza una conversión manual local (`toPrestadorUiModel`) de `UsuarioUiModel` a `PrestadorUiModel`. Esto duplica lógica que debería ser universal.
- **Contexto**: Se identifica correctamente como "Mi Propio Perfil" para habilitar edición.

---

## 🏗️ 2. App del Prestador
**Pantalla**: [PrestadorPerfilScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/profile/PrestadorPerfilScreen.kt)

### Funcionamiento:
- También consume el componente de `ui-shared`.
- **Complejidad**: Maneja la jerarquía completa del `CuentaMaestroPrestadorMav` (Prestador -> Empresas -> Sucursales).
- **Redundancia**: Al igual que la App Cliente, tiene bloques de `remember` masivos para mapear identidades hijas.

---

## 🔄 3. Flujo de Datos y SSOT (Pilar Core)

El flujo actual es:
`Room (Entity)` -> `Repository` -> `Maestro (Domain Model)` -> **`Mapeo Manual (UI)`** -> `PrestadorUiModel (Shared)`

### Riesgo Detectado:
Si mañana decides añadir un campo nuevo (ej: "Nivel de Seguridad"), tendrías que actualizar el mapeo en **dos aplicaciones distintas**, rompiendo la Ley #9 del Núcleo Atómico.

---

## 🚀 Plan de Unificación "Grandes Ligas"

Para que la app funcione como las mejores del mercado, propongo centralizar el mapeo en el **Core** o en un **Mapper compartido**:

1.  **Mapeador Maestro Unificado**: Crear una función de extensión en el Core que transforme `CuentaMaestroUsuarioMav` y `CuentaMaestroPrestadorMav` directamente a la lista de `PrestadorUiModel` que necesita la pantalla.
2.  **Soberanía de Identidad**: Asegurar que el `HorizontalPager` del perfil sea el mismo para un Cliente viendo a un Prestador, que para el Prestador viendo su propio perfil (Paridad Total).
3.  **Inyección de Acciones**: Las acciones (Guardar, Cambiar Foto) se pasan como lambdas, manteniendo la UI de `ui-shared` totalmente **Stateless** (Pantallas Tontas - Ley #1).

---
> [!IMPORTANT]
> La estructura de perfiles es la más compleja del sistema porque une los 5 Pilares. Centralizar el mapeo ahora ahorrará cientos de horas de mantenimiento y evitará errores de visualización entre apps.
