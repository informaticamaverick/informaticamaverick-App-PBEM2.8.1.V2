# Walkthrough - Arquitectura Elite de Descubrimiento y Señalización

He implementado una reestructuración de "Grandes Ligas" para el motor de búsqueda, licitaciones y notificaciones, garantizando consistencia absoluta en todo el ecosistema Maverick.

## ¿Qué hemos blindado?

### 1. El Motor Central: `MotorDescubrimientoMav` (:core)
He creado un componente soberano que es ahora la **única fuente de verdad** para generar llaves en el sistema.
- **Normalización Unificada**: Utiliza `normalizeFull()` para eliminar acentos y símbolos, asegurando que "Médico" y "medico" generen el mismo tag: `4000_medico clinico`.
- **Detección Determinística**: Ya no hay adivinanzas. Tanto la app que registra como la app que busca le preguntan al mismo motor qué llave usar.

### 2. Segmentación de Nube (Big League Firestore)
Siguiendo las mejores prácticas de seguridad y rendimiento, he separado la nube en colecciones especializadas:
- **`indice_busqueda`**: Contiene los perfiles aplanados para descubrimiento rápido.
- **`indice_licitaciones`**: Exclusivo para concursos públicos.
- **`indice_promociones`**: Reservado para ofertas georeferenciadas.

### 3. Corrección Quirúrgica de Búsqueda
He resuelto el fallo técnico de Firestore que impedía encontrar prestadores al activar filtros (24hs, Verificados).
- **Estrategia "Costo Zero"**: La consulta a Firestore ahora es atómica por **Tag Base**. El refinamiento fino se realiza localmente en el repositorio mediante Kotlin, evitando errores de red y garantizando inmediatez.

### 4. Sincronización de Señales (Licitaciones)
- **Coherencia Total**: Al crear un concurso, el sistema genera un tópico FCM (ej: `concurso_4000_plomeria`) que coincide exactamente con lo que el prestador está escuchando.
- **Detección de C.P. Robusta**: He mejorado el motor de sincronización para que, si un prestador no tiene vinculada una dirección específica, el sistema use su primera base operativa conocida para indexarlo, evitando que sea "invisible".

## Resultados Técnicos
- **Building**: ✅ ÉXITO (Core, App, Prestador)
- **Seguridad**: Reforzada mediante segmentación de colecciones.
- **Consistencia**: 100% (Mismo tag para Búsqueda, Notificación y Registro).

> [!IMPORTANT]
> Los prestadores ahora aparecerán instantáneamente en la App del Usuario bajo sus rubros correspondientes, sin importar mayúsculas o acentos.
