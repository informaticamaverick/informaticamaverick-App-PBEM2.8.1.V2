# 📘 Protocolo Maverick Elite: Módulo `:core` (v2026.FINAL)

Este módulo es el **Corazón de Datos e Inteligencia (SSOT)** del ecosistema Informática Maverick. Centraliza la lógica de negocio, persistencia local (Room) y comunicación remota (Firebase) mediante una arquitectura **Offline-First**.

---

## 🔄 LA PIPELINE ELITE (FLUJO DE DATOS v2026)

Para garantizar la integridad y el SSOT (Single Source of Truth), todo nuevo flujo de datos debe seguir esta jerarquía obligatoria:

1.  **Entidad (`Entity`)**: Estructura pura de Room en `:core`. Representa el dato físico.
2.  **Relación (`RelacionesBD`)**: Si el dato requiere joins (ej: Presupuesto con Items).
3.  **DAO**: Interfaz de acceso a datos con funciones reactivas (`Flow`).
4.  **Repositorio**: Orquesta la persistencia local y la sincronización con la nube.
5.  **Mapper**: Transforma entidades de base de datos a Modelos de Dominio.
6.  **Modelo de Dominio (`MDominio`)**: Objeto ligero y agnóstico para lógica de negocio.
7.  **ViewModel**: Consume el repositorio, aplica filtros y emite un `UiState` consolidado.
8.  **UiState**: Data class que representa el 100% de la verdad visual de la pantalla.
9.  **Screen (Compose)**: Pantalla "tonta" que solo renderiza el `UiState`.

---

## ⚖️ LAS 13 LEYES (Protocolo de Arquitectura Elite)

### 1. Pantallas Tontas (Stateless UI & MVI)
La UI no toma decisiones de filtrado, no calcula identidades ni procesa datos crudos. Solo refleja el `UiState` emitido por el ViewModel.
*   **Mecanismo**: Implementación estricta de **Unidirectional Data Flow (UDF)**. La lógica de negocio reside exclusivamente en UseCases/ViewModels o Repositorios, nunca en Composables.

### 2. Metodología "Costo Zero" (Cloud Economy & Cero Duplicidad)
Todo dato recibido desde Firebase debe impactar en **Room** con sus tags de identidad antes de ser emitido a la UI. La lectura es siempre **Local-First**.
*   **Cero Duplicidad**: Se prohíbe la duplicación de tablas para búsqueda. Se utilizan **Vistas de Base de Datos (DatabaseViews)** para unificar identidades (ej: Humanos y Empresas) sin desperdiciar almacenamiento.
*   **Eficiencia**: Firebase actúa únicamente como sistema de respaldo y transporte.

### 3. Ley de Carga On-Demand Dual (Shallow vs Deep & Local Lazy)
La carga de datos debe ser perezosa para proteger el **Hilo Principal (Main Thread)** y la memoria RAM.
*   **Shallow (Ligera)**: Listas de búsqueda, chats e índices deben usar proyecciones mínimas de Room (<1KB).
*   **Deep (Profunda)**: Detalles completos (biografías, galerías full) se cargan solo bajo demanda explícita.
*   **Local Lazy**: Está prohibido cargar listas de más de 50 elementos "en frío" desde Room. Se debe usar carga segmentada o porción a porción para evitar bloqueos en la UI.

### 4. Ley de Inmediatez (Reactive Paging & Indexing)
El descubrimiento y filtrado debe ser instantáneo mediante columnas indexadas y **DAOs Reactivos**.
*   **Técnica**: Uso obligatorio de `Flow<List<T>>` o `PagingData`. 
*   **Motor**: Se utiliza **Paging 3 con RemoteMediator** para que la sincronización entre Firestore y Room ocurra en lotes, garantizando que el usuario siempre vea datos locales mientras se descarga el resto.

### 5. Carga Proactiva (Background Warm-up)
Al recibir señales de red (FCM), mensajes o al iniciar sesión, el sistema actualiza automáticamente los perfiles y activos (Upsert) en segundo plano.
*   **Garantía**: Asegura que los IDs de identidad estén listos en Room antes de que el usuario entre a la pantalla, eliminando estados de "Cargando" innecesarios.

### 6. Ley Pareja (Deduplicación & Soberanía Atómica)
Ninguna entidad puede monopolizar el campo visual. Si un proveedor tiene múltiples sucursales en una zona, el sistema realiza una **Deduplicación por Proximidad**.
*   **Soberanía**: Si el flag `priorizarEmpresa` es activo, los datos personales se ocultan del índice de búsqueda para garantizar la soberanía de marca comercial.

