# 📝 Manual Técnico: Licitaciones Públicas (v2026.ELITE)

## 📌 Descripción General
El sistema de **Licitaciones Públicas** permite a los usuarios (clientes) convocar a múltiples prestadores de servicios de forma simultánea mediante un proceso de 3 pasos (Wizard). Este sistema está diseñado bajo los principios de **Inmediatez**, **Desacoplamiento** y **Soberanía Táctica**.

---

## 🏗️ Estructura de Archivos

### 1. Capa de Datos (`:core`)
| Archivo | Función |
| :--- | :--- |
| `ConcursoMavRepository.kt` | Dueño del ciclo de vida del concurso. Gestiona Firebase Storage y Firestore. |
| `ConcursoEntity.kt` | Entidad de Room/Kotlin que define el modelo de datos de la licitación. |
| `PresupuestoDao.kt` | Operaciones CRUD para concursos en la base de datos local (Room). |

### 2. Capa de Negocio y Estado (`:app`)
| Archivo | Función |
| :--- | :--- |
| `BorradorConcursoViewModel.kt` | Mantiene el estado efímero del Wizard y orquesta la integración con Be. |
| `PresupuestoViewModel.kt` | Orquesta la vista de lista de concursos y presupuestos recibidos. |

### 3. Capa de Interfaz (UI)
| Archivo | Función |
| :--- | :--- |
| `ArmadorConcursoCaja.kt` | Contenedor BottomSheet que inicia el flujo. |
| `ArmadorConcursoLienzo.kt` | Controlador de animaciones y pasos del Wizard. |
| `SeccionesArmador.kt` | Definición de los pasos 1, 2 y 3 del formulario. |
| `PiezasArmador.kt` | Componentes reutilizables (Campos de texto, indicadores, switches). |

---

## ⚡ Flujo de Datos Técnico

### Paso 1: Iniciación del Borrador
Cuando el usuario pulsa en "Nuevo Concurso", se instancia el `BorradorConcursoViewModel`. Este hereda la **Dirección Activa** del `CoordinadorAccionesMav`.

### Paso 2: Validación y Navegación Táctica
A medida que el usuario completa campos, el ViewModel actualiza el **Contrato HUD**. El sistema **Be** recibe este contrato y:
- Habilita/Deshabilita el botón "Siguiente" en su barra de herramientas.
- Muestra una burbuja de vista previa si hay imágenes cargadas.

### Paso 3: Persistencia y Difusión
Al ejecutar `publicarLicitacion()`:
1. **Local-First**: Se guarda el borrador en la DB local para feedback instantáneo.
2. **Multimedia**: Se suben las imágenes a `concursos/{idConcurso}/...` en Firebase Storage.
3. **Global-Sync**: Se escribe en Firestore con etiquetas geográficas normalizadas (CP).
4. **Sincronización de Red**: Be activa el motor de descubrimiento para notificar a prestadores que coincidan con el rubro y la zona.

---

## 🤖 Integración con Be (Pilot Mode)
El sistema Be asume la navegación mediante la **Ley #12**. El ViewModel no le dice a Be "muestra un botón", le dice "estoy en este estado", y Be decide visualmente qué botones mostrar (`wizard_next`, `wizard_back`, `wizard_publish`).

---

## ⚖️ Leyes Maverick Aplicadas
- **Ley #9**: Nomenclatura 100% en español.
- **Ley #11**: Textos adaptativos para diferentes densidades de pantalla.
- **Ley #12**: El contenido (ViewModel) es soberano sobre su presentación en el HUD.
