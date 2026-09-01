# 📝 Protocolo de Licitaciones Públicas (v2026.ELITE)

Este documento detalla la arquitectura, el flujo de datos y los componentes técnicos del sistema de **Licitaciones Públicas (Concursos)** dentro del ecosistema Maverick.

## 🏛️ Arquitectura del Módulo
El sistema sigue una arquitectura de tres capas, desacoplando la gestión de presupuestos directos de la creación formal de concursos.

### 1. Inteligencia Atómica (`:core`)
Gestiona la persistencia SSOT (Single Source of Truth) y la sincronización con la nube.

*   **[ConcursoMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/repositorios/ConcursoMavRepository.kt)**:
    *   `crearNuevoConcurso(concurso)`: Orquestador de subida. Sube imágenes a Storage, normaliza el CP, genera etiquetas de descubrimiento y persiste en Firestore (`indice_concursos`) y Room.
    *   `obtenerMercadoPaginado()`: Permite a los prestadores ver licitaciones cercanas mediante un Mediador Remoto.
*   **[ConcursoEntity.kt]**: Modelo de datos unificado que contiene cláusulas tácticas (Visita, Garantía, Pago, ART) y metadatos de ubicación.

### 2. Estado Efímero (`:app`)
Maneja el ciclo de vida del usuario mientras construye la licitación.

*   **[BorradorConcursoViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/budget/BorradorConcursoViewModel.kt)**:
    *   **Ley #8 (Tránsito Efímero)**: Mantiene el estado del Wizard (pasos, fotos temporales) sin afectar la base de datos hasta la publicación final.
    *   **Pilot Mode (Ley #12)**: Envía el contrato HUD al Coordinador para que Be tome el control táctico (Siguiente, Atrás, Publicar).

### 3. UI Táctica (El Armador)
Ubicado en `ui/pantallas/budget/armador/`, sigue el patrón de **Screen Anatomy**.

*   **Caja ([ArmadorConcursoCaja.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/armador/ArmadorConcursoCaja.kt))**: El `BottomSheet` soberano. Reclama soberanía del HUD al abrirse.
*   **Lienzo ([ArmadorConcursoLienzo.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/armador/ArmadorConcursoLienzo.kt))**: Gestiona las transiciones horizontales (`AnimatedContent`) entre los 3 pasos.
*   **Secciones ([SeccionesArmador.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/pantallas/budget/armador/SeccionesArmador.kt))**: Átomos stateless que renderizan el formulario (Solicitante, Detalle, Requisitos).

---

## 🔄 Flujo de Datos de una Licitación

1.  **Iniciación**: El usuario activa la acción `concurso_nuevo`. El `PresupuestosScreen` abre la **Caja** del Armador.
2.  **Construcción**:
    *   El usuario ingresa datos en las **Secciones**.
    *   Las fotos se guardan como URIs locales en el **Borrador (ViewModel)**.
    *   **Be (HUD)** muestra botones de navegación sincronizados con las validaciones del ViewModel.
3.  **Publicación**:
    *   Se dispara `publicarLicitacion()`.
    *   El **Repositorio** sube las imágenes a Firebase Storage.
    *   El objeto final se guarda en Firestore con etiquetas de búsqueda geográficas.
    *   Se limpia el borrador y se cierra la Caja.
4.  **Notificación**: Be sincroniza el ecosistema de red para alertar a prestadores compatibles en la zona.

---

## 🤖 Integración con Be (Pilot Mode)
Durante el proceso de creación, el Asistente Be entra en un modo especial de "Piloto Táctico":
-   **Ojos Ocultos**: Para no distraer del formulario.
-   **Barra Activa**: Muestra botones de flujo (`wizard_next`, `wizard_back`, `wizard_publish`).
-   **Burbuja de Vista Previa**: Aparece una burbuja con la miniatura de la primera imagen cargada al lado de la barra de acciones.

---

## ⚖️ Cumplimiento de Leyes Maverick
-   **Ley #9 (Idioma)**: Todo el código fuente, desde el DAO hasta la UI, utiliza nomenclatura en español.
-   **Ley #11 (Textos Elásticos)**: Uso de `TextCompactoAutoFit` en las etiquetas de ubicación para evitar desbordamientos.
-   **Ley #12 (Contrato HUD)**: La comunicación con el sistema Be se realiza exclusivamente mediante contratos de `ContextoHUD`.
