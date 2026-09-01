# Auditoría Elite: Flujo de Logueo y Persistencia Soberana (V2026.FINAL)

He realizado una auditoría exhaustiva del flujo de autenticación, comparándolo con los estándares de las **"Grandes Ligas"** (apps con millones de usuarios que priorizan la velocidad y el ahorro de datos).

## 📊 1. Relevamiento de Funcionamiento Paso a Paso

El sistema ahora opera bajo un esquema de **Soberanía Local (SSOT en Room)**, asegurando que Firebase sea solo un sistema de transporte y respaldo.

### FASE A: El Primer Inicio (Cold Start / Discovery)
1.  **Sembrado Táctico**: Al abrir la app, el `AppStartupManager` verifica la base de datos. Si está vacía, realiza el **Seed de Categorías** (500+ rubros) en Room. Esto permite que el buscador funcione incluso sin internet desde el primer minuto.
2.  **Decisión de Entrada**: El `GestorArranqueMav` toma el control. Verifica `FirebaseAuth`.
3.  **Autenticación Google/Email**: El usuario ingresa sus credenciales.
4.  **Paso Atómico (Room First)**:
    *   `AutenticacionViewModel` recibe el éxito de Firebase.
    *   **Antes de navegar**, guarda la `CuentaMavEntity` en Room. Esto es vital: si la app se cierra justo aquí, el login ya es persistente localmente.
5.  **Sincronización de Fondo**: Se dispara el `SyncWorker`. Mientras el usuario ve el Dashboard, el sistema descarga su perfil completo, direcciones y preferencias desde Firestore en segundo plano.

### FASE B: Inicios Posteriores (Inmediatez Maverick)
1.  **Costo Zero de Red**: El `GestorArranqueMav` encuentra la cuenta en **Room**.
2.  **Acceso Instantáneo**: La app navega a `main_screen` **sin consultar a Firebase**. No hay pantallas de carga de red (Ley #4).
3.  **Refresco Silencioso**: Solo si hay internet, el `SyncWorker` busca cambios en el perfil para mantener Room actualizado.

---

## 🏛️ 2. Análisis de "Grandes Ligas" vs Maverick

| Característica | Apps Comunes | Apps Grandes Ligas | Maverick Elite (Tu App) |
| :--- | :--- | :--- | :--- |
| **Fuente de Verdad** | Firebase (Remoto) | Local DB (Offline-First) | **Room (SSOT)** |
| **Inicio de Sesión** | Bloqueante (Carga red) | Optimista (Carga local) | **Optimista (Ley #4)** |
| **Uso de Datos** | Alto (Fetch constante) | Bajo (Sincronización deltas) | **Mínimo (Ley #2)** |
| **Logueo de Google** | Cada vez que expira token | Persistente en DB local | **Persistente (Ley #5)** |

---

## 🛠️ 3. Hallazgos de Código y Redundancia

### Código Duplicado Detectado:
- **`BeBrainViewModel`**: Aún conserva funciones como `realizarVerificacionAutenticacionInicial` que ahora son responsabilidad del `GestorArranqueMav`.
- **Mappers**: Existe lógica de mapeo de `FirebaseUser` a `UsuarioUiModel` en el repositorio, pero no se está usando la función de extensión unificada del Core para asegurar la paridad de datos.

### Posibilidades de Mejora:
1.  **Unificación de Repositorio**: El `UsAutenticacionRepository` debería ser el único puente con Firebase Auth, eliminando llamadas directas a `FirebaseAuth` desde los ViewModels.
2.  **Saneamiento de `BeBrain`**: El cerebro debe quedar "limpio" de lógica de login. Su único trabajo tras el refactor será observar el `accountState` que emite Room.

---
> [!TIP]
> El sistema actual es muy profesional porque trata al login no como un "evento de red", sino como un **"cambio de estado en la base de datos local"**. Esto garantiza que la app se sienta instantánea y premium.

**Estado de la Auditoría**: Finalizada con enfoque en leyes Maverick y rendimiento industrial.
