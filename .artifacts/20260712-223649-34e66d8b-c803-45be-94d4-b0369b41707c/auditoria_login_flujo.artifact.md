# Auditoría Técnica: Flujo de Login e Identidad (V2026.FINAL)

He realizado un relevamiento del flujo de autenticación comparando la App del Cliente con la del Prestador para asegurar el cumplimiento de las **Leyes Maverick**.

## 📊 1. Comparativa: Cliente vs. Prestador

### App del Prestador (Modelo a Seguir)
*   **ViewModel**: `PrestadorLoginViewModel` gestiona el estado de la UI (`EstadoLoginMav`).
*   **Repositorio**: `PrestadorAuthRepository` encapsula Firebase Auth.
*   **Paso Crítico (Warm-up)**: Utiliza `PrestadorPerfilRepository` para verificar si el perfil existe en la nube y sincronizarlo con Room antes de entrar. Esto garantiza que la app sea **Local-First** desde el segundo uno.

### App del Cliente (Estado Actual)
*   **ViewModel**: `LoginViewModel` gestiona el estado.
*   **Repositorio**: `UsAuthRepository` maneja la lógica de Firebase.
*   **Hallazgo**: Se está realizando un `usuarioRepository.refrescarDesdeNube(uid)` al loguear, lo cual es correcto, pero falta asegurar que la entidad de la **Cuenta Maestra** se cree en Room si no existe, para evitar inconsistencias en el `BeBrainViewModel`.

---

## 🏛️ 2. El Dilema del `BeBrainViewModel`

### Problema Detectado:
El `BeBrainViewModel` está asumiendo tareas de **"Gestor de Arranque"** (Checking Auth, Initial Nav Target).

### Respuesta Arquitectónica (Web Research):
Según las mejores prácticas de **Clean Architecture 2024**, el ViewModel global (Brain) debe ser un **coordinador de estado reactivo**, no un orquestador de procesos de autenticación.
1.  **Fuga de Lógica**: Si el `BeBrain` maneja el login, se vuelve pesado y difícil de testear.
2.  **Solución Profesional**:
    *   **AuthRepository**: Único responsable de hablar con Firebase.
    *   **LoginViewModel**: Responsable de la pantalla de login.
    *   **StartupManager (Opcional)**: Una clase inyectada que verifique el estado inicial al abrir la app.
    *   **BeBrainViewModel**: Solo se suscribe a los cambios de usuario emitidos por el Repositorio para actualizar la identidad global.

---

## 🔄 3. Mejora del Flujo de Datos

Para que la cuenta se guarde correctamente en **Room (SSOT)**:

1.  **Mappers Elite**: Debemos asegurar que el objeto `FirebaseUser` se mapee inmediatamente a `CuentaMavEntity` y se guarde en el `CuentaMavDao`.
2.  **Sincronización Proactiva**: El login exitoso debe disparar un "descarga y guarda" atómico:
    *   `Auth -> Firebase` (Éxito)
    *   `Mapper -> CuentaMavEntity` -> `Room.insert()`
    *   `Repository -> PerfilCloud` -> `Room.upsert()`

---

## 🚀 Recomendaciones de Auditoría

1.  **Desacoplar BeBrain**: Mover `realizarVerificacionAutenticacionInicial` a un componente de inicio dedicado o mantenerlo en el Repositorio de Auth.
2.  **Garantizar Persistencia**: En la App Cliente, al igual que en la del Prestador, el flujo de login debe asegurar el `insert` en Room de la cuenta básica.
3.  **Unificación de Estados**: Usar el mismo sellado de estados (`EstadoLoginMav`) en ambas apps para mantener la coherencia Maverick.

---
> [!IMPORTANT]
> El `BeBrainViewModel` debe ser el **consumidor** de la identidad, no el **creador**. Su trabajo es decir: "Tengo este usuario, ahora ajusto todo el HUD para él".

**Estado de la Auditoría**: Completada. No se han realizado cambios en el código.
