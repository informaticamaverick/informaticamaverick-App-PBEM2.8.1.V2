# 💰 Protocolo Presupuestos SUPREME: Arquitectura de Ciclo Completo (v2026.SUPREME)

Este documento detalla el estándar técnico para la gestión de presupuestos en el ecosistema Informática Maverick, implementando el patrón de **"La Cocina vs. La Mesa Final"** para garantizar privacidad, inmutabilidad contable y analíticas de alto desempeño.

---

## 🏛️ 1. Filosofía: La Cocina vs. La Mesa Final

Para cumplir con la **Ley #16 (Soberanía de Persistencia)**, el sistema divide el ciclo de vida del presupuesto en dos dominios aislados:

### A. La Cocina (Módulo `:prestador`)
Es el entorno privado del profesional. Aquí los datos son **editables, volátiles y detallados**. El cliente nunca tiene acceso a esta base de datos.
*   **Archivos Clave**: `BorradorPresupuestoEntity`, `ProductoMavEntity` (Catálogo Privado).
*   **Propósito**: Permitir al prestador armar su oferta, ajustar costos internos y manejar stock sin exponer sus márgenes al cliente.

### B. La Mesa Final (Módulo `:core`)
Representa el **Documento Soberano**. Una vez enviado, el presupuesto se convierte en un contrato inmutable.
*   **Archivos Clave**: `PresupuestoFinalEntity`, `ProductoFinalEntity`, `FinanzaFinalEntity`.
*   **Propósito**: Servir como "Snapshot" (Foto fija) de la oferta. Si el prestador cambia sus precios en el catálogo mañana, el presupuesto enviado **no se altera**.

---

## 🟠 2. App Naranja: Proceso de Creación y Envío

### Paso 1: El Borrador (Drafting)
Cuando el prestador inicia un presupuesto (desde un chat o concurso), se crea una entrada en `borradores_presupuesto`.
*   **ViewModel**: `BorradorPresupuestoViewModel` orquesta la lógica.
*   **Auto-guardado**: Cada cambio en la UI dispara un `Upsert` táctico. Los ítems (Materiales, Servicios, Gastos) se guardan en la tabla `productos_mav` vinculados mediante el campo `idBorrador`.

### Paso 2: El Snapshot Financiero
Al pulsar "Enviar", el **`SnapshotFinancieroMapper`** realiza las siguientes acciones:
1.  **Congelación de Precios**: Copia el `precioVenta` y el `precioCosto` actual a campos `snapshot`.
2.  **Denormalización de Subtotales**: Suma todos los materiales, servicios y gastos, y los guarda directamente en la cabecera (`subtotalArticulos`, `subtotalServicios`, etc.).
3.  **Generación de Finanzas**: Crea el desglose atómico de impuestos e intereses en `finanzas_finales_mav`.

### Paso 3: Envío Shared
El `PrestadorPresupuestoRepository` guarda el documento en la tabla de "Finales" del prestador y lo despacha al repositorio de red.

---

## ⚡ 3. Motor de Tránsito y Compresión

Para cumplir con la **Ley #8 (Tránsito Efímero)** y minimizar el uso de datos, usamos el **`CompresorPresupuestoMav`**:

1.  **JSON Táctico**: Se genera un JSON con llaves ultra-cortas (ej: `sArt` para subtotalArticulos, `ls` para lineas).
2.  **Flujo GZIP**: El JSON se comprime mediante `GZIPOutputStream`.
3.  **Base64**: El binario resultante se codifica en Base64 para viajar como un string legal en el nodo `contenido` del chat en Firebase RTDB.

---

## 🔵 4. App Azul: Recepción y Análisis

### Paso 1: Captura en Red
El **`ChatMotorSincRepositorio`** escucha el nodo de Firebase. Al detectar un mensaje tipo `PRESUPUESTO`, delega al **`MapeadorMensajesMav`**.

### Paso 2: Descompresión y Persistencia
1.  El mapeador invoca a `CompresorPresupuestoMav.descomprimir`.
2.  El objeto `PresupuestoConItems` (Header + Lineas + Finanzas) impacta en **Room** de forma atómica.
3.  **Ley #2**: Una vez en Room, el cliente confirma la recepción y el mensaje original puede ser purgado de la nube (Tránsito Efímero).

### Paso 3: El Analizador de Be (Comparativa Instantánea)
Gracias a la denormalización realizada en la App Naranja, el **`PresupuestoAnalyticsViewModel`** no necesita procesar listas:
*   Lee directamente los subtotales de la cabecera.
*   Compara: "Prestador A cobra $100 en Materiales vs Prestador B que cobra $120".
*   **Resultado**: Be ofrece comparativas multidimensionales con **Zero-Lag**, incluso con cientos de presupuestos en memoria.

---

## 📊 5. Entidades SUPREME (Estructura de Datos)

| Entidad | Módulo | Función |
| :--- | :--- | :--- |
| `PresupuestoFinalEntity` | `:core` | Cabecera inmutable con subtotales denormalizados. |
| `ProductoFinalEntity` | `:core` | Línea de ítem con snapshot de costo y venta. |
| `FinanzaFinalEntity` | `:core` | Desglose de Impuestos, Intereses y Recargos. |
| `ProductoMavEntity` | `:prestador` | Catálogo privado y líneas de borrador (Unificado). |
| `MovimientoStockEntity` | `:prestador` | Historial legal de entradas y salidas de inventario. |

---

## 🛠️ Archivos Intervinientes (Resumen)

### Lógica Core
*   `com.example.myapplication.core.utilidades.CompresorPresupuestoMav`
*   `com.example.myapplication.core.datos.remoto.mapeadores.SnapshotFinancieroMapper`
*   `com.example.myapplication.core.dominio.motores.CalculadoraPresupuestoMav`

### Lógica Prestador
*   `com.example.myapplication.prestador.datos.repositorios.PrestadorPresupuestoRepository`
*   `com.example.myapplication.prestador.viewmodel.presupuesto.BorradorPresupuestoViewModel`

### Lógica Cliente
*   `com.example.myapplication.viewmodel.budget.PresupuestoAnalyticsViewModel`
*   `com.example.myapplication.core.datos.remoto.mapeadores.PresupuestoMapper`

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
