# Auditoría de Sincronización Descentralizada (v2026.ELITE)

He analizado el flujo de sincronización respetando la independencia de la App Azul (`:app`) y la App Naranja (`:prestador`). Se han detectado problemas de desconexión entre el proceso de fondo y los repositorios específicos.

## 1. Hallazgos en la Arquitectura Descentralizada

### A. Desconexión del Background Sync (Worker)
> [!CAUTION]
> **Jerarquía de Dependencias**: El `SincronizacionWorkerMav` vive en `:core` y está intentando ejecutar la lógica de sincronización mediante `MotorSincronizacionMav`. Sin embargo, la "lógica viva" reside ahora en los repositorios especializados de cada app.
>
> **Problema**: Como `:core` no tiene acceso a las clases de `:app` o `:prestador`, el Worker de fondo no puede disparar la sincronización jerárquica actualizada de forma directa.

### B. Rendimiento Local (Inserciones en Room)
Se ha confirmado que la descarga de datos (PULL) sigue siendo ineficiente en ambos repositorios. Están iterando sobre listas de documentos de Firebase e insertando uno por uno:
- **App Azul**: `SincUsuarioRepositorio.kt` -> Bucles `forEach` para Direcciones y Sucursales.
- **App Naranja**: `SincPrestadorRepositorio.kt` -> Bucles `forEach` en la restauración profunda.

## 2. Puntos de Mejora Detectados

| Componente | Problema | Impacto |
| :--- | :--- | :--- |
| **Worker (Core)** | Lógica vacía/comentada. | Los cambios offline no se suben a la nube automáticamente. |
| **SincRepos (Apps)** | Inserciones atómicas item por item. | Cuellos de botella y posibles bloqueos de UI durante la carga de perfiles grandes. |
| **Logcat** | Ausencia de logs en el flujo de descarga (PULL). | Dificultad para auditar fallos de sincronización parcial. |

## 3. Recomendaciones Técnicas (Independientes)

1.  **Reparar Workers por App**: Crear un `UserSyncWorker` en `:app` y un `ProviderSyncWorker` en `:prestador`. Esto permite que cada Worker consuma su repositorio específico sin violar la jerarquía de módulos.
2.  **DAOs con Inserción Masiva**: Actualizar los DAOs en `:core` para soportar `insertarLista(...)` y usarlos en los repositorios de cada app.
3.  **Logs de Auditoría**: Inyectar trazabilidad en los métodos `descargarPerfil...` de ambas apps.

---
**Auditoría finalizada el 26 de Julio de 2026.**