### 7. Ley de Trazabilidad Hormiga (Omnipresent Tactical Audit)
Cada bit debe dejar un rastro claro. Todo flujo (UI -> VM -> Repo -> Remote) debe incluir logs estandarizados entre corchetes (ej: `[SYNC_SHALLOW]`, `[CONCURSO_EMITIDO]`).
*   **Huellas de Pan (Cabeceras)**: Cada archivo debe comenzar con un bloque de comentario indicando: **Título**, **Propósito**, **Funcionamiento Interno** y **Relación** con el ecosistema.

### 8. Ley de Tránsito Efímero (Privacidad & Cloud Economy)
Los datos pesados (imágenes, audios) no deben vivir en la nube permanentemente.
*   **Mecanismo**: Una vez que el destinatario confirma la persistencia en Room (SSOT Local), el mensaje multimedia **DEBE ser eliminado automáticamente de Firebase**. El dispositivo del usuario es el único poseedor del dato físico.

### 9. Ley de Núcleo Atómico (Desacoplamiento & Determinismo)
El Core es el cerebro central; las Apps son extremidades especializadas.
*   **Soberanía de Modelos**: Solo las **Entities**, **DAOs** y **Modelos de Dominio** residen en `:core`.
*   **Determinismo**: El **`MotorDescubrimientoMav`** es el único generador legal de llaves (Huellas) y tópicos. Nadie fuera de este motor decide cómo se nombra una señal en la red.
*   **Idioma**: Todo código y nombres de archivos deben estar en **Español**.

### 10. Ley del Rompecabezas (Screen Anatomy Protocol)
Anatomía obligatoria para la construcción de pantallas, asegurando reusabilidad total entre la App del Cliente y del Prestador.
*   **Jerarquía**: **Caja (Screen)** -> **Lienzo (Lienzo)** -> **Secciones** -> **Bloques** -> **Piezas**. 
*   **Previews**: Toda pieza debe incluir su `@Preview` mostrando obligatoriamente el Modo Lectura y el Modo Edición con datos Mock.

### 11. Ley "CuatroOjos" (Accesibilidad & Elasticidad Visual)
Ninguna información debe morir cortada por limitaciones físicas o de configuración. La UI debe ser elástica y adaptarse a los ojos de cualquier usuario.
*   **Escalabilidad**: Se debe prever el uso de fuentes grandes en el sistema (`fontScale > 1.0`). Para títulos y métricas críticas, es obligatorio el uso de **`AutoSizeText`** para reducir el tamaño de fuente si el espacio es insuficiente.
*   **Paridad**: En documentos técnicos e inalterables (ej: Planilla A4), se debe forzar una escala fija mediante el componente `BloquearEscaladoFuente` para garantizar la fidelidad del documento final.

### 12. Ley "La Unión hace la Búsqueda" (Soberanía por Contrato & Coordinador de Navegación)
El Asistente Be y los elementos globales (HUD) son portales de acceso y portavoces, no dueños de la lógica. La soberanía del contexto reside en las pantallas (Obreros).
*   **Coordinador de Navegación (SSOT)**: Existe un único coordinador encargado de la visibilidad de la interfaz. Mantiene un **Mapa de Registros Soberanos**.
*   **Registro Automático**: Cada pantalla/hoja registra su contrato (`mostrarBe`, `mostrarBarraNavegacion`, `herramientas`) al entrar y lo remueve al salir. El sistema activa automáticamente el último registro del mapa.
*   **Higiene Radical**: Se prohíben las reglas de visibilidad basadas en texto de ruta en el Scaffold. Todo control debe ser declarativo mediante el contrato soberano.
*   **Segmentación Elite (SRP)**: El cerebro de Be está fragmentado para garantizar fluidez. `CoordinadorNavegacion` (Visibilidad), `BeBusquedaViewModel` (Escaneo), `BeFisicaViewModel` (Ojos) y `BeCuerpoViewModel` (Herramientas).

### 13. Ley de Estratificación Atómica (Especialización sobre Monolitos)
La complejidad del ecosistema se combate mediante la fragmentación jerárquica de componentes. Se prohíbe el patrón "Master Builder" (un solo componente gigante que intenta resolverlo todo).
*   **Estratificación (Layering)**:
    1.  **Átomos**: Bloques base indivisibles (`MenuItemV3`, `BotonV3`). No poseen lógica de negocio y son 100% configurables.
    2.  **Moldes (Infrastructure)**: Componentes de "envase" (`MoldeMenuArmadorV3`, `MoldeSheetV3`). Gestionan la geometría técnica, colas/flechas, sombras y animaciones. No deciden el contenido.
    3.  **Armadores (Assemblers)**: Archivos centrales de inteligencia (`ArmadorMenuV3`). Saben qué átomos pertenecen a qué contexto (ej: qué lleva un menú de perfil).
    4.  **Especialistas (Opinionated)**: El componente final que consume la UI (`MenuUbicacionV3`). Son archivos pequeños que unen un Molde con un Armador.
