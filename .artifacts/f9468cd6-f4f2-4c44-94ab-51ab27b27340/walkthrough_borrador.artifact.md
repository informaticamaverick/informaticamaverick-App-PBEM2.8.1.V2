# Walkthrough: Sistema de Borrador Soberano (Elite v2026)

Se ha implementado una arquitectura de **Borrador Atómico** exclusiva para la App Naranja, garantizando que el perfil del prestador se edite de forma segura en memoria antes de impactar los datos permanentes.

## Cambios Realizados

### 🧠 Gestor de Borrador (`GestorBorradorPerfilMav`)
- **Aislamiento en RAM**: Se creó un componente soberano en el módulo `:prestador` que mantiene una copia del "Ecosistema Maestro".
- **Reactividad Total**: Cualquier cambio en horarios, direcciones o datos personales fluye a través del borrador, notificando a la UI de forma instantánea sin escribir en disco.
- **Diferencial Inteligente**: El sistema compara constantemente el borrador con la base de datos original para detectar si hay cambios pendientes.

### 📡 Sincronización en Bloque (Todo-o-Nada)
- **Commit Atómico**: Se implementó `guardarEcosistemaCompleto` en el Core. Al presionar el icono de la Nube, el sistema limpia y re-siembra toda la jerarquía en Room en una única transacción segura.
- **Background Sync**: Tras el guardado local, se encola un **Worker de Sincronización** que se encarga de subir los cambios a Firebase y actualizar las etiquetas de búsqueda (Topics) incluso si la App se cierra.

### 🛡️ Seguridad de Usuario (Salida Segura)
- **BackHandler Implementado**: Tanto en el Perfil como en la Configuración de Horarios, si el usuario intenta salir con cambios pendientes, se activa el diálogo de seguridad.
- **Opciones Claras**:
    - **GUARDAR Y SALIR**: Realiza el guardado atómico y subida.
    - **DESCARTAR**: Elimina el borrador y vuelve al estado original.
    - **CANCELAR**: Permite seguir editando.

## Verificación de Auditoría

1.  **Icono de Nube**: Ahora el botón de la nube se ilumina en **Cyan Eléctrico** y muestra un punto rojo de notificación en cuanto se realiza el primer cambio en el borrador (ej: añadir un horario).
2.  **Integridad de Datos**: Se validó que al "Descartar", la base de datos de Room permanece intacta, evitando datos parciales o corruptos.
3.  **Idioma**: Todo el flujo interno, funciones y variables han sido estandarizados al **Español (Ley #9)**.

> [!IMPORTANT]
> Esta mejora elimina los errores de sincronización parcial y asegura que el prestador tenga el control total sobre cuándo sus cambios se hacen públicos en la red Maverick.
