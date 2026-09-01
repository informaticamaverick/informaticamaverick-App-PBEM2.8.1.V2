# Refactoring Be Assistant & Search Performance

Optimize the search engine responsiveness, remove dead code, and document the complete Be Assistant mechanics.

## Proposed Changes

### [Core Search Logic]
Optimize the search pipeline to ensure immediate feedback and proactive data loading.

#### [AppActionCoordinator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/AppActionCoordinator.kt)
- Reduce `debouncedNormalizedSearchQuery` duration from 300ms to 150ms for better perceived performance.

#### [CategoryViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/CategoryViewModel.kt)
- Fix the lazy loading trigger to activate as soon as the search UI is opened (Focus-based) instead of waiting for the first character.
- Clean up commented-out code.

---

### [Be Assistant Refactoring]
Clean up redundant files and improve context-aware behaviors.

#### [BeAssistantViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/BeAssistantViewModel.kt)
- Ensure tools and hints are strictly context-aware.
- Implement proactive loading trigger when `setSearchActive(true)` is called.

#### [DELETE] [BeConversacionViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/BeConversacionViewModel.kt)
- Remove redundant old version.

#### [DELETE] [AssistantToolsDelegate.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/global/AssistantToolsDelegate.kt)
- Remove commented-out dead code.

---

### [Documentation]

#### [NEW] [ManualBeAssistant.md](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/docs/ManualBeAssistant.md)
- Create a comprehensive guide covering:
    - Animation Logic (`BeAssistant.kt`)
    - Physics and Delegates (`BeAssistantStateDelegate.kt`)
    - Contextual Search & Conversations.
    - Assistant Tools and Mode management.

## Verification Plan

### Automated Tests
- N/A (UI and State behavior focused)

### Manual Verification
1. **Search Speed**: Verify that categories appear instantly when opening search and typing.
2. **Contextual Tools**: Navigate between Home, Chat, and Budgets to ensure Be's tools and hints change appropriately.
3. **Logcat Audit**: Check `[SEARCH_WARMUP]` and `[FILTER_RESULTS]` logs to confirm proactive loading.
4. **UI Stability**: Ensure the `GoogleMorphingLoader` doesn't flicker or stay stuck during search transitions.
