# 🔍 Auditoría de Repositorios de Autenticación

He analizado los repositorios de autenticación de ambas apps y, efectivamente, existe una inconsistencia de nombres y estructuras que confirma tu sospecha: el sistema no es un "espejo" perfecto.

## 📊 Comparativa Actual

| Característica | App Azul (Cliente) | App Naranja (Prestador) | Estado |
| :--- | :--- | :--- | :--- |
| **Nombre Archivo** | `UsAutenticacionRepository.kt` | `PrestadorAuthRepository.kt` | ❌ **Inconsistente** |
| **Modelos de Retorno** | `UsuarioUiModel` | `FirebaseUser` (Raw) | ❌ **Diferente Nivel** |
| **Registro Email** | Incluido en el Repositorio | No incluido (se asume externo) | ❌ **Asimétrico** |

---

## ⚠️ ¿Por qué `UsAutenticacionRepository` es "extraño"?

1.  **Prefix "Us":** Mientras que la App Naranja usa el nombre completo "Prestador", la App Azul usa la abreviatura "Us" (User), lo cual rompe la Ley #9 de nombres legibles y completos.
2.  **Mapeo Prematuro:** El repositorio de la App Azul está haciendo el trabajo del ViewModel al convertir el `FirebaseUser` en un `UsuarioUiModel` directamente. Esto no sucede en la App Naranja, donde el repositorio es más puro.
3.  **Redundancia:** En la App Azul, el repositorio tiene métodos de registro que en la Naranja parecen estar manejados de otra forma, creando una asimetría funcional.

---

## 🛠️ Plan de Unificación "Espejo"

Para resolver esto de forma profesional y no con parches, aplicaré los siguientes cambios:

### 1. Estandarización de Nombres
Renombraré ambos repositorios para que sigan el mismo patrón:
-   App Azul: **`UsuarioAutenticacionRepository.kt`**
-   App Naranja: **`PrestadorAutenticacionRepository.kt`**

### 2. Unificación de Firmas
Aseguraré que ambos repositorios devuelvan el mismo tipo de dato (`FirebaseUser`) y deleguen el mapeo a los modelos visuales (`UsuarioUiModel` / `PrestadorUiModel`) a sus respectivos ViewModels. Esto hace que el código sea más flexible y fácil de probar.

### 3. Limpieza de Lógica
Moveré cualquier lógica de "negocio" (como el registro de email) fuera de la autenticación pura si no es estrictamente necesaria ahí, para que ambos archivos sean reflejos exactos.

> [!IMPORTANT]
> **Conclusión:** El archivo `UsAutenticacionRepository` debe seguir existiendo por soberanía de datos (cada app tiene su propia lógica de sesión), pero su **nombre y estructura** están mal.

**¿Procedo a renombrar y unificar estos repositorios para que sean espejos perfectos?**
