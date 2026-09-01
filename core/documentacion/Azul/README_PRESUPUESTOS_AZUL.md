# 🔵 Manual de Presupuestos: App Azul (Cliente)

Este documento detalla el ciclo de vida de un presupuesto desde que llega como señal de red hasta que es procesado por el Analizador Táctico de Be.

---

## 1. El Flujo de Recepción (Ley #2: Local-First)

El proceso es totalmente pasivo y reactivo. El usuario no necesita "descargar" el presupuesto; el sistema lo asegura en Room automáticamente.

### A. Detección en Tiempo Real
El **`ChatMotorSincRepositorio`** (`core/datos/repositorios`) mantiene un listener en el nodo `chats/{idChat}` de Firebase RTDB.
*   **Trigger**: Al detectar un mensaje con `tipo == "PRESUPUESTO"`.
*   **Acción**: Delega el procesamiento al `MapeadorMensajesMav`.

### B. Descompresión de Tránsito (Motor SUPREME)
El contenido llega como una cadena Base64 comprimida para ahorrar datos (Ley #8).
*   **Componente**: `CompresorPresupuestoMav.descomprimir(contenido)`
*   **Proceso**: 
    1.  Decodifica Base64.
    2.  Descomprime el flujo GZIP.
    3.  Parsea el JSON táctico (llaves cortas como `sArt`, `ls`, `fs`).
*   **Resultado**: Un objeto `PresupuestoConItems`.

### C. Persistencia Atómica
El **`ChatMotorSincLocal`** (`core/dominio/motores`) recibe el objeto y lo guarda en Room:
*   **Transacción**: Se guardan en una sola operación la cabecera (`PresupuestoFinalEntity`), las líneas (`ProductoFinalEntity`) y el desglose financiero (`FinanzaFinalEntity`).
*   **Confirmación**: Una vez persistido, el cliente marca el mensaje como "Recibido" y permite que la señal efímera sea purgada de la nube.

---

## 2. El Analizador Táctico (Zero-Lag Analytics)

La App Azul está diseñada para comparar múltiples ofertas de forma instantánea.

### A. Los Subtotales Denormalizados
A diferencia de sistemas básicos que suman ítems en el momento, Maverick recibe los subtotales ya calculados por el prestador:
*   `subtotalArticulos`: Materiales.
*   `subtotalServicios`: Mano de Obra.
*   `subtotalGastos`: Viáticos/Logística.
*   `totalImpuestos`: Carga fiscal.

### B. Funcionamiento del Analizador
El **`PresupuestoAnalyticsViewModel`** (`app/viewmodel/budget`) consume estos campos fijos.
1.  **Carga**: Recupera los `PresupuestoConItems` seleccionados.
2.  **Mapeo**: Transforma los datos al modelo `ElementoGraficoPresupuesto`.
3.  **Comparación**: Be compara los campos de la cabecera directamente.
    *   *Ejemplo*: Si comparas 5 plomeros, Be te dirá quién es más caro en materiales y quién en mano de obra sin recorrer miles de líneas de productos.

---

## 🛠️ Archivos Intervinientes (App Azul)

| Archivo | Responsabilidad |
| :--- | :--- |
| `MapeadorMensajesMav.kt` | Orquestador de la conversión Firebase -> Room. |
| `PresupuestoFinalEntity.kt` | El "Folder" que contiene los subtotales de analítica. |
| `PresupuestoConItems.kt` | Relación soberana que une cabecera, líneas y finanzas. |
| `PresupuestoAnalyticsViewModel.kt` | Cerebro del comparador de Be. |
| `BudgetPreviewPDFDialog.kt` | Renderizador inmutable de la planilla A4. |

---
**Informática Maverick - División de Inteligencia para Clientes (2026)**
