# Walkthrough - Coordinación Profesional de Señales

He refactorizado la lógica de suscripción a tópicos en la App del Prestador para cumplir con la **Ley #1 (Pantallas Tontas)** y la **Ley #9 (Soberanía de Módulos)**, replicando el exitoso patrón de la App del Usuario pero adaptado a la complejidad profesional.

## Cambios Arquitectónicos

### 1. El Coordinador Profesional (:prestador)
He creado el **`CoordinadorPrestadorMav`** en el módulo del prestador.
- **Simetría**: Es el equivalente funcional al `CoordinadorAccionesMav` de la App del Usuario.
- **Responsabilidad**: Gestionar la entrada y salida de los "Hilos de Red" (FCM Topics) de forma automática.
- **Eficiencia**: Implementa una lógica de diferencial (`topicosActivos - nuevosTopics`) para no re-suscribirse a tópicos que ya tiene activos, optimizando el tráfico de red.

### 2. Sincronización en el ViewModel (SSOT)
He movido la lógica de negocio fuera de la `MainActivity`.
- **`PrestadorPerfilViewModel`**: Ahora actúa como el disparador soberano. Al detectar cambios en el `ecosistemaMaestro` (cambio de dirección base, nueva sucursal o nuevos rubros), le pide al coordinador que sincronice los hilos de red.
- **Cero Fricción**: El proceso ocurre en segundo plano y es invisible para el usuario.

### 3. Limpieza de MainActivity
Siguiendo el protocolo Maverick Elite, he dejado la `MainActivity` como un componente puramente estructural ("Tonto").
- **Acción**: He eliminado el código de suscripción manual que había agregado anteriormente.
- **Resultado**: Mejor mantenibilidad y cumplimiento total del protocolo de arquitectura.

## Verificación de Paridad

| Característica | App Usuario (BeBrain) | App Prestador (PerfilVM) |
| :--- | :--- | :--- |
| **Generador de Llaves** | `MotorDescubrimientoMav` | `MotorDescubrimientoMav` |
| **Manejador de Red** | `CoordinadorAccionesMav` | **`CoordinadorPrestadorMav`** |
| **Activación** | Automática por GPS/Dirección | **Automática por Ecosistema/Soberanía** |

## Resultados
- **Build**: ✅ ÉXITO
- **Logs**: `📡 [SUBSCRIBE] Entrando en: concurso_4000_plomeria` verificado en Logcat.
