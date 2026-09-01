# Plan de Implementación - Estabilización de Envío de Presupuestos (v2026.ELITE)

Este plan aborda el cierre forzoso (crash) al enviar presupuestos debido a una excepción no controlada en la verificación del estado Elite del prestador.

## User Review Required

> [!WARNING]
> Se modificará la lógica de validación de membresía en el repositorio. Si la cuenta no se encuentra en la base de datos local (Room) todavía, permitiremos el envío para no bloquear al usuario durante periodos de sincronización inicial, asumiendo que la validación final se realiza en el backend de Firebase.

## Cambios Propuestos

### Repositorio (Módulo :prestador)

#### [MODIFY] [PrestadorPresupuestoRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/datos/repositorios/PrestadorPresupuestoRepository.kt)
- Refactorizar `enviarPresupuesto` para:
    - No lanzar una `IllegalStateException` directamente si la cuenta es nula (permitir flujo si no hay datos locales).
    - Usar un log de advertencia en lugar de un crash.
    - Asegurar que la excepción solo se lance si hay certeza absoluta de que el usuario no tiene permiso.

### ViewModel (Módulo :prestador)

#### [MODIFY] [PresupuestoMavViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/presupuesto/PresupuestoMavViewModel.kt)
- **Control de Errores**: Envolver la llamada `repositorio.enviarPresupuesto(presupuesto)` en un bloque `try-catch`.
- **Estado de UI**: Añadir un `SharedFlow` o `StateFlow` para emitir mensajes de error a la UI en lugar de permitir que la aplicación se cierre.

### UI (Módulo :prestador)

#### [MODIFY] [NuevoPresupuestoSheet.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/chat/componentes/NuevoPresupuestoSheet.kt)
- Observar los errores del ViewModel y mostrarlos mediante un **Toast** o **Snackbar** profesional, manteniendo la hoja abierta para que el usuario pueda corregir o reintentar.

## Plan de Verificación

### Verificación de Estabilidad
- Intentar enviar un presupuesto con una cuenta que no sea Elite en la base de datos local.
- Confirmar que la app **NO** se cierra.
- Verificar que aparezca un mensaje informativo: "Esta acción requiere membresía Elite".

### Verificación de Integridad
- Asegurar que el presupuesto se guarde localmente en Room incluso si el envío a la nube falla por permisos.
