# Walkthrough: Interfaz Jerárquica y Soberanía del Cliente (v2026.ELITE)

Se ha completado la transformación de la interfaz de usuario de la App del Cliente, permitiendo una gestión corporativa completa mediante un carrusel de identidades, mientras se mantiene una estricta higiene visual que oculta métricas comerciales irrelevantes para un usuario final.

## Cambios Clave Realizados

### 1. Evolución del Modelo de Identidad (Core)
- **[PrestadorUiModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/domain/model/PrestadorUiModel.kt)**: Se añadió el flag `esPerfilComercial`. Este es el interruptor maestro que decide si se muestran estrellas de rating, métricas de trabajos o insignias profesionales.
- **[UsuarioMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/remote/UsuarioMapper.kt)**: Se implementó `deEntidadAPrestadorUi`, un traductor táctico que crea una versión "Identidad" del modelo, ideal para el carrusel de perfiles.

### 2. Carrusel de Perfiles para el Cliente (UI Shared)
- **[UsuarioPerfilParteLienzo.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/components/profile/parts/UsuarioPerfilParteLienzo.kt)**:
    - Se reemplazó la lista estática por un **HorizontalPager**.
    - El cliente ahora puede deslizar entre su "Perfil Personal" y sus "Empresas/Sucursales de Gestión".
- **Higiene Visual (Ley #10)**: La pieza `SeccionPerfilMaestroMav` ahora detecta perfiles no comerciales y oculta automáticamente las tarjetas de capacidades, horarios y reputación, dejando una interfaz limpia enfocada en **Identidad y Ubicación**.

### 3. Gestión Corporativa en la App Azul
- **[UsIdentidadViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/profile/UsIdentidadViewModel.kt)**:
    - Se activaron las funciones `crearEmpresa`, `añadirSucursal` y `eliminarEmpresa`.
    - El cliente ahora tiene soberanía total sobre sus múltiples identidades corporativas de facturación/entrega.

## Verificación de Experiencia

> [!TIP]
> **Carrusel Fluido**: Al abrir el perfil en la app del usuario, ahora verás un indicador de páginas si tienes empresas asociadas. Desliza para cambiar de contexto.

> [!IMPORTANT]
> **Privacidad Profesional**: Aunque la estructura es idéntica a la del prestador, un cliente **NUNCA** verá estrellas de rating ni métricas de trabajo en sus perfiles de empresa. La UI se adapta dinámicamente para ser puramente informativa.

## Resultados
1.  **Cero Duplicidad**: Usamos el mismo motor visual para ambas apps, ahorrando miles de líneas de código.
2.  **Soberanía Total**: El usuario gestiona su complejidad corporativa de forma intuitiva.
3.  **Higiene Estricta**: Los datos comerciales solo aparecen donde hay un profesional vendiendo servicios.
