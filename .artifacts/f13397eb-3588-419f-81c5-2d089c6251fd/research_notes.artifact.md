# Auditoría de Inicio de App y Flujo de Mensajería

## Hallazgos en Logcat
1. **Burst de Notificaciones**: Al iniciar, `ChatMavRepository` activa listeners de RTDB para todos los hilos. RTDB dispara `onChildAdded` para los últimos 50 mensajes existentes. El repositorio dispara una notificación local por cada uno de ellos, provocando que el sistema Android bloquee las notificaciones por "ruidosas" (`NotifAttentionHelper: Muting recently noisy`).
2. **Jank de Inicio**: El procesamiento masivo de mensajes viejos al arrancar genera micro-stutters (`Davey! duration=940ms`).
3. **Errores de gRPC/Firestore**: Fallos de resolución de nombre en background, posiblemente por falta de conectividad estable al pausar la app.

## Problemas de Arquitectura Detectados
- **Notificaciones Locales vs Push**: La app está usando el listener de RTDB como disparador de notificaciones incluso para mensajes históricos cargados al inicio.
- **Ley #8 (Tránsito Efímero)**: Los mensajes permanecen en RTDB hasta que se marcan como leídos. Si hay muchos acumulados, el "arranque en frío" es pesado.

## Plan de Mejora - Mensajería
1. **Guardia de Tiempo**: Solo disparar notificaciones para mensajes cuya fecha de servidor sea posterior a la hora de inicio de la sesión actual de la app (con un margen pequeño).
2. **Detección de Duplicados**: No notificar si el mensaje ya existe en Room.
3. **Optimización de Carga**: Diferir el `observarChat` masivo unos segundos después del inicio para dar prioridad al renderizado de la UI.

## Plan de Mejora - Promociones (Cascada Instagram)
1. **Suscripción Proactiva**: El `CoordinadorAccionesMav` ya fue actualizado, pero falta asegurar que al buscar, se limpie y re-suscriba correctamente si cambia la zona.
2. **Feed Inteligente**: Refinar `getStoriesCascada` para que, en lugar de "o uno o lo otro", pueda combinar niveles o asegurar una transición suave si el primer nivel es pobre en contenido.
