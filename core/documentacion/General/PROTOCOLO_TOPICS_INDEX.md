# 📡 Protocolo de Topics e Índices de Red (v2026.ELITE)

Este manual define el estándar de **Señalización Atómica** de PBEM. Sustituye al protocolo anterior y garantiza una compatibilidad del 100% entre los sistemas de descubrimiento (Firestore) y las señales de red (FCM) mediante la unificación de normalización bajo el estándar de "Grandes Ligas".

---

## 🏛️ 1. SEGMENTACIÓN DE LA NUBE (FIRESTORE)

Los datos de descubrimiento se organizan en colecciones soberanas con nombres estandarizados en español:

| Colección | Contenido | Prefijo Atómico |
| :--- | :--- | :--- |
| **`indice_busqueda`** | Perfiles Shallow de profesionales y sucursales. | `P_` (Prestador) |
| **`indice_concursos`** | Licitaciones públicas activas (Mercado Topik). | `C_` (Concurso) |
| **`indice_promociones`** | Ofertas y anuncios geolocalizados. | `O_` (Oferta) |

---

## ⚙️ 2. MOTOR DE NORMALIZACIÓN ATÓMICA

El **`MotorDescubrimientoMav`** es el único orquestador legal de huellas. Se prohíbe terminantemente la manipulación manual de cadenas (`replace`, `lowercase`) en repositorios o viewmodels.

### A. El Estándar de Guion Bajo (Underscore Standard)
Para garantizar que un tópico FCM sea idéntico a una etiqueta de base de datos, se utiliza el método `estandarizarLlave()` que aplica `normalizeForTopic()`.
- **PROHIBIDO**: El uso de espacios en llaves de red (antes `normalizeFull`).
- **OBLIGATORIO**: Uso de minúsculas y guiones bajos para separar palabras.

### B. Fórmula de Huella Maestra
Todo índice debe seguir la estructura jerárquica: `[PREFIJO]_[CP]_[CATEGORIA]`.

| Tipo | Huella / Tópico FCM | Ejemplo |
| :--- | :--- | :--- |
| **Zona** | `Z_{CP}` | `Z_4000` |
| **Búsqueda** | `P_{CP}_{Rubro}` | `P_4000_plomeria` |
| **Oferta** | `O_{CP}_{Rubro}` | `O_4000_peluqueria` |
| **Concurso** | `C_{CP}_{Rubro}` | `C_4000_electricidad` |

---

## 📡 3. ARQUITECTURA DE CARGA DUAL (CACHÉ INSTAGRAM)

Para maximizar el rendimiento y permitir el uso offline, PBEM implementa la **Ley #2 (Costo Zero)** mediante `RemoteMediator`:

1.  **Fase de Descubrimiento (Red)**: La app consulta Firestore usando `whereArrayContainsAny` con las huellas maestras.
2.  **Fase de Siembra (Room)**: Los datos descargados (Shallow <1KB) se guardan inmediatamente en Room antes de mostrarse.
3.  **Fase de Reactividad (UI)**: La pantalla se suscribe únicamente a la base de datos local (SSOT).

> [!TIP]
> Este flujo garantiza que el usuario vea contenido instantáneamente al abrir la app, incluso sin conexión, mostrando los últimos datos sincronizados.

---

## 🛡️ 4. REGLAS SOBERANAS PARA EL DESARROLLADOR

1.  **Idioma Legal**: Todos los nombres de colecciones, campos y métodos de red deben estar en **Español** (Ley #9).
2.  **Soberanía de Huellas**: Si un método en el motor está marcado como comentado o depreciado, **NO LO USES**. Usa las funciones `generarHuellaMaestra` o `generarHuellasJerarquicasMav`.
3.  **Higiene de Datos Shallow**: Al publicar (App Azul o Naranja), es obligatorio inyectar el **Kit de Comunicación**: `idEmisor`, `nombreEmisor` y `fotoEmisor`. Sin esto, el receptor no podrá iniciar un chat desde el índice.

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
