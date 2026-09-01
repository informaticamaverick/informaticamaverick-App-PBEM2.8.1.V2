# 🔍 Auditoría Técnica: Comparativa Espejo (App Azul vs. App Naranja)

He realizado un análisis profundo de la arquitectura de acceso y sincronización de ambas aplicaciones. Aunque hemos avanzado en la unificación, todavía existen discrepancias de nombres y estructuras que explican por qué la App Azul está fallando en la compilación.

## 📊 Tabla Comparativa de Archivos

| Componente | App Naranja (Modelo Elite) | App Azul (Estado Actual) | Estado Espejo |
| :--- | :--- | :--- | :--- |
| **ViewModel Acceso** | `PrestadorLoginViewModel` | `AutenticacionViewModel` | ❌ **Inconsistente** |
| **Repositorio Sync** | `SincPrestadorRepositorio` | `SincUsuarioRepositorio` | ✅ **Correcto** |
| **Inyección Scope** | `@ApplicationScope` | `@ApplicationScope` | ✅ **Correcto** |
| **Obrero (Worker)** | `SincPrestadorWorker` | `SincUsuarioWorker` | ✅ **Correcto** |
| **Gestor Obrero** | `GestorSincronizacionPrestador` | `GestorSincronizacionUsuario` | ✅ **Correcto** |
| **Arranque** | `GestorArranqueMav` | `GestorArranqueMav` | ✅ **Correcto** |

---

## ⚠️ Hallazgos Críticos (Por qué falla la App Azul)

### 1. "Anarquía" de Nombres en ViewModels
La App Naranja tiene archivos separados para `Login` y `Register`. La App Azul tiene un solo archivo masivo `AutenticacionViewModel`. Esto rompe el espejo de "Sectores Funcionales" (Ley #9).

### 2. Error de Referencia en `SincUsuarioRepositorio`
He detectado que en la App Azul, el repositorio `SincUsuarioRepositorio` tiene una declaración manual de `externalScope` que choca con la inyección de Hilt. Esto genera el error `PROCESSING_ERROR` en KSP porque el compilador no sabe cuál usar.

### 3. Redundancia de Lógica en la App Azul
El `AutenticacionViewModel` de la App Azul todavía tiene importados DAOs y utilidades que ya movimos al Repositorio, creando un "ruido" de dependencias que Hilt no puede resolver.

---

## 🛠️ Plan de Nivelación (Espejo Perfecto)

Para que ambas apps sean idénticas en calidad, propongo:

1.  **Renombrar y Dividir (Azul):** Transformar `AutenticacionViewModel` en `UsLoginViewModel` y `UsRegisterViewModel` (si aplica) para que coincidan con la estructura de la Naranja.
2.  **Limpieza de Repositorio (Azul):** Eliminar la declaración manual de `externalScope` en `SincUsuarioRepositorio` y dejar que Hilt inyecte el del `AppModule`.
3.  **Unificación de Firmas:** Asegurar que `finalizarAccesoMaverick` tenga los mismos parámetros en ambas apps.

> [!IMPORTANT]
> **Conclusión:** La App Naranja ya es el "estándar de oro". La App Azul tiene "cicatrices" de la arquitectura anterior que están bloqueando la compilación.

**¿Deseas que proceda a realizar la limpieza quirúrgica de la App Azul para que sea un espejo exacto de la Naranja y vuelva a compilar?**