*   **Estándar Android**: Siguiendo las directrices de Jetpack Compose, se priorizan los **Slot APIs** (lambdas) para inyectar contenido, evitando funciones con largas listas de parámetros opcionales ("Grab-bag Style").

### 14. Ley del Embudo (Filtrado en la Fuente)
Los grandes volúmenes de datos se gestionan mediante el filtrado atómico en la base de datos, prohibiendo el procesamiento manual en la memoria RAM del dispositivo.
*   **Filtrado SQL-First**: El filtrado y ordenamiento de chats, eventos y presupuestos debe ocurrir en el DAO (Room) mediante parámetros SQL. Está estrictamente prohibido usar `.filter { }` en Kotlin para listas de más de 100 elementos.
*   **Filtros de Metadatos (Rubros)**: La lista de opciones en los menús de filtros (ej: categorías en uso) debe obtenerse mediante un **`SELECT DISTINCT`** directo. Esto asegura que el menú sea instantáneo y solo muestre opciones con datos reales, incluso con miles de registros.
*   **Soberanía de Consulta**: El ViewModel de cada pantalla es el dueño de una `data class` de filtros estructurada. Al cambiar cualquier parámetro, el flujo de datos debe invalidarse y recargarse automáticamente (UDF).
*   **Be como Portavoz Pasivo**: El asistente Be no conoce la lógica de los filtros; solo recibe órdenes visuales para dibujar las burbujas y botones de acción.

### 15. Ley "Buscar se escribe con Be" (Portal de Inteligencia)
La búsqueda no es una utilidad aislada, es un ecosistema soberano centralizado en el Asistente Be. Se prohíbe la implementación de lógica de búsqueda local fragmentada.
*   **Centralización**: Be provee la entrada táctica (Barra), el **`BeBusquedaMotor`** procesa la intención atómica (Normalización + Debounce), y el Obrero (ViewModel de pantalla) ejecuta el filtrado en la fuente (SQL).
*   **Normalización Universal**: Solo las cadenas procesadas por el motor (sin acentos, minúsculas y sin símbolos) son legales para consultas a la base de datos.

### 16. Ley "Tabla Tablita Tablón" (Soberanía de Persistencia)
La base de datos es el Corazón de la Verdad (SSOT). Se prohíben las estructuras monolíticas y la persistencia de datos pesados.
*   **Normalización Elite**: Preferir múltiples entidades pequeñas relacionadas con `@Relation` o `@ForeignKey` sobre tablas gigantes con decenas de columnas.
*   **Higiene de Archivos**: Prohibido guardar Blobs (imágenes/audios) en Room. Solo se almacenan rutas (String) hacia el almacenamiento interno.
*   **Búsqueda Táctica**: Uso obligatorio de FTS (Full-Text Search) para buscadores de texto largo y `@Index` para coincidencias exactas.

### 17. Ley del Bautizo (Protocolo de Nomenclatura Maverick)
Para garantizar la uniformidad y escalabilidad del ecosistema, todos los archivos deben seguir una nomenclatura estricta basada en su capa y propósito. Queda prohibido el uso de sufijos ambiguos como `UiModel`.

| Capa | Sufijo / Convención | Ejemplo | Propósito |
| :--- | :--- | :--- | :--- |
| **Entidad BD** | `Entity` | `DireccionEntity` | Estructura pura de Room. |
| **Relaciones BD** | `RelacionesBD` | `UsuarioConDireccionesRelacionesBD` | Clases `@Relation` para Room. |
| **DAO** | `Dao` | `DireccionDao` | Interfaz de acceso a datos. |
| **Repositorio** | `Repositorio` | `DireccionRepositorio` | Lógica de sincronización. |
| **Dominio (Modelo)**| `MDominio` | `DireccionMDominio` | Objeto puro de lógica de negocio. |
| **Dominio (Motor)** | `Motor` | `MotorDescubrimientoMav` | Lógica centralizada de procesos. |
| **Dominio (Filtro)** | `Filtro` | `BusquedaFiltro` | Definición de filtros de datos. |
| **Mappers** | `Mappers` | `DireccionMappers` | Conversor entre capas. |
| **Servicio** | `Servicio` | `NotificadorServicio` | Tareas en background. |
| **ViewModel** | `ViewModel` | `DireccionViewModel` | Orquestador de lógica de UI. |
| **DI (Módulos)** | `ModuloDI` | `CoreModuloDI` | Configuración de Hilt. |
| **Pantalla** | `Screen` | `DireccionScreen` | Interfaz (Jetpack Compose). |


