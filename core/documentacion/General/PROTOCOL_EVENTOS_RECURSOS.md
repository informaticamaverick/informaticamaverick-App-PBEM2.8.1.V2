# 📅 Protocolo Maverick Elite: Eventos y Recursos (v2026.SUPREME)

Este manual define el funcionamiento paso a paso del ecosistema de agendamiento, implementando la arquitectura de **"Disponibilidad por Invitación"** para máxima seguridad y eficiencia de costos.

---

## 🏗️ ARQUITECTURA: LA COCINA VS LA MESA

### 🟠 La Cocina (App Naranja - El Generador)
El prestador es el dueño soberano del tiempo. Toda la lógica pesada ocurre aquí.
1.  **Definición de Reglas**: El profesional configura sus `HorarioEntity` (Cuándo abre), sus `RecursoEntity` (Canchas, Consultorios) y sus `ExcepcionHorariaEntity` (Feriados privados).
2.  **Cálculo de Huecos**: La **`CalculadoraDisponibilidad`** (Motor Privado) cruza las reglas con los turnos ya ocupados y genera opciones libres.
3.  **La Propuesta**: El prestador elige un hueco y lo envía por chat como una "Invitación de Turno".

### 🔵 La Mesa (App Azul - El Consumidor)
El cliente es un invitado a la agenda del profesional. No puede "curiosear" huecos libres en la nube.
1.  **Recepción**: Recibe una burbuja de chat con la propuesta.
2.  **Confirmación**: Al aceptar, el sistema transforma la propuesta en un **`EventoMavEntity`** (Contrato Final).
3.  **Visualización**: El turno aparece en su **`CalendarViewModel`** unificado.

---

## 🚀 PASO A PASO: FUNCIONAMIENTO DE ARCHIVOS

### 1. El Prestador configura su Inventario (App Naranja)
*   **`RecursoEntity`**: El prestador crea una "Cancha 1". Puede usar el interruptor `estaHabilitado` para apagarla si está en reparación.
*   **`HorarioEntity`**: Define que la "Cancha 1" solo funciona de tarde (`Horario_DisponibilidadTurnos`).
*   **`ExcepcionHorariaEntity`**: Marca el próximo lunes como "Cerrado por lluvia" (Dato privado, no se sube a Firebase).

### 2. El Prestador propone un Turno (App Naranja)
*   **`GestionEventosViewModel`**: Pide al repositorio los huecos libres.
*   **`PrestadorCalendarioRepository`**: Orquesta la búsqueda. Llama al **`HorarioMapper`** para tener datos masticados y se los pasa al motor.
*   **`CalculadoraDisponibilidad`**: Realiza la intersección de capas y devuelve los bloques de tiempo.
*   **`BurbujaTurnoLocal`**: Se envía el mensaje por chat con el `idRecurso` vinculado.

### 3. El Cliente acepta el Turno (App Azul)
*   **`MensajeMavEntity`**: El mensaje llega a la Room del cliente.
*   **`CalendarViewModel`**: El cliente pulsa "Aceptar". El VM llama al **`EventoMapper`** para convertir el mensaje de chat en un `EventoMavEntity` oficial.
*   **`EventoMavRepository`**: Persiste el evento en Room y sincroniza el estado con la nube para que el prestador lo vea confirmado.

### 4. Gestión de Agenda Unificada (Ambas Apps)
*   **`EventoUiModel`**: Es el modelo final que ven ambos. El **`EventoMapper`** se encarga de que los datos se vean "masticados" (ej: "Lunes, 10 de Agosto" en lugar de un timestamp largo).
*   **`CalendarViewModel` (Azul)** / **`CalendarioMavViewModel` (Naranja)**: Muestran la lista de eventos filtrada por tipo, categoría o fecha.

---

## 📊 REFERENCIA TÉCNICA DE ARCHIVOS

| Archivo | Capa | Responsabilidad |
| :--- | :--- | :--- |
| **`HorarioEntity`** | Persistencia | Tabla de rangos semanales (Room). |
| **`RecursoEntity`** | Persistencia | Tabla de activos con interruptor ON/OFF. |
| **`ExcepcionHorariaEntity`**| Persistencia | Bloqueos por fecha única (Solo Naranja). |
| **`EventoMavEntity`** | Persistencia | El contrato final de ocupación (Confirmado). |
| **`EventoMapper`** | Transformación | Único traductor de eventos para la UI. |
| **`CalculadoraDisponibilidad`** | Dominio | Motor de cálculo privado (Solo Naranja). |
| **`TurnoUiModel`** | Interfaz | Modelo para la fase de propuesta en chat. |
| **`EventoUiModel`** | Interfaz | Modelo para visualización en agenda. |

---
**Informática Maverick - Arquitectura de Software v2026.SUPREME**
