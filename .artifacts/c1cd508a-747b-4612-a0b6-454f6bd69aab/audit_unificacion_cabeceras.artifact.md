# Auditoría de Unificación: Cabeceras de Lista (v2026.ELITE)

He realizado un análisis comparativo de las pantallas core (`Presupuestos`, `Chats`, `Calendario`, `Home` y `Descubrimiento`). A continuación, el detalle de por qué visualmente no son idénticas y el plan para corregirlo.

## 1. El Problema del Emoji (Presupuestos)
En la **Imagen 1**, el emoji `💰` aparecía duplicado.
- **Causa**: Se estaba enviando como parámetro `icono = "💰"` al `ArmadorListaPantallaCompleta`, ignorando que la cabecera superior de la pantalla ya lo incluía.
- **Acción**: ✅ He modificado `PresupuestosScreen.kt` eliminando el icono redundante. La lista ahora se alinea limpiamente con la burbuja de perfil.

## 2. Divergencia de Componentes (Root Cause)

| Pantalla | Componente Utilizado | Observación Visual |
| :--- | :--- | :--- |
| **Presupuestos** | `ArmadorListaPantallaCompleta` | **Estándar ROG**. Cabecera integrada con la lista y física de colapso. |
| **Home** | `ArmadorListaPantallaCompleta` | **Estándar ROG**. Consistente con Presupuestos. |
| **Chats** | `MoldeSheetEmergenteV3` | **Manual**. Construye su propia cabecera fila por fila. No usa el Armador. |
| **Calendario** | `MoldeSheetEmergenteV3` | **Manual**. Similar a Chats. Incluye lógica de fecha personalizada en el centro. |
| **Descubrimiento**| `MoldeSheetEmergenteV3` | **Manual**. Usado para los resultados de rubros. |

## 3. Por qué son distintas?

1.  **Migración Incompleta**: El `ArmadorListaPantallaCompleta` es la evolución más reciente (v2.8). Las pantallas de `Chat` y `Calendario` se quedaron en la versión anterior donde se usaba el `MoldeSheetEmergenteV3` para simular una "Bandeja" inmersiva.
2.  **Rigidez de Slots**: La cabecera estandarizada (`CabeceraMaverickV3`) actual es rígida. No tiene un "Slot Central" para el selector de fecha del Calendario, lo que impide su migración inmediata sin romper funcionalidad.
3.  **Jerarquía de Textos**: En `Chat`, el subtítulo está hardcodeado como "MI PERFIL". En `Presupuestos`, el subtítulo es contextual ("Gestión de Concursos"). Esto rompe la simetría visual entre pantallas hermanas.

## 4. Plan de Unificación "Elite"

1.  **Evolución del Armador**: Debemos actualizar `ArmadorMoldeListaV3.kt` para que acepte un `slotCentral: @Composable () -> Unit` opcional.
2.  **Migración Progresiva**: Una vez que el Armador sea flexible, migraremos `ChatScreen.kt` y `CalendarScreen.kt` para que dejen de usar cabeceras manuales.
3.  **Soberanía de Subtítulos**: Estandarizar que el subtítulo de la lista siempre describa la acción o el perfil activo, no mezclar ambos.

---
**Informática Maverick - Auditoría de Arquitectura UI (2026)**
