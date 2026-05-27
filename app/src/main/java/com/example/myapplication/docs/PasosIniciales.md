# 🚀 PASOS INICIALES Y PROTOCOLO MAVERICK ELITE SSOT (V5.1)

Este documento es el **punto de entrada obligatorio** para cualquier intervención en el código. Define la filosofía de trabajo, la arquitectura y el protocolo de comunicación entre los componentes del sistema.

---

## ⚖️ LAS 3 LEYES FUNDAMENTALES (Prioridad Absoluta)

Estas leyes deben verificarse **OBLIGATORIAMENTE** antes de editar, actualizar, modificar, borrar o crear cualquier archivo.

1.  **Pantallas Tontas (Stateless Screens):** La UI no piensa ni contiene lógica de negocio. Solo recolecta el estado del **Obrero** (ViewModel de pantalla) y el **Intermediario/Contexto** (BeBrain/Coordinator) y emite eventos.
2.  **Metodología "Costo Zero":** Garantizar el mínimo acceso posible a servicios de pago/nube.
    *   **Persistencia Total:** Los datos **SIEMPRE** se guardan primero en Room.
    *   **Lectura Local:** Las búsquedas y consultas se realizan **SIEMPRE** primero en Room.
    *   **Sincronización Inteligente:** Solo se accede a Firebase para verificar actualizaciones. Si hay cambios confirmados, se descargan los datos y se actualiza Room.
3.  **Carga On-Demand (Bajo Demanda):** La carga de datos desde Room (y posteriormente Firebase) debe ser estrictamente bajo demanda para todas las entidades: Categorías, Servicios, Prestadores, Licitaciones, etc. No cargar listas masivas si no se requieren.
4.  **Ley de Inmediatez (Zero Friction):** El tiempo del usuario es sagrado. Está terminantemente prohibido el uso de delays artificiales (`delay()`) con fines puramente estéticos en el flujo de inicio, login o transiciones críticas. La navegación debe ser atómica: **Login-First**.
5.  **Carga Proactiva (Background Warm-up):** El Cerebro y los Obreros deben iniciar el acceso a Room y la verificación de identidad lo más temprano posible (MainActivity onCreate). El objetivo es que cuando la UI termine su transición inicial, los datos ya residan en el StateFlow listo para el renderizado.
6.  **Experiencia Visual Fluida (Anti-Flash):** Se prohíbe el uso de fondos de sistema por defecto (Blanco/Gris). La aplicación debe mantener su esquema oscuro (VantaBlack) desde el SplashScreen nativo hasta el último frame de Compose para garantizar una inmersión total y evitar destellos visuales.
7.  **Maverick Core Hardware:** El monitoreo de sensores (WiFi, Datos, GPS) reside exclusivamente en el `AppActionCoordinator`. Ningún ViewModel u Obrero debe monitorear el hardware por cuenta propia para evitar redundancia y consumo de batería.

---

## 📋 1. REGLAS DE ORO DEL DESARROLLO (Protocolo Maverick)

> [!IMPORTANT]
> **PRIORIDADES ABSOLUTAS:**
> 1. **Lectura Íntegra:** Al abrir este documento, es obligatorio leerlo de principio a fin sin saltar secciones.
> 2. **Sinceridad Técnica:** Si en algún momento no sé qué hacer o detecto que he olvidado un detalle del flujo, debo avisar inmediatamente para reorientar el trabajo.
> 3. **Feedback Detallado:** Ante cualquier error o solicitud de solución, siempre debo entregar una explicación técnica minuciosa del problema antes de proponer el arreglo.

1.  **Idioma:** Todas las respuestas y explicaciones técnicas deben ser en **Español**.
2.  **Integridad y Actualización de Comentarios:** **JAMÁS** borrar comentarios existentes. Al intervenir el código, es **OBLIGATORIO** actualizar o añadir comentarios que expliquen la nueva lógica.
3.  **Sectorización:** Todo archivo debe estar organizado en secciones detalladas (`// === SECCIÓN ===`).
4.  **Acción Quirúrgica:** Nunca borrar código que no esté estrictamente relacionado con el plan de acción aprobado.
5.  **Validación Previa:** Siempre presentar el plan de acción y esperar aprobación antes de realizar cambios.
6.  **Uso de Referencias (@):** Al solicitar cambios, se usará el prefijo `@` seguido del nombre del archivo de documentación para seguir los procedimientos allí descritos.

