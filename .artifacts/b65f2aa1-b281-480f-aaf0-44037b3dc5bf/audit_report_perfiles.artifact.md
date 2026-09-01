# Auditoría Completa: Sistema de Perfiles Maverick Elite (v2026)

Esta auditoría detalla la arquitectura, el flujo de datos y la relación entre las aplicaciones del **Cliente** y del **Prestador** con respecto a la visualización y gestión de perfiles, utilizando componentes compartidos en `:ui-shared`.

## 🏛️ 1. Arquitectura de Pantallas (SSOT en `:ui-shared`)

El sistema utiliza un **Patrón de Orquestación Modular**. En lugar de duplicar pantallas, existe un componente maestro en `:ui-shared` que se adapta según quién lo use y qué identidad esté mostrando.

### Componente Maestro (El Corazón)
- **Archivo:** [PrestadorPerfilScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/PrestadorPerfilScreen.kt)
- **Función:** Actúa como el lienzo principal que organiza las "piezas" del perfil (Cabecera, Secciones de Datos, Direcciones, Sucursales, etc.).
- **Modularidad:** Utiliza submódulos en `parts/` como `PrestadorPerfilParteCabecera.kt` o `PrestadorPerfilParteSecciones.kt` para cumplir la **Ley #10 (Rompecabezas)**.

---

## 📱 2. Detalle por Aplicación

### A. App del Cliente (`:app`)
La aplicación del cliente maneja dos contextos de perfil:

1.  **Mi Propio Perfil (Usuario):**
    - **Pantalla:** [PerfilUsuarioScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/PerfilUsuarioScreen.kt)
    - **Uso:** Llama a `PrestadorPerfilScreen` con `esMiPropioPerfil = true`.
    - **Datos:** Consume `UsIdentidadViewModel` que mapea el `CuentaMaestroUsuarioMav` a modelos UI.

2.  **Ver a un Prestador:**
    - **Pantalla:** [PerfilPrestadorScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/PerfilPrestadorScreen.kt)
    - **Uso:** Llama a `PrestadorPerfilScreen` con `esMiPropioPerfil = false`.
    - **Acción Especial:** Habilita el botón de "Chat" para iniciar una conversación con el profesional.

### B. App del Prestador (`:prestador`)
La aplicación del prestador también maneja dos contextos:

1.  **Mi Perfil Profesional:**
    - **Pantalla:** [ProfileScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/profile/PrestadorPerfilScreen.kt)
    - **Uso:** Llama a `PrestadorPerfilScreen` con `esMiPropioPerfil = true`.
    - **Diferenciador:** Aquí es donde se habilitan las funciones de gestión de **Empresas y Sucursales** (Soberanía Profesional).

2.  **Ver a un Cliente:**
    - **Pantalla:** [ClientePerfilScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/client/ClientePerfilScreen.kt)
    - **Uso:** Llama a `PrestadorPerfilScreen` con `esMiPropioPerfil = false`.
    - **Relación Táctica:** El prestador ve una versión simplificada del perfil del cliente (Nombre, Foto, Reputación básica) para saber con quién está tratando.

---

## 🔄 3. Relación y Flujo de Datos (Contrato de Dominio)

La relación entre ambas apps se basa en el **Contrato Único de Visualización** definido en `:core`.

- **`PrestadorUiModel`:** Es el modelo que unifica a todos. Ya sea un Usuario Individual, una Empresa o una Sucursal, todos se "visten" de `PrestadorUiModel` para entrar al lienzo de `:ui-shared`.
- **Soberanía de Datos:**
    - El **Prestador** es el dueño de sus métricas de reputación y estructura empresarial.
    - El **Cliente** es el dueño de su información personal y direcciones de servicio.
- **Interoperabilidad:** Cuando un Cliente busca un rubro, recibe una lista de `PrestadorUiModel`. Al tocar uno, navega a la pantalla compartida de perfil, garantizando una experiencia visual idéntica pero con permisos (flags) diferentes.

---

## 🌍 4. Análisis de "Grandes Ligas" (Benchmarks)

Al observar cómo gigantes como **Uber, LinkedIn y Airbnb** manejan sus perfiles, Maverick Elite ya implementa varias de sus mejores prácticas:

| Característica | Airbnb / LinkedIn | Maverick Elite (Tu Proyecto) |
| :--- | :--- | :--- |
| **Pruebas Sociales** | Reviews y validaciones son centrales. | Implementado vía `reputacion` y `trabajosRealizados` en `PrestadorUiModel`. |
| **Verificación** | Badges de "Usuario Verificado". | Implementado vía `estaVerificado` (visible en cabecera compartida). |
| **Jerarquía Visual** | Sección "Hero" con foto y banner. | Implementado en `PrestadorPerfilParteCabecera.kt`. |
| **Modularidad** | Secciones expandibles (Lazy). | Implementado vía `PrestadorPerfilLienzo.kt` (Patrón Rompecabezas). |
| **Dualidad** | Perfil de "Anfitrión" vs "Huésped". | Implementado mediante el parámetro `esMiPropioPerfil` en el componente compartido. |

---

## 📋 5. Conclusión de la Auditoría

> [!TIP]
> La arquitectura actual es **altamente escalable**. El hecho de que la misma pantalla (`PrestadorPerfilScreen`) sirva para 4 propósitos distintos (Ver mi perfil, ver al otro, en ambas apps) reduce el costo de mantenimiento y asegura la coherencia visual de la marca.

**Puntos Fuertes:**
- **Inmediatez:** Uso de `Flow` y `collectAsStateWithLifecycle` para actualizaciones en tiempo real.
- **SSOT:** Toda la lógica de mapeo vive en los repositorios y modelos del Core.
- **Seguridad:** El componente compartido no conoce la lógica de red; solo recibe "intenciones" (lambdas como `alGuardarCambios`).

**Observaciones de Mejora:**
- El nombre `PrestadorPerfilScreen` podría evolucionar a `IdentidadMavPerfilScreen` para reflejar que también muestra clientes.
- Algunas funciones en la App del Prestador (como `subirFoto`) están marcadas con `TODO` y requieren implementación final en el ViewModel correspondiente.
