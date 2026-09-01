# 🔍 Auditoría Técnica: Flujo de Inicio y Sincronización (App Naranja)

He analizado el comportamiento del inicio de sesión y la gestión de datos en la App del Prestador. Los errores `Job was cancelled` en el Logcat son la señal de un problema de arquitectura en la gestión del ciclo de vida de las corrutinas de sincronización.

## 📊 Estado Actual de la Fuente de la Verdad (SSOT)

| Componente | Comportamiento Detectado | Calificación |
| :--- | :--- | :--- |
| **Consumo de Room** | El Dashboard consume de `ConsultasPrestadorRepositorio`, que ensambla datos de DAOs. Es correcto. | ✅ **ELITE** |
| **Sincronización PULL** | Se ejecuta en `viewModelScope` de la pantalla de Login y de Inicio por separado. | ❌ **FRÁGIL** |
| **Gestión de Sesión** | Optimista. Navega rápido pero cancela la descarga de datos en segundo plano. | ⚠️ **MEJORABLE** |
| **Background Sync** | Solo existe un `Worker` para PUSH (subida). No hay respaldo para PULL (descarga). | ⚠️ **INCOMPLETO** |

---

## ⚡ Análisis de los Errores detectados (Logcat)

El error `❌ [PULL_SHALLOW_ERROR] Job was cancelled` ocurre porque:
1.  **Login exitoso**: El `PrestadorLoginViewModel` inicia la descarga (`descargarPerfilPrestadorCompleto`).
2.  **Navegación inmediata**: La app cambia de pantalla al Dashboard.
3.  **Destrucción del VM**: Al cambiar de pantalla, el `viewModelScope` del Login se cancela, matando el proceso de descarga a mitad de camino.
4.  **Reintento en Inicio**: El `ArmadorPrestadorViewModel` detecta que Room está vacío y vuelve a intentar la descarga, causando redundancia y posibles estados inconsistentes en la UI.

---

## 🏆 Estándares de "Grandes Ligas" vs. Maverick

| Característica | App de Grandes Ligas (WhatsApp, Outlook) | App Maverick (Actual) |
| :--- | :--- | :--- |
| **Persistencia de Sync** | Las descargas corren en un `ApplicationScope`. Sobreviven al cambio de pantallas. | Atado al ciclo de vida de la pantalla (UI Scope). |
| **Idempotencia** | Si el proceso muere, un `WorkManager` lo retoma donde quedó. | Se reinicia desde cero o falla silenciosamente. |
| **Single Responsibility** | El ViewModel solo pide "Sincroniza", no sabe *cómo* ni *cuándo* termina. | El ViewModel gestiona la lógica de descarga directamente. |

---

## 🛠️ Propuesta de Mejora Elite v2026

Para que la App Naranja sea 100% robusta, propongo los siguientes cambios:

### 1. Inyección de `ApplicationScope`
Crear un scope global en el repositorio de sincronización para que el proceso de "Warm-up" no dependa de si el usuario está en el Login o en el Dashboard.

### 2. Creación de `SincPullWorker`
Implementar un obrero de WorkManager específico para la descarga inicial. Esto garantiza que si el usuario tiene mala conexión, la app siga intentando descargar su perfil Maverick incluso si minimiza la aplicación.

### 3. Semilla Mínima Viable
Asegurar que el proceso de Login solo cree la "identidad root" y delegue la descarga de la jerarquía completa a un motor de sincronización centralizado.

> [!IMPORTANT]
> **Conclusión:** La app está consumiendo los datos correctamente de Room, pero el motor que llena ese Room (los archivos `Sinc`) está fallando por estar mal "atado" a la interfaz de usuario.

**¿Quieres que proceda a centralizar la sincronización en un scope global para eliminar estos errores y asegurar que los datos bajen siempre completos?**