---

## 🎨 2. ESTÁNDARES DE IU/UX (Componentes Maverick)

Para garantizar la coherencia visual y funcional (Elite UI), es obligatorio el uso de los siguientes componentes y archivos del sistema de diseño:

1.  **Popups:** Toda ventana emergente táctica debe usar [PopUpEmergenteMolde.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/PopUpEmergenteMolde.kt).
2.  **Listas:** Para la creación de listas con cabeceras colapsables, usar [ListaElementosMoldeV2.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/ListaElementosMoldeV2.kt).
3.  **Sheets:** Paneles emergentes desde la base deben implementarse con [SheetEmergenteVertical.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/sheet/SheetEmergenteVertical.kt).
4.  **Colores:** Utilizar exclusivamente la paleta definida en [Colores.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/designsystem/components/Colores.kt).

---

## 🏗️ 3. ARQUITECTURA ELITE SSOT (Single Source of Truth)

La aplicación funciona bajo un esquema de **Cerebro, Mediador y Obreros**:

*   **El Cerebro (`BeBrainViewModel`):** El Director de Orquesta. Gestiona el HUD global y la navegación de alto nivel.
*   **El Mediador (`AppActionCoordinator`):** El Maestro de Intenciones y **Maverick Core**. Única fuente para conectividad, ubicación y filtros globales.
*   **Los Obreros (ViewModels de Pantalla):** Responsables de la lógica pesada. Transforman datos de Room en `UiState`.
*   **Pantallas Tontas (Stateless Screens):** La UI no piensa. Solo refleja el estado y emite eventos.
*   **Data Layer (Local-First):** Política de **Costo Cero**.
    *   Prioridad 1: Room (SQLite) - Datos inmediatos.
    *   Prioridad 2: Firebase (Firestore/RTDB) - Sincronización en segundo plano.

---

## 🗺️ 5. MAPA DE DOCUMENTACIÓN CONECTADA (ACCESOS DIRECTOS)

Haz clic en los siguientes enlaces para acceder a la documentación específica de cada módulo:

| Módulo | Documentación (Click para abrir) | Responsabilidad Principal |
| :--- | :--- | :--- |
| 🏠 **Home** | [HomeScreen.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/HomeScreen.md) | Grilla Bento, Banners y Orquestación inicial. |
| 👤 **Usuario** | [Usuario.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/Usuario.md) | Perfil, direcciones, empresas y login. |
| 👥 **Prestadores** | [Prestadores.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/Prestadores.md) | Perfiles profesionales y búsqueda. |
| 💰 **Presupuestos** | [Presupuestos.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/Presupuestos.md) | Licitaciones y comparativa técnica. |
| 💬 **Mensajería** | [Mensajeria.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/Mensajeria.md) | Chat tiempo real y multimedia. |
| 📅 **Calendario** | [Calendario.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/Calendario.md) | Agenda inteligente y recordatorios. |
| 🔥 **Promociones** | [Promociones.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/Promociones.md) | Feed de ofertas y anuncios. |
| 🤖 **Asistente Be** | [AsistenteBe.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/AsistenteBe.md) | Lógica de la burbuja y emociones. |
| 🛠️ **Herramientas Be** | [HerramientasBe.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/HerramientasBe.md) | Pequeñas Acciones (Toolbox). |
| ✨ **Animaciones** | [AnimacionesBe.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/AnimacionesBe.md) | Coreografía visual de Be. |
| 🗺️ **Navegación** | [NavegacionPantallas.md](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/docs/NavegacionPantallas.md) | Rutas, transiciones y SSOT de navegación. |

---

> [!IMPORTANT]
> **RECUERDA:** Para cualquier cambio, primero lee `PasosIniciales.md` y luego el documento específico del módulo.
