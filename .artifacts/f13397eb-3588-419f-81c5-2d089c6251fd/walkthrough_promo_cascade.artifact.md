# Walkthrough - Descubrimiento de Promociones en Cascada y Suscripción Inteligente

He implementado la lógica de **Descubrimiento en Cascada** para las historias y la **Suscripción Jerárquica** automática cuando un usuario busca un rubro en la App Azul.

## Cambios Realizados

### 💎 App Naranja (Prestador)
- **Logs de Auditoría**: Se añadió el log `PROMO_SUBIDA` en `PrestadorPromocionRepository.kt` para ver qué etiquetas se están publicando realmente en Firestore.

### 🌊 App Azul (Cliente)
- **Suscripción en Caliente**: Ahora, cuando buscas una categoría (ej: "Plomería"), la app te suscribe automáticamente no solo a ese rubro, sino también a su **Supercategoría** (ej: "Hogar") y a la **Zona** para promociones y concursos.
    - **Log**: `RED_SUSCRIPCION_PROMO` muestra los canales a los que te unes.
- **Feed de Historias en Cascada**: El feed de historias ahora es inteligente (estilo Instagram).
    - **Nivel 1**: Busca historias del rubro exacto que te interesa.
    - **Nivel 2**: Si no hay, busca historias de rubros afines (Supercategoría).
    - **Nivel 3**: Si sigue vacío, te muestra historias generales de tu zona.
    - **Log**: `PROMO_CASCADA` indica el nivel de búsqueda activo.

### 🛰️ Módulo Core (Shared)
- **Repositorio de Promociones**: Nueva función `getStoriesCascada` que orquesta la lógica de niveles de precisión.
- **Motor de Descubrimiento**: Logs mejorados con `[HUELLAS_JERARQUICAS]` para rastrear la generación de etiquetas en ambos lados.

## Verificación de Resultados

### Flujo de Auditoría
1. **Prestador**: Publica una historia. Verifica en el Logcat: `PROMO_SUBIDA: ... [O_4000_hogar_plomeria, O_4000_plomeria, Z_4000]`.
2. **Cliente**: Busca "Plomería". Verifica en el Logcat: `RED_SUSCRIPCION_PROMO: ... [O_4000_hogar_plomeria, O_4000_plomeria, Z_4000]`.
3. **Coincidencia**: Si los tags son idénticos, la sincronización es exitosa.

### Cascada en Acción
- Si un plomero sube una historia, la verás primero.
- Si ningún plomero subió nada, pero un electricista (del mismo grupo "Hogar") sí lo hizo, su historia aparecerá en el feed para que nunca veas la pantalla vacía.
