# 💬 Protocolo Maverick Elite: Mensajería Multiperfil (v2026.FINAL)

Este manual define el ruteo, la persistencia y la política de tránsito de mensajes para asegurar el aislamiento de contextos entre perfiles personales y comerciales.

---

## 🏛️ 1. ARQUITECTURA DE AISLAMIENTO (Identidad Mav)

Maverick 2026 elimina la confusión de hilos de chat mediante un **Ruteo de Identidad Simétrico** basado en 4 etiquetas de identidad atómicas.

### A. El Sistema de 4 Tags
Cada mensaje viaja con:
1.  **`idEmisor`**: La identidad activa que habla (ej: Sucursal Centro).
2.  **`idReceptor`**: La identidad activa que recibe.
3.  **`idPropietarioEmisor`**: El UID del humano dueño del emisor.
4.  **`idPropietarioReceptor`**: El UID del humano dueño del receptor.

**Beneficio**: Esto permite que un dueño de empresa supervise los chats de sus sucursales y que las notificaciones push lleguen al dispositivo del humano correcto.

---

## ⚙️ 2. SINCRONIZACIÓN EN TIEMPO REAL (SSOT)

El sistema de chat es **Offline-First**. Room es la única fuente de verdad para la UI.

### Flujo de Recepción (Real-Time)
1.  **Escucha**: El `ChatMavRepository` activa un `ChildEventListener` en Firebase RTDB al abrir un hilo.
2.  **Mapeo**: El `MapeadorMensajesMav` traduce el dato de red al modelo de Room (**Ley #9: Español Total**).
3.  **Impacto**: El mensaje se inyecta en la tabla `mensajes_mav`.
4.  **Reactividad**: La UI, al observar el `Flow` de Room, se actualiza automáticamente en milisegundos (**Ley #4: Inmediatez**).

---

## 🚀 3. POLÍTICA DE TRÁNSITO EFÍMERO (Ley #8)

Para maximizar el rendimiento y garantizar la privacidad, los datos pesados siguen el ciclo de vida **P2P-Hybrid**.

1.  **Tránsito de Media (Imágenes y Audios)**:
    *   **Envío**: El dato viaja por la nube en Base64 o URL temporal.
    *   **Persistencia**: Al recibirse, el dispositivo descarga el archivo físico y lo guarda en el almacenamiento local.
    *   **Limpieza**: Una vez confirmado el guardado en Room, el dato **debe ser eliminado** de Firebase (Cloud Economy).

---

## 🛡️ 4. REGLAS PARA EL DESARROLLADOR (LEY #7)

*   **Trazabilidad Hormiga**: Cada mensaje enviado debe loguearse como `[CHAT_ENVIO_TEXTO]`.
*   **Ahorro de Red**: Antes de pedir un perfil de usuario, verifica siempre en Room si ya existe y es reciente (**Ley #2: Costo Zero**).
*   **Higiene**: Siempre llama a `detenerObservacionChat()` al salir de la pantalla para liberar memoria y datos.

---
**Informática Maverick - División de Comunicaciones (2026)**
