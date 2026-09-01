# 🔍 Auditoría Detallada de Flujos: Identidad y Sincronización (v2026.ELITE)

Esta auditoría técnica desglosa minuciosamente los procesos de inicio, registro y sincronización de las aplicaciones Maverick para asegurar el cumplimiento del **Protocolo de Grandes Ligas**.

---

## 1. Flujo de Arranque e Instalación (Instalación Limpia)

### Escenario: El usuario abre la App por primera vez
- **Paso 1 (Local First)**: Al abrir la app, Hilt inyecta el `RepositorioAutenticacionMav` (en `:core`). Este comprueba `auth.currentUser`.
- **Paso 2 (Semilla Local)**: El `AutenticacionViewModel` (Cliente) o `PrestadorLoginViewModel` (Prestador) recibe la señal de éxito de Google Auth.
- **Paso 3 (Creación de Cuenta Maestra)**: Se inserta un registro en la tabla `cuentas_mav` (Room) con `ultimaSincronizacion = 0`. Esto marca la cuenta como "Nueva".
- **Paso 4 (Atomic Seed Sync)**:
    - Se dispara el `MotorSincronizacionMav`.
    - Se utiliza un **WriteBatch** para subir a Firestore:
        1. Documento en `/cuentas/{uid}` (Configuración base).
        2. Documento en `/clientes/{uid}` o `/proveedores/{uid}` (Perfil humano).
        3. Documento en `/direcciones/{uid}` (Si existe ubicación inicial).
    - **Resultado**: El usuario ya existe en la nube antes de que termine de ver la animación de carga.

---

## 2. Descarga y Persistencia (Ley #2 y #3)

### Escenario: Cuenta existente en un dispositivo nuevo
- **Carga On-Demand**: Al loguearse, el `MotorSincronizacionMav` detecta que el documento en la nube tiene un timestamp mayor al local (o local es 0).
- **Protocolo de "Calentamiento" (Warm-up)**:
    - Se descargan los perfiles en segundo plano (`ObreroSincronizacionMav`).
    - Las imágenes se procesan con `ImageUtils.processImageSource`, priorizando la `miniaturaBase64` para que el Dashboard no se vea vacío mientras se descarga la URL pesada.
- **Jerarquía Sincronizada**: Las sucursales y empresas se descargan automáticamente en la app del prestador, reconstruyendo el árbol de identidades en Room.

---

## 3. Descubrimiento y Comunicación (Ley #4 y #9)

### Búsqueda (Motor de Descubrimiento)
- **Indexación Dual**: El prestador, al guardar su perfil, genera sus "Huellas" (Tags).
    - **Tag ZIP**: `4000_informatica_tecnico` (Búsqueda rápida por zona).
    - **Tag Geohash**: `geo_6e1x7_informatica_tecnico` (Búsqueda por cercanía física).
- **Publicación**: Estos tags se guardan en la colección plana `/indice_busqueda`.
- **Consumo**: La app del cliente usa `BusquedaRemoteMediator` con Paging 3. Al filtrar por categoría, consulta `/indice_busqueda` y descarga en lotes hacia Room.

### Comunicación (Mensajería Elite)
- **Soberanía de ID**: Los mensajes ya no viajan con IDs genéricos. Usan el ID de la identidad activa (sea Humano o Empresa).
- **Tránsito Efímero (Ley #8)**: Al recibir un mensaje multimedia, la app lo guarda en Room y lanza una orden de eliminación en Firebase Storage/Database tras confirmar la lectura local.

---

## 4. Auditoría de Código y Redundancia

### Archivos Congelados (Comentados)
Se han desactivado los siguientes archivos por ser redundantes con el Core:
- `UsAutenticacionRepository.kt` (En favor de `RepositorioAutenticacionMav`).
- `UsIndiceBusquedaRepository.kt` (En favor de `RepositorioIndiceBusquedaMav`).
- `PrestadorAuthRepository.kt` (En favor de `RepositorioAutenticacionMav`).
- `ClimaMavRepository.kt` (En favor de `RepositorioClimaMav`).

### Recomendaciones de Rendimiento
1. **Batching Total**: Actualmente, el perfil de usuario sube direcciones una a una. Se recomienda migrar a un batch único en `UsUsuarioRepository.sincronizarPerfilEnNube`. (Ya incluido en este plan).
2. **WebP forzado**: Asegurar que todas las fotos de perfil se conviertan a WebP en el cliente antes de subir a la nube para ahorrar un ~40% de ancho de banda.

---
**Veredicto Final**: El sistema está preparado para la fase de **Limpieza de Firebase**. El código es ahora determinista y sigue la soberanía del Core.
