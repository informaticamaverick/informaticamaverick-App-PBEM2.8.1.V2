# 📘 Protocolo Maverick Elite: Módulo `:core` (v5.5 - High-Performance & Audit)

Este módulo es el **Corazón de Datos e Inteligencia (SSOT - Single Source of Truth)** del ecosistema Informática Maverick. Centraliza la lógica de negocio, persistencia local (Room) y comunicación remota (Firebase/Retrofit) mediante una arquitectura **Offline-First** y sincronización por **Deltas**, asegurando paridad total entre la App del Cliente y la App del Prestador.

---

## ⚖️ LAS 7 LEYES ELITE (Protocolo de Arquitectura)

### 1. Pantallas Tontas (Stateless UI & MVI)
La UI no toma decisiones de filtrado ni calcula identidades. Solo refleja el `UiState` emitido por el ViewModel.
*   **Terminología**: Implementación de **Unidirectional Data Flow (UDF)**. La lógica de negocio reside en UseCases/ViewModels, nunca en Composables.

### 2. Metodología "Costo Zero" (Cloud Economy & Local-First)
Todo dato recibido desde Firebase debe impactar en **Room** con sus tags de identidad antes de ser emitido a la UI.
*   **Mecanismo**: La lectura es siempre **Local-First**. Firebase actúa como un sistema de respaldo y transporte, optimizando el uso de la cuota gratuita (Spark Plan) mediante **Estrategias de Cache Proactivo**.

### 3. Carga On-Demand Dual (Shallow vs. Deep Loading)
*   **Shallow (Carga Ligera)**: Listas de búsqueda y chats filtradas por `localBranchId` usando proyecciones mínimas de Room.
*   **Deep (Carga Profunda)**: Detalles de mensajes o perfiles completos cargados por `chatId`/`uid` solo bajo demanda explícita.
*   **Objetivo**: Minimizar el **Over-fetching** y mejorar el **Time to Content (TTC)**.

### 4. Ley de Inmediatez (Reactive Indexing)
El filtrado por sucursales y categorías debe ser instantáneo mediante columnas indexadas y **DAOs Reactivos**.
*   **Técnica**: Uso de `Flow<List<T>>` en Room para que cualquier cambio en la base de datos se refleje en milisegundos en la pantalla sin recargar.

### 5. Carga Proactiva (Background Warm-up)
Al recibir una notificación (FCM) o mensaje, el sistema actualiza automáticamente el perfil del prestador/cliente (Upsert) en background.
*   **Garantía**: Evita inconsistencias visuales y asegura que los IDs de identidad estén listos antes de que el usuario entre a la pantalla.

### 6. Ley Pareja (Proximity Deduplication & Fair Exposure)
Ninguna empresa o profesional puede monopolizar el campo visual del usuario.
*   **Agrupamiento (Aggregation)**: Si un proveedor tiene múltiples sucursales en un mismo Código Postal, el sistema realiza una **Deduplicación por Cercanía**.
*   **Lógica**: Se calcula la distancia (Haversine) y se muestra únicamente la sucursal más cercana.
*   **Indicador de Expansión**: Uso de un badge dinámico (`+X sucursales`) para informar sobre la infraestructura sin romper el feed.
*   **Soberanía de Empresa (Atomic Sovereignty)**: Si el flag `priorizarEmpresa` es activo, los datos personales se eliminan del `search_index` para evitar ruido visual y redundancia.

### 7. Ley de Trazabilidad Hormiga (Omnipresent Tactical Audit)
Cada viaje del bit debe ser auditable en tiempo real.
*   **Etiquetado Táctico**: Todo flujo (UI -> VM -> Repository -> Remote) debe incluir logs con tags estandarizados entre corchetes (ej: `[FETCH_RESULTS]`, `[SYNC_INDEX]`, `[EPHEMERAL_CLEANUP]`).
*   **Diagnóstico de Campo**: Permite diagnosticar fallos de sincronización o inconsistencias de CP/Geohash simplemente filtrando el Logcat, eliminando la necesidad de depuración paso a paso (Breakpoints).

### 8. Ley de Tránsito Efímero (P2P-Hybrid & Cloud Economy)
Los datos pesados no deben vivir en la nube, solo viajar a través de ella.
*   **Mecanismo**: Las imágenes y audios se envían vía Realtime Database como Base64 (Tránsito). 
*   **Acción Obligatoria**: Una vez que el destinatario confirma la persistencia en **Room** (SSOT Local), el mensaje multimedia **DEBE ser eliminado automáticamente de Firebase**.
*   **Objetivo**: Garantizar el **Costo Zero** de infraestructura (Cloud Storage) y asegurar la **Privacidad Total**, convirtiendo el dispositivo del usuario en el único poseedor del dato físico.

---

## 🏛️ ESTRUCTURA DE COMUNICACIÓN ELITE (Identidades)

### 🆔 Tagged Identity Symmetric (SSOT)
Para asegurar paridad total y evitar "fugas de identidad", cada mensaje se guarda con 4 tags atómicos:

| Campo | Rol Técnico | Propósito |
| :--- | :--- | :--- |
| `senderBranchId` | Identidad de Origen | SSOT del Emisor |
| `receiverBranchId` | Identidad de Destino | SSOT del Receptor |
| **`localBranchId`** | **Identidad Operativa** | Filtrado de Pestaña Local |
| **`remoteBranchId`** | **Referencia de Perfil** | Vínculo con el "Otro" |

---

## 🚀 INTELIGENCIA GEOGRÁFICA (Grandes Ligas)

### 📍 Geohash & Spatial Indexing
El sistema no solo depende del Código Postal. Implementa un sistema de **Geohashing (Base32)** para:
1.  **Búsquedas por Radio**: Consultas ultrarrápidas en Firestore mediante rangos de strings.
2.  **Preparación FAST**: Búsqueda instantánea combinando `category_online_24h` para resultados en tiempo real.

---
**Informática Maverick - Departamento de Arquitectura de Software (2024)**
