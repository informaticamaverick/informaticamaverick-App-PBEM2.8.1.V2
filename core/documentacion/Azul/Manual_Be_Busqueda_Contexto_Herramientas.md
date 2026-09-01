# 🧠 Manual Técnico: Be Assistant (Búsqueda y Herramientas v2026.ELITE)

Este manual detalla la implementación técnica de la inteligencia de Be bajo la **Ley #14 (El Embudo)** y la **Ley #15 (Buscar se escribe con Be)**.

---

## 🏛️ 1. El Triángulo Dorado de la Búsqueda

Para garantizar rendimiento de "Grandes Ligas", la búsqueda se fragmenta en tres niveles:

| Nivel | Componente | Responsabilidad |
| :--- | :--- | :--- |
| **Entrada** | `BeBarraBusqueda` | Captura física de pulsaciones y foco del teclado. |
| **Inteligencia** | `BeBusquedaMotor` | **Normalización Atómica** (quita acentos/mayúsculas) y **Debounce**. |
| **Ejecución** | ViewModels Obreros | Filtrado final en la fuente (**SQL-First**). |

---

## 🔄 2. Flujo de Datos (Data Pipeline)

1.  **Captura**: El usuario escribe en la `BarraBusquedaTacticaV3`.
2.  **Transmisión**: El `BeBusquedaViewModel` entrega el texto crudo al `BeBusquedaMotor` (Core).
3.  **Purificación**: El Motor aplica `normalizeFull()` y espera **300ms** (Debounce).
4.  **Emisión**: El Motor emite la señal limpia vía `consultaNormalizadaDebounced`.
5.  **Reacción**: Las pantallas (Chats, Eventos, Presupuestos) observan el flujo y actualizan su lista instantáneamente mediante consultas SQL optimizadas.

---

## 🛠️ 3. Sistema de Herramientas (Bento Islands)

Be expone herramientas basadas en IDs registrados en el `BeDictionary`.

### Clasificación de Herramientas:
- **Primarias**: Acciones críticas de la pantalla (ej: `nuevo_concurso`).
- **Navegación**: Flujo entre vistas (ej: `atras`, `sig`).
- **Edición**: Modificación de datos (ej: `select_all`, `delete_multi`).
- **Sistema**: Utilidades de Be (ej: `teclado`, `cerrar_todo`).

---

## 📐 4. Física HUD y Margen ROG

### Margen ROG (110.dp)
Toda hoja o lista que interactúe con el modo búsqueda debe aplicar un `padding` superior de **110.dp**. Esto evita colisiones visuales entre los resultados y los ojos de Be.

### Soberanía por Contrato (Ley #12)
Cada pantalla reclama su configuración al entrar mediante un `DisposableEffect`. Be es un **Portavoz Pasivo**: solo dibuja el contrato que el Obrero le dicta.

---
> [!IMPORTANT]
> **Higiene de Datos:** Está terminantemente prohibido filtrar listas en RAM usando `.filter { }`. El filtrado debe ocurrir en el DAO mediante parámetros `@Query`.
