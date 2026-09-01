# Auditoría de Suscripción a Topics (FCM) - Historias y Promociones

Este documento detalla el flujo actual de suscripción a temas (Topics) en ambas aplicaciones para garantizar que el contenido publicitario llegue a la audiencia correcta basándose en su ubicación geográfica.

## 1. App del Usuario (Cliente)

### Mecanismo de Suscripción
La suscripción está centralizada en el `AppActionCoordinator` (Cerebro Global).

- **Clase**: [AppActionCoordinator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/AppActionCoordinator.kt)
- **Activador**: Cada vez que el `activeAddress` (dirección activa) cambia. Esto sucede cuando el usuario:
    1. Selecciona una dirección guardada.
    2. Cambia su perfil (lo que cambia su dirección predeterminada).
    3. El GPS actualiza su ubicación.

### Lógica de Topics
Cuando se detecta un cambio de Código Postal (CP):
1. **Licitaciones**: Se suscribe a `zona_{CP}` (ej: `zona_T4000`).
2. **Promociones/Historias**: Se suscribe a `promos_{CP}` (ej: `promos_T4000`).

### Auditoría de Código
```kotlin
// AppActionCoordinator.kt
private fun initTopicAutomation() {
    scope.launch {
        activeAddress.map { it?.codigoPostal }.distinctUntilChanged().collect { cp ->
            if (cp != null) {
                syncZoneTopic(cp) // Gestiona ambos topics
            }
        }
    }
}
```

---

## 2. App del Prestador

### Mecanismo de Suscripción
La suscripción se realiza de forma proactiva al iniciar la app o al actualizar el perfil.

- **Clase**: [MainActivity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/MainActivity.kt)
- **Clase Lógica**: [EditProfileViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/profile/EditProfileViewModel.kt)

### Lógica de Topics
El prestador se suscribe a topics basados en la combinación de sus **zonas de cobertura** y sus **categorías de servicio**.

1. **Estructura**: `tender_{CP}_{CATEGORIA}` (ej: `tender_T4000_plomero`).
2. **Multi-Zona**: Si tiene varias sucursales o direcciones, se suscribe a todas las combinaciones.
3. **Propósito**: Recibir avisos de licitaciones (oportunidades de trabajo) creadas por usuarios en esas zonas/rubros.

### Auditoría de Código
```kotlin
// MainActivity.kt (App Prestador)
lifecycleScope.launch {
    editProfileViewModel.profileState.collectLatest { state ->
        if (state is ProfileState.Success) {
            val provider = state.provider
            val allCp = (...) // Recolecta CPs de sucursales y personal
            val allCats = (...) // Recolecta categorías de empresas y personal

            allCp.forEach { cp ->
                editProfileViewModel.syncTopics(cp, allCats, provider.isSubscribed)
            }
        }
    }
}
```

---

## 3. Matriz de Flujo de Datos

| Evento | Emisor | Topic | Receptor |
| :--- | :--- | :--- | :--- |
| **Nueva Licitación** | App Usuario | `tender_{CP}_{CAT}` | App Prestador (Suscrito por perfil) |
| **Nueva Historia/Promo** | App Prestador | `promos_{CP}` | App Usuario (Suscrito por zona activa) |

## 4. Hallazgos y Observaciones

1. **Consistencia Geográfica**: Ambos usan el Código Postal (CP) normalizado como clave de ruteo.
2. **Costo Cero**: Las suscripciones se limpian (`unsubscribeFromTopic`) cuando el CP cambia, evitando recibir basura de zonas viejas.
3. **Multiperfil**:
    - En la **App Usuario**, si el usuario cambia a su perfil de "Empresa" y esa empresa tiene otra dirección, el `AppActionCoordinator` automáticamente cambia las suscripciones a la nueva zona.
    - En la **App Prestador**, se suscriben todas las zonas de todas las empresas vinculadas al prestador simultáneamente.

## 5. Recomendación de "Grandes Ligas"
Para que funcione como una app de primer nivel, se ha implementado en la auditoría:
- [x] **Diferenciación de Topics**: Separar `promos_` de `zona_` (licitaciones).
- [x] **Limpieza Automática**: Desuscripción proactiva al cambiar de contexto.
- [x] **Persistencia Local**: Las promos se guardan en Room para acceso offline inmediato tras recibir el push.
