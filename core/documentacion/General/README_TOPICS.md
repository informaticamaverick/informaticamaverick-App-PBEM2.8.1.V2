# 📡 Protocolo Maverick Elite: Gestión de Hilos de Red (v2026.FINAL)

PBEM utiliza una arquitectura de **Señalización Quirúrgica** basada en tópicos de Firebase Cloud Messaging (FCM) para garantizar que la información crítica (Concursos, Promociones y Alertas de Zona) llegue al destinatario correcto sin latencia.

---

## 🏛️ ARQUITECTURA DE SEÑALIZACIÓN

El sistema se basa en el desacoplamiento total entre el disparador de negocio y el transporte de red, utilizando un motor de normalización centralizado.

### 1. Capas de Responsabilidad
*   **Inteligencia (`MotorDescubrimientoMav`)**: Única fuente de verdad en `:core`. Genera las llaves normalizadas (sin acentos, minúsculas) para asegurar paridad entre el que publica y el que escucha.
*   **Dominio (`TopicRepository`)**: Interfaz que define las operaciones de suscripción.
*   **Datos (`FirebaseTopicRepository`)**: Implementación técnica que interactúa con el SDK de Firebase.
*   **Orquestación (`Coordinadores`)**:
    *   `CoordinadorAccionesMav` (Cliente): Gestiona tópicos de Zona y Promociones.
    *   `CoordinadorPrestadorMav` (Prestador): Gestiona tópicos de Concursos (Licitaciones) y área profesional.

### 2. Estándar de Tópicos (Naming Elite)
Todos los nombres se generan mediante el `MotorDescubrimientoMav` siguiendo estas estructuras:

| Tipo | Formato | Ejemplo |
| :--- | :--- | :--- |
| **Zona** | `zona_{cp}` | `zona_4000` |
| **Concurso** | `concurso_{cp}_{rubro}` | `concurso_4000_plomeria` |
| **Promoción** | `promo_{cp}_{rubro}` | `promo_4000_peluqueria` |

---

## ⚙️ FUNCIONAMIENTO TÁCTICO

### Ciclo de Vida Reactivo (Prestador)
1. **Detección**: El `PrestadorPerfilViewModel` observa el `ecosistemaMaestro` (perfil humano + empresas).
2. **Sincronización**: Al detectar un cambio en rubros o direcciones, invoca al `CoordinadorPrestadorMav`.
3. **Optimización**: El coordinador calcula el diferencial de tópicos. Solo se suscribe a los nuevos y abandona los que ya no son relevantes (**Metodología Costo Zero**).
4. **Seguridad**: Uso de `Mutex` para garantizar la integridad de la tabla de tópicos activos durante transiciones rápidas de red.

### Normalización Obligatoria
Toda señal de red debe pasar por el proceso de normalización del motor:
*   `normalizeForTopic()`: Asegura caracteres válidos para red.
*   `normalizeFull()`: Garantiza que "Médico" y "medico" se encuentren siempre.

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
