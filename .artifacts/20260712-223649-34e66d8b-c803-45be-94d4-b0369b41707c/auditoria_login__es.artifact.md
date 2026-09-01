# Auditoría Técnica: Saneamiento de Idioma y Flujo de Login (Estándar Mav 2026)

He realizado una re-auditoría profunda del flujo de login en la App del Cliente, integrando la obligatoriedad del idioma español (**Ley #9**) y las mejoras de arquitectura de "Grandes Ligas".

## 📊 1. Hallazgos de Nomenclatura (Violaciones Ley #9)

He detectado que el módulo `:app` (Cliente) tiene gran parte de su lógica de autenticación en inglés, lo cual rompe el protocolo Maverick Elite:

| Componente Actual | Estado de Ley | Propuesta Mav 2026 |
| :--- | :--- | :--- |
| `LoginViewModel` | ❌ Inglés | `AutenticacionViewModel` |
| `LoginUiState` | ❌ Inglés | `EstadoUiAutenticacion` |
| `UsAuthRepository` | ❌ Inglés | `UsAutenticacionRepository` |
| `isLoading` | ❌ Inglés | `estaCargando` |
| `verificarPerfilYNavegar` | ✅ Correcto | (Mantener) |

---

## 🏛️ 2. Rediseño del Flujo de Datos (Soberanía Atómica)

Bajo las nuevas instrucciones, el flujo de login debe ser el siguiente:

1.  **Repositorio Soberano**: `UsAutenticacionRepository` se encarga de Firebase Auth.
2.  **Mapeo Inmediato (Room First)**:
    *   Al recibir el `FirebaseUser`, el sistema **DEBE** generar una `CuentaMavEntity` y guardarla en Room antes de que el usuario vea el Dashboard.
    *   Esto asegura que el `BeBrainViewModel` nunca encuentre un estado nulo al iniciar la app.
3.  **Desacoplamiento del Cerebro**:
    *   Se confirma la necesidad de un `AppStartupManager` (en español: `GestorArranqueMav`).
    *   Este gestor verificará `auth.currentUser` y decidirá si enviar al usuario a `main_screen` o `login_screen`.
    *   El `BeBrainViewModel` quedará libre de lógica de login, actuando solo como **orquestador reactivo** de la identidad ya validada.

---

## 🔄 3. Plan de Saneamiento y Mejora

1.  **Refactor de Archivos**: Renombrar archivos y clases al español siguiendo el estándar del proyecto.
2.  **Inyección de Dependencias**: Actualizar Hilt para reconocer los nuevos nombres de repositorios.
3.  **Persistencia Local Atómica**:
    *   Modificar el flujo de éxito del login para incluir:
        ```kotlin
        val cuenta = CuentaMavEntity(id = firebaseUid, ...)
        cuentaMavDao.insertarOActualizar(cuenta)
        usuarioRepository.refrescarDesdeNube(firebaseUid)
        ```

---

## 🚀 Conclusión de Auditoría

El flujo actual es funcional pero "débil" arquitectónicamente por el uso de idiomas mixtos y la falta de garantía de persistencia en Room durante el primer acceso. El paso a un sistema **100% en Español** y **Local-First** pondrá a la App del Cliente al mismo nivel de robustez que la del Prestador.

**Estado de la Auditoría**: Revisada con leyes de idioma. No se han realizado cambios.
