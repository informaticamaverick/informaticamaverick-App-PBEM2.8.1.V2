# 🔍 Auditoría de Notificaciones: Informe Maverick

## 📋 Diagnóstico de Fallos

Tras una revisión profunda del código en la App del Cliente, se han detectado las causas exactas del mal funcionamiento del sistema de alertas:

### 1. El "Cerebro sordo" (FCM Incompleto)
*   **Estado Actual**: La aplicación del cliente **no tiene un Servicio FirebaseMessagingService** declarado en el manifiesto ni implementado en el código.
*   **Consecuencia**: Android mata el proceso de la app al cerrarse y no tiene a quién entregarle los mensajes de la nube. Por eso, con la app cerrada, el usuario no se entera de nada.

### 2. Badges Estáticos y Limitados
*   **Estado Actual**: El componente `AppBottomNavigationBar` solo observa el flujo de chat.
*   **Omisión**: Ignora por completo los flujos de `BudgetRepository` (Presupuestos) y `CalendarRepository` (Citas). El usuario debe entrar a cada sección para "adivinar" si hay novedades.

### 3. Falta de Control de Usuario (Gestión de Silencio)
*   **Estado Actual**: No existe una pantalla para que el usuario decida qué secciones silenciar.
*   **Consecuencia**: O recibe todo o no recibe nada (si bloquea los permisos de Android). Falta granularidad.

---

## 🚀 Propuesta de Solución "Grandes Ligas"

Implementaremos una arquitectura **Event-Driven con Persistencia Local Proactiva**:

1.  **FCM Handler Elite**: Un nuevo servicio que, al recibir un presupuesto, primero lo inyecta en Room (Ley #2) y luego muestra la notificación.
2.  **State-In-Sync Badges**: Modificaremos `BeBrainViewModel` para que emita un mapa de estados de notificación consolidado, permitiendo que la barra de navegación brille en cualquier pestaña con novedades.
3.  **Configuración Granular**: Pantalla de ajustes con switches para Chat, Calendario, Presupuestos y Promociones, persistidos vía DataStore para máxima velocidad.