### 18. Ley de la Cascada (Soberanía de Scroll & Paging)
Las listas dinámicas son el punto crítico de rendimiento. Se prohíbe el uso de contenedores de scroll estáticos para datos variables.
*   **Eficiencia**: Uso obligatorio de `LazyColumn` (Compose) o `RecyclerView` con `ListAdapter`.
*   **Identidad**: Es mandatorio el uso de `keys` únicas para cada ítem para evitar recomposiciones masivas.
*   **Decisión**: Usar **Paging 3** para catálogos infinitos (>100 ítems) y **Listas Planas** para gestiones acotadas y rápidas (<100 ítems).
*   **Protocolo**: [Protocolo de Armado de Listas Elite](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_ARMADO_LISTAS_ELITE.md).

---

## 🛠️ GUÍA: CREACIÓN DE HERRAMIENTAS TÁCTICAS (BE HUD)

Para añadir nuevas herramientas al Asistente Be, siga este flujo atómico:

1.  **Registro Visual (`BeDictionary.kt`)**: Defina el ID de la acción y sus visuales (`icon`, `label`, `emoji`, `tint`).
2.  **Definición del Contrato**: En la Pantalla (Screen), defina la lista de IDs en el bloque `edicion` o `primarias` del contrato soberano.
3.  **Registro de Soberanía**: Use `navCoordinador.registrarPantalla(beConfig)` dentro de un `DisposableEffect`.
4.  **Escucha de Eventos**: Capture los clicks en el `LaunchedEffect` de la pantalla observando `brainViewModel.actionEvent`.

---

### 🏢 Colección General (Ecosistema Global)
*   [Anatomía de Pantallas Elite](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_ANATOMIA_PANTALLAS_ELITE.md)
*   [El Embudo: Filtrado Elite](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_EL_EMBUDO_FILTRADO_ELITE.md)
*   [Búsqueda con Be: Portal de Inteligencia](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_BUSQUEDA_BE_ELITE.md)
*   [Persistencia Elite: Tabla Tablita Tablón](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_PERSISTENCIA_ELITE_ROOM.md)
*   [Estratificación Atómica V3](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_ESTRATIFICACION_ATOMICA_V3.md)
*   [Protocolo de Identidad Maverick](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOL_IDENTIDAD_MAV.md)
*   [Los 5 Pilares de la Identidad](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_5PILARES_IDENTIDAD.md)
*   [Sistema de Mensajería y Chat](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOL_CHAT_MAV.md)
*   [Gestión de Eventos y Recursos](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOL_EVENTOS_RECURSOS.md)
*   [Índice de Tópicos y Huellas](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_TOPICS_INDEX.md)
*   [Protocolo de Presupuestos SUPREME](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_PRESUPUESTOS_SUPREME.md)
*   [Protocolo de Armado de Listas Elite](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_ARMADO_LISTAS_ELITE.md)
*   [Protocolo de Disponibilidad en Cascada](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_DISPONIBILIDAD_CASCADA.md)
*   [Guía de Sincronización de Red](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/README_TOPICS.md)

### 🔵 Colección App Azul (Cliente)
*   [Manual de Operaciones Asistente Be](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Azul/ManualBeAssistant.md)
*   [Búsqueda con Be: Portal de Inteligencia](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_BUSQUEDA_BE_ELITE.md)
*   [Concursos Públicos: Gestión de Proyectos](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Azul/MANUAL_CONCURSOS_PUBLICOS_ELITE.md)
*   [Búsqueda, Contexto y Herramientas Be](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Azul/Manual_Be_Busqueda_Contexto_Herramientas.md)
*   [Resultados y Descubrimiento](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Azul/PROTOCOLO_BUSQUEDA_RESULTADOS_PRESTADORES.md)
*   [Infraestructura del HUD](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Azul/README_HUD_TOOLS.md)
*   [Analizador de Presupuestos Táctico](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Azul/README_PRESUPUESTOS_AZUL.md)
*   [Reserva de Turnos y Recursos](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Azul/README_RESERVA_DE_TURNOS.md)

### 🟠 Colección App Naranja (Prestador)
*   [Manual de Presupuestos (La Cocina)](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Naranja/README_PRESUPUESTOS_NARANJA.md)
*   [Recursos y Horarios (Gestión Táctica)](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/Naranja/README_RECURSOS_Y_HORARIOS.md)
*   *(Pendiente de migración de protocolos adicionales)*


### Glorasio
*   **Concurso**: (Anteriormente *Tender*) Licitación pública de servicios creada por un cliente.
*   **Huella**: Llave única normalizada (ej: `4000_plomeria`) generada por el Motor de Descubrimiento.
*   **Soberanía**: Estado que define qué perfil (Personal o Empresa) tiene el mando de la App.


---

### 🏢 Colección General (Ecosistema Global)
*   [Protocolo de Nomenclatura Maverick](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_NOMENCLATURA_MAVERICK.md)
*   [Anatomía de Pantallas Elite](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/documentacion/General/PROTOCOLO_ANATOMIA_PANTALLAS_ELITE.md)
