# 🟠 Manual de Presupuestos: App Naranja (Prestador)

Este documento detalla el proceso de creación, gestión de costos y el motor de envío del profesional (La Cocina Privada).

---

## 1. La Cocina (Gestión Privada)

El profesional trabaja en un entorno aislado para proteger sus márgenes y stock.

### A. Gestión de Borradores (Drafting)
Toda oferta en construcción vive en la tabla `borradores_presupuesto`.
*   **Auto-guardado**: El `BorradorPresupuestoViewModel` ejecuta un `Upsert` en cada cambio. 
*   **Unificación de Ítems**: Los materiales, servicios y gastos no se guardan en una lista JSON; se guardan en la tabla **`productos_mav`** de la App Naranja, vinculados mediante el `idBorrador`. Esto permite usar FTS (Búsqueda rápida) incluso dentro del borrador.

### B. Inventario y Costos
El sistema Maverick no solo maneja precios de venta, sino también **Costos Reales**.
*   **`ProductoMavEntity`**: Almacena el `precioCosto` actual.
*   **`MovimientoStockEntity`**: Registra auditorías de entradas/salidas (Compra, Ajuste, Venta).

---

## 2. El Proceso de Envío (Snapshot SUPREME)

Cuando el prestador pulsa "Enviar", el sistema "congela" la realidad económica en un documento inmutable.

### Paso 1: Mapeo de Snapshot
El **`SnapshotFinancieroMapper`** realiza un volcado físico de los datos:
1.  **Denormalización**: Suma los ítems por categoría para generar los subtotales de analítica (`subtotalArticulos`, `subtotalServicios`, etc.).
2.  **Copia Física**: El costo actual se guarda en `precioCostoSnapshot`. Esto protege las estadísticas futuras si el costo sube mañana.
3.  **Etiquetado**: Se aplica la `etiquetaManoObra` (ej: "Honorarios") según la preferencia del prestador.

### Paso 2: Compresión Elite
Para el tránsito por red, el **`CompresorPresupuestoMav`** actúa como un túnel:
1.  **Serialización**: Convierte el objeto `PresupuestoConItems` a un JSON de alta densidad.
2.  **GZIP**: Comprime el JSON, reduciendo su peso hasta un 85%.
3.  **Base64**: Codifica el binario para su envío como texto en Firebase.

### Paso 3: Protocolo de Aviso
El **`ProtocoloEnvioPresupuesto`** envía una señal RTDB al cliente y dispara una notificación push mediante FCM para asegurar que Be avise al destinatario.

---

## 🛠️ Archivos Intervinientes (App Naranja)

| Archivo | Responsabilidad |
| :--- | :--- |
| `BorradorPresupuestoViewModel.kt` | Cerebro del armado y cálculos en tiempo real. |
| `PrestadorPresupuestoRepository.kt` | Gestiona la persistencia local y el gatillo de envío. |
| `SnapshotFinancieroMapper.kt` | Congela los precios y genera los subtotales de analítica. |
| `CompresorPresupuestoMav.kt` | Motor de compresión GZIP/Base64. |
| `MovimientoStockEntity.kt` | Auditoría de inventario vinculada a ventas. |

---
**Informática Maverick - Departamento de Taller Profesional (2026)**
