# 🏛️ Auditoría y Plan: Unificación del Ecosistema de Acceso (Elite v2026)

Tras revisar los archivos de Login, Registro, Arranque y Sincronización, he detectado una fragmentación de lógica que compromete la integridad de los datos. El error detectado en el Logcat (`Job was cancelled`) es solo el síntoma; la causa es la falta de un **Gestor de Ecosistema Centralizado**.

## 📊 Hallazgos de Redundancia y Fragmentación

| Funcionalidad | Ubicación Actual (Fragmentada) | Problema Detectado |
| :--- | :--- | :--- |
| **Procesamiento de Imagen** | `LoginVM` y `RegisterVM` | Lógica de compresión y WebP duplicada. |
| **Creación de Identidad** | `LoginVM`, `RegisterVM` y `SincRepo` | Distintos criterios para crear el primer registro en Room. |
| **Control de Navegación** | `GestorArranque` y `LoginVM` | Doble verificación de ruteo que causa parpadeos en UI. |
| **Ciclo de Vida de Sinc** | `viewModelScope` | Las descargas pesadas mueren al cambiar de pantalla. |

---

## 🏗️ Propuesta de Unificación: "El Pilar de Soberanía"

Propongo centralizar toda la construcción del perfil Maverick en un nuevo componente de arquitectura.

### 1. `EcosistemaPrestadorManager` (Nuevo Repositorio Central)
Este componente será el único responsable de:
-   **Seed Inteligente**: Crear la identidad mínima en Room tras el Auth.
-   **Image Engine**: Centralizar `ImageUtils` para procesar fotos de Google o Cámara en un solo flujo.
-   **Download/Upload Deep**: Ejecutar las sincronizaciones pesadas usando un `ApplicationScope` (inyectado vía Hilt) para que nunca se cancelen.

### 2. Unificación de ViewModels
-   **`PrestadorAuthViewModel`**: Fusión de la lógica de acceso. Se encargará solo de capturar credenciales y llamar al `EcosistemaPrestadorManager`.
-   **Limpieza de `SincPrestadorRepositorio`**: Mover las corrutinas al `ApplicationScope`.

---

## 🛠️ Plan de Acción Atómico

### Fase 1: Infraestructura de Fondo
- [ ] Crear `EcosistemaPrestadorManager.kt` para centralizar la construcción de entidades.
- [ ] Inyectar `@ApplicationScope` en los repositorios de sincronización.

### Fase 2: Unificación de Lógica
- [ ] Mover el procesamiento de imágenes de los ViewModels al Manager.
- [ ] Estandarizar la "Semilla de Identidad" (Seed) para que sea idéntica en Login y Google Sign-in.

### Fase 3: Robustez de Sincronización (Respaldo)
- [ ] Crear `SincPullWorker` para respaldar descargas fallidas por mala conexión.
- [ ] Configurar `WorkManager` para reintentos automáticos de descarga de jerarquía.

> [!IMPORTANT]
> **Beneficio Directo:** Eliminamos los errores `Job was cancelled`, aceleramos el tiempo de respuesta del Login (navegación instantánea) y garantizamos que el Room nunca quede a medias.

**¿Deseas que proceda con esta unificación estructural antes de seguir "parchando" errores individuales?**
