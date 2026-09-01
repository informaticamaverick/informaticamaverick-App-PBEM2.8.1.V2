# 🛡️ Informe de Auditoría de Licitaciones (v13.0)

## 📊 Estado Actual del Flujo
El sistema de licitaciones opera bajo una arquitectura **Topic-Based (FCM)** segmentada por Código Postal y Categoría.

### 1. Auditoría de Identidad (Client Thumbnails)
**Hallazgo**: En la captura, las licitaciones "ejempko" y "rggh" no muestran avatar.
- **Causa**: Al crear la licitación (`PresupuestoViewModel.kt`), se usa `activeProfilePhoto`. Si el usuario no tiene foto de perfil o la sesión de Firebase Storage falló durante el upload, el campo queda nulo en Firestore.
- **Acción Realizada**: Se ha reforzado la UI con un icono de mazo (`Gavel`) estilizado como fallback M3 cuando la imagen es nula, evitando el espacio en blanco.

### 2. Auditoría de Sincronización y Persistencia (Room)
**Hallazgo**: El estado "ENVIADO" no siempre era persistente entre reinicios.
- **Solución**: Se ha implementado un `combine` reactivo en `NotificacionesViewModel.kt` que cruza `TenderEntity` con `BudgetEntity` local.
- **Ley #2 (SSOT)**: Ahora, al entrar al mercado o tocar una licitación, el sistema descarga automáticamente el presupuesto enviado desde la nube si no existe localmente, garantizando que el SSOT local siempre refleje la realidad.

### 3. Mejoras de UX (Mercado Topik)
- **Tarjeta de Mercado**: Ahora muestra un distintivo verde "ENVIADO" y un botón flotante con el emoji `📑`.
- **Acción Inmediata**: Al tocar el emoji `📑`, se abre el visor PDF del presupuesto enviado sin salir del mercado (Ley #4: Inmediatez).
- **Detalle de Licitación**: Se añadió un banner de estado en la `ModalBottomSheet` con la fecha y hora exacta del envío.

## 🛠️ Puntos de Mejora Identificados
- **Redundancia**: Se detectó que `syncTendersByCategory` y `syncTendersByMatchKey` hacían operaciones casi idénticas. Se recomienda unificar en una función de búsqueda por filtros genéricos.
- **Obsoleto**: Algunos campos de `TenderEntity` como `clientPhotoUrl` han sido migrados a `clientThumbnail` para optimizar carga (v13.0). Se ha limpiado el código que aún buscaba el campo anterior.

---
**Auditoría realizada por Maverick AI - Departamento de Arquitectura**
