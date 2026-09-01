# Walkthrough - Historias en Caliente y Liberación de Índices

He implementado la sincronización proactiva de promociones e historias y he habilitado los logs completos para que Firestore muestre los enlaces de creación de índices.

## Cambios Realizados

### 🛰️ Sincronización Proactiva (Hot Loading)
- **PromoViewModel.kt**: Se añadió un disparador automático (`viewModelScope.launch`) dentro de los flujos de `stories` y `promotions`.
    - **Beneficio**: Ahora, al detectar el código postal del usuario o cambiar de categoría, la app azul solicita inmediatamente las historias a la nube en lugar de esperar una acción manual. Esto garantiza que Room siempre tenga datos frescos para mostrar.

### 🔍 Visibilidad de Índices en Logcat
- **Remote Mediators**: He modificado `PromocionRemoteMediator.kt`, `ConcursoRemoteMediator.kt` y `BusquedaRemoteMediator.kt`.
    - **Antes**: Solo imprimían `e.message`, lo cual a veces omitía el link de Firebase.
    - **Ahora**: Imprimen la excepción completa (`e`). Esto obligará a Firestore a imprimir el link azul subrayado en el Logcat cuando falte un índice.

### 📈 Auditoría de Cascada de Historias
- **PromocionRepository.kt**: Se añadieron logs con el tag `PROMO_CASCADA` para cada nivel de búsqueda:
    - `🎯 Nivel 1 [RUBRO]`
    - `📂 Nivel 2 [SUPERCAT]`
    - `📍 Nivel 3 [ZONA]`

## Verificación de Resultados

### Pruebas de Sincronización
1. Abre la **App Azul**.
2. Deberías ver en el Logcat el tag `PROMO_OBRERO` y luego `PROMO_CASCADA` indicando que se disparó la descarga desde la nube.
3. Si la app intenta ordenar y falta un índice, ahora verás el link de Firebase en rojo/naranja en tu Logcat.

## 🛠️ Acción Requerida
Si todavía no ves las promociones, revisa el Logcat. Si aparece un error `FAILED_PRECONDITION`, busca el link que ahora sí debería ser visible, haz clic en él y crea el índice compuesto en la consola de Firebase.
