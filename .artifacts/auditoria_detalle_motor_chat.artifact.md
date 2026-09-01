# Auditoría Detallada: Chat y Motor de Sincronización (v2026.ELITE)

Este reporte detalla las funciones exactas, la lógica compartida y las responsabilidades del repositorio de Chat y el Motor rehabilitado, asegurando la transparencia total del ecosistema.

## 1. ChatMavRepository (El Corazón de la Comunicación)

Ubicado en `:core`, este archivo es el **estándar legal** de mensajería para ambas aplicaciones.

### Funciones Principales:
- **Gestión de Lectura (Room)**:
    - `obtenerConversaciones`: Carga la bandeja de entrada.
    - `obtenerMensajes` / `obtenerFlujoMensajesPaginados`: Carga el historial de forma eficiente (Ley #3).
    - `obtenerSoloImagenes/Ubicaciones/Productos`: Alimenta el "Archivero" (filtros rápidos).
- **Tránsito de Mensajes (RTDB)**:
    - `observarChat`: Activa el "oído" en la nube para recibir mensajes en tiempo real.
    - `enviarMensaje[Texto/Imagen/Audio/etc]`: Implementa la **Ley #2** (Primero guarda en Room como 'Enviando', luego sube a la nube).
- **Señalización Táctica**:
    - `iniciarEscuchaBuzonGlobal`: Detecta cuando alguien nuevo nos escribe (timbre virtual).
    - `enviarMensaje...`: "Toca el timbre" en `inbox_signals` del receptor.

### Lógica Compartida (Unificación de Criterios):
- **Esquema de Datos**: Ambas apps usan la misma estructura en Firebase RTDB y las mismas tablas en Room. Esto elimina fallos de compatibilidad.
- **Ley de Inmediatez**: La lógica de actualizar la bandeja (`sincronizarResumenConversacion`) es idéntica para que el usuario siempre vea el último mensaje.

---

## 2. MotorSincronizacionMav (Infraestructura de Identidad)

Rehabilitado en `:core` como un **Servicio de Apoyo**.

### Funciones Detalladas:
1.  **`descargarIdentidadShallow(uid, tipo)`**:
    - **Qué hace**: Busca el perfil básico (Nombre/Foto) en Firestore y lo inyecta en Room.
    - **Relación con Chat**: El Chat lo llama cuando recibe un mensaje de un UID desconocido. Sin esto, verías "Usuario 123" en lugar de "Juan Pérez".
2.  **`sincronizarCuentaAtomica(uid)`**:
    - **Qué hace**: Sincroniza suscripciones (Elite/Bronce) y roles.
    - **Fuera del Chat**: Esta función sirve para todo el sistema (ej: bloquear funciones premium), pero el Chat la usa para mostrar insignias de verificación.
3.  **`descargarSucursalShallow(...)`**:
    - **Qué hace**: Resuelve datos de un punto de venta físico.

---

## 3. ¿Realizan acciones ajenas al Chat?

| Archivo | Acción Ajena al Chat | Razón de existencia |
| :--- | :--- | :--- |
| **`ChatMavRepository`** | Ninguna. | 100% enfocado en mensajería. |
| **`MotorSincronizacionMav`** | **Sincronización de Cuentas**. | Necesario para que el Chat sepa el estatus del usuario, pero también sirve para el Muro de Pago y la Configuración Global. |

## 📊 Diagnóstico de "Grandes Ligas"

**El problema de confusión**: El nombre "Motor" sugería que hacía "todo" (el negocio pesado de las apps).
**La Solución**: Ahora el Motor está acotado.
- El **Negocio Pesado** (Empresas, Equipos, Direcciones de envío) vive en los repositorios de cada App (`SincUsuarioRepositorio`, `SincPrestadorRepositorio`).
- El **Motor** es solo el "Conserje" que presenta a los usuarios en el Chat y verifica sus cuentas.

---
**Auditoría finalizada. El sistema ahora tiene fronteras claras y una lógica unificada.**
