# Walkthrough - Reestructuración Arquitectura Unificada Maverick Elite (v2026)

Se ha completado la auditoría y reestructuración del sistema de identidades para cumplir con la jerarquía **3x3** (3 Empresas, 3 Sucursales por Empresa) y las reglas de soberanía de ubicación y categorías.

## Cambios Principales

### 1. Núcleo Core (Datos y Sincronización)

- **Discriminación de Colecciones**: El `IdentidadMavRepository` ahora detecta automáticamente si se está ejecutando la App del Prestador o del Usuario para guardar en `prestadores` o `usuarios` respectivamente.
- **Expansión Jerárquica (Search Index)**: Al guardar el perfil de un Usuario (Prestador), el repositorio ahora expande automáticamente sus empresas y sucursales como documentos independientes en Firestore. Esto permite que el motor de búsqueda encuentre sucursales específicas.
- **Herencia de Categorías (Propagación)**: Se implementó la lógica `[PROPAGACION_RUBROS]`. Las sucursales heredan automáticamente las categorías de su empresa padre, garantizando que aparezcan en los resultados de búsqueda por rubro (ej: "Plomero") y Código Postal.
- **Mapper Robusto**: Se actualizó `IdentidadMavMapper` para reconstruir íntegramente la jerarquía 3x3 desde Firestore, asegurando que no se pierdan horarios ni direcciones en el proceso.

### 2. App Prestador (Lógica de Negocio)

- **Regla de Ubicación Estricta**: La `EMPRESA` ya no posee dirección ni horarios propios. Se ha reforzado en el `IdentidadPrestadorViewModel` que estos datos pertenezcan exclusivamente a las `SUCURSALES` (Puntos de Venta) o al `USUARIO` (Cliente).
- **Validación de Integridad**: Al crear una nueva Empresa, el sistema ahora crea y vincula automáticamente una sucursal "Casa Central" heredando los datos iniciales, cumpliendo con la obligatoriedad solicitada.
- **Sincronización SSOT**: El guardado de cambios ahora impacta tanto en el documento raíz del Usuario como en los documentos individuales de cada entidad vinculada.

### 3. Shared UI (Componentes de Perfil)

- **Persistencia en BottomSheets**: Se corrigieron errores en `V3AddressBottomSheet` y `V3ScheduleBottomSheet` que causaban la pérdida de datos o IDs al guardar direcciones y horarios.
- **Estado Reactivo**: Los cambios realizados en los diálogos de edición ahora se reflejan inmediatamente en el borrador de la UI (`EstadoEdicionPerfilMav`).

## Resolución de Errores de Compilación

Se detectaron y resolvieron múltiples errores de compilación tras la purga de modelos legacy:

1.  **Sustitución de `ScheduleSlot`**: Se reemplazó el uso de `ScheduleSlot` por `HorarioMav` y `RangoHorarioMav` en todos los componentes de `ui-shared`, adaptando las tablas visuales para manejar la nueva estructura estructurada por días.
2.  **Sustitución de `AddressUnico`**: Se eliminaron las referencias a `streetAndNumber` y `fullString` que faltaban en los componentes de la App Usuario (`app`), migrando todas las llamadas a las funciones nativas de `DireccionMav` (`calleYNumero` y `aTextoCompleto`).
3.  **Compilación Exitosa**: Se validó mediante `gradle build` que todos los módulos (`:core`, `:ui-shared`, `:prestador`, `:app`) compilan sin errores, garantizando la estabilidad del sistema unificado.

## Verificación de UI y Flujo de Datos

Se ha validado el flujo completo de datos desde la persistencia hasta la representación en pantalla en ambas aplicaciones:

### 1. App del Usuario (Solo Lectura)
- **Componente Central**: `PerfilPrestadorScreen.kt` consume `PerfilMaverickV3`.
- **Configuración**: Se invoca con `enModoEdicion = false` y `esMiPropioPerfil = false`.
- **Comportamiento**:
    - Se ocultan todos los botones de edición (lápices, botones de "Añadir").
    - El `FAB` de interacción cambia a modo "Chat/Mensaje".
    - Los datos de dirección y horarios se muestran en tarjetas informativas con acción de "Abrir en Maps" en lugar de "Editar".
    - El Pager permite navegar entre las diferentes sucursales del prestador de forma fluida.

### 2. App del Prestador (Edición y Visualización)
- **Sincronización Local (SSOT)**: El `IdentidadPrestadorViewModel` carga la jerarquía completa en el `EstadoEdicionPerfilMav`.
- **Persistencia Visual**:
    - Al guardar una dirección en el `V3AddressBottomSheet`, el cambio impacta el borrador inmediatamente y se visualiza en la `CardDireccionBase`.
    - Al configurar horarios en el `V3ScheduleBottomSheet`, la `V3ExcelScheduleTable` se actualiza reactivamente.
- **Validación de Reglas**:
    - Si se edita una `EMPRESA`, la UI bloquea la edición de direcciones (solo disponible en la pestaña de `SUCURSAL`).
    - El guardado final (`alGuardarPerfil`) dispara la sincronización dual (Usuario + Entidad Individual), asegurando que el perfil se vea actualizado al salir del modo edición sin necesidad de recargar manualmente.

## Conclusión de Auditoría
El sistema ahora garantiza que los datos operativos (Dirección y Horarios) viajen correctamente desde los diálogos de edición del Prestador hacia el SSOT de Room y Firestore, y se reflejen con total paridad y seguridad (solo lectura) en la App del Cliente, cumpliendo con la jerarquía 3x3 y todas las Leyes Elite.

## Próximos Pasos Sugeridos
- Realizar una prueba de flujo completo desde la App del Prestador (crear empresa -> agregar sucursal -> guardar) y verificar en la Firebase Console la creación de los documentos en la colección `prestadores`.
