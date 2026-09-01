# Walkthrough - Auditoría Táctica de Sincronización (Logs)

He añadido logs estratégicos en ambas aplicaciones para verificar que las etiquetas de búsqueda y suscripción coincidan exactamente antes de proceder con la creación de los índices en Firestore.

## Logs Añadidos

### 1. App Azul (Cliente)
- **Acción**: Al subir un nuevo concurso.
- **Log**: `CONCURSO_SUBIDA: 🔍 App Azul subiendo etiquetas: [C_4000_..., Z_4000, ...]`

### 2. App Naranja (Prestador)
- **Acción**: Al entrar al Mercado de Concursos.
- **Log**: `MERCADO_CONSULTA: 📡 App Naranja buscando etiquetas: [C_4000_...]`
- **Acción**: Al sincronizar suscripciones de red.
- **Log**: `RED_SUSCRIPCION: 🛰️ Matriz de tópicos generada: [C_4000_..., Z_4000, ...]`

### 3. Motor de Datos (Firestore Mediators)
- **Logs**: `CONCURSO_FIRESTORE` y `PROMO_FIRESTORE`.
- Muestran exactamente qué etiquetas se están enviando en la consulta real a Firestore.

## Verificación de Flujo

1.  Abre la **App Azul** y crea un concurso. Anota las etiquetas del log `CONCURSO_SUBIDA`.
2.  Abre la **App Naranja** y entra a la pestaña "Concursos". Verifica que el log `MERCADO_CONSULTA` tenga etiquetas idénticas.
3.  Si las etiquetas coinciden (por ejemplo, ambas usan `C_4000_informatica_tecnico`), habremos confirmado que el canal de comunicación es perfecto.

## Siguiente Paso
Una vez confirmada la coincidencia en los logs, puedes proceder a hacer clic en los enlaces de `FAILED_PRECONDITION` en el Logcat para crear los índices finales en la consola de Firebase.
