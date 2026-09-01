# Walkthrough: Paridad Jerárquica del Cliente (v2026.ELITE)

Se ha implementado la estructura de datos jerárquica completa para la App del Usuario (Cliente), asegurando que su ecosistema de empresas y sucursales se sincronice con Firebase con la misma robustez que la del Prestador.

## Cambios Clave Realizados

### 1. Sincronización de Árbol Completo
- **[UsUsuarioRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/data/repository/UsUsuarioRepository.kt)**:
    - Se refactorizó `sincronizarPerfilEnNube` para utilizar un **WriteBatch** atómico que sube:
        - Perfil de cliente.
        - Direcciones personales (subcolección).
        - Empresas vinculadas (subcolección).
        - Sucursales y sus respectivas direcciones (sub-subcolecciones).
    - Esto garantiza que un usuario corporativo (ej: compras de una empresa) no pierda su configuración al cambiar de dispositivo.

### 2. Refresco Recursivo (Warm-up)
- Se actualizó `refrescarDesdeNube` para realizar una descarga profunda y recursiva de todo el ecosistema del cliente. El sistema ahora reconstruye el árbol de Room automáticamente al iniciar sesión.

### 3. Trazabilidad y Logs
- Se añadieron migas de pan estandarizadas (`☁️ [SYNC_DEEP]`, `✅ [SYNC_JERARQUICO_OK]`) para facilitar el soporte técnico y la auditoría de datos en tiempo real.

## Verificación de Estructura en Firebase

> [!TIP]
> **Ruta Maestra**: Los datos del cliente ahora siguen este patrón jerárquico:
> `/clientes/{uid}/empresas/{id}/sucursales/{id}/direcciones/{id}`

> [!IMPORTANT]
> **Consistencia Total**: Al haber unificado el comportamiento, ambas aplicaciones hablan el mismo idioma técnico. Un usuario puede ser Cliente o Prestador y sus datos siempre estarán organizados bajo su ID raíz.

## Resultados
1.  **Cero Pérdida de Datos**: Las empresas y sucursales del cliente ahora son persistentes en la nube.
2.  **Seguridad por Herencia**: Al estar dentro del documento del cliente, Firebase puede aplicar reglas de "si eres el dueño del padre, eres dueño de los hijos" automáticamente.
3.  **Rendimiento Atómico**: Un solo viaje de red para sincronizar todo el perfil corporativo del usuario.
