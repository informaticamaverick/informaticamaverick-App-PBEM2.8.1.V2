# Análisis Técnico: Flujo de Datos de Productos y Presupuestos

Este documento detalla el funcionamiento actual del ecosistema de presupuestos en la App Naranja y propone una hoja de ruta para alcanzar estándares de "Grandes Ligas" (Apps Tier-1).

## 1. Arquitectura Actual del Flujo

### Capa de Datos (Core)
- **Entidades**:
    - `PresupuestoEntity`: Almacena el documento completo, incluyendo listas de artículos, servicios y honorarios.
    - `ProductoMavEntity`: Define los ítems del catálogo reutilizable.
- **Persistencia (Room)**:
    - `PresupuestoDao`: Maneja las tablas `presupuestos_mav` y `concursos_mav`.
    - `ProductoMavDao`: Maneja el catálogo de `productos_mav`.
- **Repositorio Central**: `PresupuestoMavRepository`
    - Orquesta el guardado local y el envío a Firebase.
    - Utiliza **RTDB** (`transito_presupuestos`) para el envío efímero de presupuestos completos entre identidades.

### Capa de Negocio (Prestador)
- **ViewModel**: `PresupuestoMavViewModel`
    - Gestiona el estado de creación, catálogos (almacenados actualmente como metadata de la app) y la interacción con la UI.
- **Mapeo y Compresión**:
    - `CompresorPresupuestoMav`: Utiliza **GZIP + Base64** para comprimir el JSON del presupuesto. Este se usa específicamente para enviar el presupuesto *dentro* de un mensaje de chat, permitiendo una carga ultra-rápida.
- **Cálculos**: `CalculadoraPresupuestoMav` realiza la aritmética de subtotales, impuestos y descuentos.

### Capa de Interfaz (UI)
- **Pantalla Independiente**: `CrearPresupuestoPrestadorScreen` (Para licitaciones/concursos).
- **Hoja de Chat**: `NuevoPresupuestoSheet` (Para ventas directas en el chat).
- **Sub-hojas**: `HojasPresupuestoMav.kt` (Formularios de carga de ítems).

---

## 2. Puntos Críticos Detectados

1.  **Redundancia de Formulación**: Existen dos flujos de creación (Pantalla vs Sheet) que comparten lógica pero no estructura visual, lo que duplica el mantenimiento.
2.  **Inconsistencia en el Envío**: Algunos presupuestos se envían como objetos completos por RTDB y otros se comprimen en cadenas dentro de mensajes. Las apps "Grandes Ligas" suelen usar un **ID de Referencia** en el mensaje y un **Documento Atómico** en la base de datos distribuida.
3.  **Gestión de Catálogo**: Los catálogos se guardan actualmente en una tabla de `AppMetadata` como strings delimitados por pipes (`|`), lo cual es poco escalable y propenso a errores de parseo.

---

## 3. Estándares de "Grandes Ligas" (Benchmarking)

Apps como **Shopify, QuickBooks o Zoho Invoice** manejan los presupuestos (Estimates) de la siguiente manera:

### Flujo de Datos Profesional
1.  **Builder Universal**: Un único componente de creación que puede funcionar en pantalla completa, diálogo o embebido.
2.  **State Management Inmutable**: El presupuesto en construcción se maneja como una copia inmutable que solo se persiste al tocar "Finalizar".
3.  **Offline First**: El documento se genera localmente con un **UUID** y se sincroniza en segundo plano mediante un `WorkManager`, garantizando que el usuario nunca espere a la red.
4.  **Referencia por Puntero**: El mensaje de chat solo lleva el `idPresupuesto` y una `miniaturaBase64`. El cuerpo denso del presupuesto se descarga por demanda al abrir la ficha técnica.

---

## 4. Propuesta: El Nuevo "Elite Budget Builder"

Para llevar la App Naranja al siguiente nivel, propongo crear un nuevo archivo de orquestación visual:

### [NEW] `EliteBudgetBuilder.kt`
- **Responsabilidad**: Unificar `CrearPresupuestoPrestadorScreen` y `NuevoPresupuestoSheet`.
- **Arquitectura**:
    - **UI Stateless**: Recibe una `PresupuestoEntity` y emite eventos de cambio.
    - **Integración con Catálogo**: Búsqueda real sobre la tabla `productos_mav` en lugar de strings de metadata.
    - **Auto-guardado**: Guardado de borradores (Drafts) en Room mientras el usuario edita.

---

## ¿Deseas que preparemos el Plan de Implementación para el nuevo Elite Budget Builder?
Este sería un cambio arquitectónico mayor que simplificaría todo el módulo de ventas.
