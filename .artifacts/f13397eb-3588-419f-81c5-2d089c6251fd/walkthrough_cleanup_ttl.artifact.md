# Walkthrough - Limpieza Automática Táctica (Costo Zero)

He implementado la lógica necesaria para delegar la limpieza de datos expirados a Firebase Firestore (TTL), garantizando que las apps se mantengan ligeras y los costos de almacenamiento sean mínimos.

## Cambios Realizados

### 💎 Publicación de Promociones e Historias
- **Cálculo de Expiración**: Se modificó `PrePromotionViewModel.kt` para calcular automáticamente la `fechaExpiracion`:
    - **Historias**: +24 horas exactas.
    - **Promociones**: +7 días.
- **Contexto de Identidad**: Se actualizó `PrestadorDashboardViewModel.kt` y `PrestadorDashboardScreen.kt` para capturar y pasar el contexto completo (CP, IDs, Categorías, Verificación) al momento de publicar. Esto asegura que la promoción se asocie correctamente a la identidad activa (Individual, Empresa o Sucursal).

### ⚖️ Concursos Públicos
- Se verificó que el flujo de creación de concursos en la App Azul ya envía correctamente el campo `fechaFin`.
- Esto permite que Firebase pueda borrar el concurso automáticamente una vez que finalice el periodo de licitación.

## 🛠️ Acción Requerida (Configuración en Consola)

Para que la limpieza automática se active, debes realizar estos pasos **una sola vez** en tu consola de Firebase:

1.  Ve a **Firebase Console** > **Firestore Database**.
2.  Entra en la pestaña **Settings** (Ajustes).
3.  Busca la sección **TTL (Time-to-live)**.
4.  Haz clic en **Añadir Política** y agrega estas dos:
    - **Colección**: `indice_promociones` | **Campo**: `fechaExpiracion`
    - **Colección**: `indice_concursos` | **Campo**: `fechaFin`

## Beneficios
- **Costo Zero**: Firebase no cobra por estas eliminaciones automáticas.
- **Sin Workers**: Las apps no gastan batería ni datos borrando registros viejos.
- **Higiene de Red**: Los usuarios nunca verán historias o concursos "muertos".
