# Plan de Estandarización Maverick Elite: Integridad y Trazabilidad v2026

Este plan tiene como objetivo blindar la arquitectura del ecosistema Maverick mediante el cumplimiento estricto de las 10 Leyes del Core, eliminando cualquier rastro de bilingüismo técnico y asegurando una trazabilidad quirúrgica en cada bit de información.

## User Review Required

> [!IMPORTANT]
> - Se eliminará el soporte bilingüe (español/inglés) en la red. A partir de ahora, todas las llaves en Firebase serán exclusivamente en **Español** para alinearse con las Entities de Room.
> - Se auditará cada archivo para incluir la cabecera obligatoria de la **Ley de Trazabilidad Hormiga** (Título, Propósito, Funcionamiento, Relación).
> - Todos los logs del sistema serán estandarizados con etiquetas entre corchetes.

## Proposed Changes

### 1. Unificación Radical al Español (Ley #9)
- **`MapeadorMensajesMav.kt`**: Eliminar todos los fallbacks a llaves en inglés (`senderId`, `chatId`, `content`). Si el dato no viene en español, se considerará inválido. Esto garantiza que no haya "fugas de idioma" en el SSOT.
- **`ChatMavRepository.kt`**: Asegurar que el envío de mensajes utilice exclusivamente las llaves de diccionario en español definidas en el motor.

### 2. Implementación de Trazabilidad Hormiga (Ley #7)
- **Cabeceras Explicativas**: Añadir o corregir los bloques de comentario iniciales en:
    - `MotorDescubrimientoMav.kt`
    - `ChatMavRepository.kt`
    - `BusquedaRemoteMediator.kt`
    - `MapeadorMensajesMav.kt`
- **Etiquetado de Logs**: Actualizar las llamadas a Log para usar el estándar Elite:
    - `[CHAT_ENVIO_TEXTO]`
    - `[CHAT_LECTURA_NUBE]`
    - `[SYNC_SHALLOW_PAGING]`

### 3. Documentación Maestra Detallada
- **`PROTOCOLO_BUSQUEDA_RESULTADOS_PRESTADORES.md`**: Manual técnico exhaustivo sobre la `DatabaseView` y el flujo de estados.
- **`PROTOCOLO_TOPICS_INDEX.md`**: Guía determinística sobre la generación de huellas y la segmentación de colecciones Firestore.

### 4. Ajuste Táctico de Identidad (:prestador)
- Corregir en `ChatConversationScreen.kt` la captura del `idPropietarioReceptor` para asegurar que las notificaciones push se ruteen al humano correcto en comunicaciones multiperfil.

## Verification Plan

### Manual Verification
- **Auditoría de Código**: Verificar manualmente que no existan strings en inglés en la capa de datos.
- **Logcat**: Comprobar que los tags de log sigan el formato `[TAG]`.
- **Comunicación**: Validar un ciclo completo de mensaje (Envío -> Nube -> Room -> UI) entre ambas apps.
