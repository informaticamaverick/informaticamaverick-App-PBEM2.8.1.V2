# Plan de Modernización: Animaciones Morphing, Shimmer y PullRefresh Global

Este plan busca elevar la experiencia de usuario del chat mediante transiciones fluidas, feedback visual de carga moderno y una sincronización forzada bajo demanda.

## User Review Required

- **PullToRefresh Experimental**: Se utilizará `PullToRefreshBox` de Material 3, que actualmente es experimental en la versión de Compose utilizada.
- **Efecto Shimmer vs Morph**: Se implementarán esqueletos (shimmer) para la carga inicial de listas, siguiendo la estética de Google.
- **Sincronización Profunda**: El PullRefresh no solo refrescará la UI, sino que disparará `refreshUserFromRemote()` y `syncWithFirebase()` en los repositorios correspondientes.

---

## Proposed Changes

### Core Enhancements (Repositories & Logic)

#### [ChatListViewModel.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatListViewModel.kt)
- Agregar un estado `isRefreshing`.
- Implementar `refreshAll()` que invoque:
    - `userRepository.refreshUserFromRemote()`
    - `categoryRepository.syncWithFirebase()`
    - Re-activar `chatRepository.startGlobalListening(uid)`.

---

### UI & Animations (Components)

#### [ChatComponents.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/ChatComponents.kt)
- **[NEW] ChatThreadSkeleton**: Componente de Shimmer para la carga de hilos.
- **UnifiedChatListItem**: Optimizar con `Modifier.animateItemPlacement()` para evitar saltos al reordenar.

#### [ChatScreen.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatScreen.kt)
- Envolver `ChatListContent` en un `PullToRefreshBox`.
- Utilizar `AnimatedContent` para la transición entre "Cargando (Skeleton)" y "Lista Real".
- Implementar transiciones de "Morphing" simuladas mediante `AnimatedVisibility` con escalado y fade coordinado al cambiar de perfiles.

#### [ListaElementosMoldeV2.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/ListaElementosMoldeV2.kt)
- Ajustar el `HorizontalPager` para que use `tween` con duraciones que permitan ver el "desplazamiento suave" solicitado.

---

## Verification Plan

### Manual Verification
1.  **Pull to Refresh**: Deslizar hacia abajo en la lista de chats. Verificar que aparece el indicador de Google y que los datos se actualizan (simular un cambio en Firebase).
2.  **Transición de Perfil**: Cambiar entre Personal y Empresa. Verificar que la transición no tenga "tirones" y use el nuevo escalado suave.
3.  **Skeleton Screens**: Forzar un retardo en la carga de chats y verificar que se muestran los "esqueletos" con el efecto shimmer antes de la lista real.
