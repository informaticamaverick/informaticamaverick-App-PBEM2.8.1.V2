# Fix Emoji Display in ResultBusquedaCategoriaScreen Header

The goal is to ensure the header in `ResultBusquedaCategoriaScreen.kt` correctly displays the emoji and color associated with the selected category or service (including SuperCategories). Currently, it incorrectly sources categories from `beViewModel.allCategories`, which is often empty.

## User Review Required

> [!NOTE]
> I will be changing the source of truth for categories from `BeBrainViewModel` to `CategoryViewModel` in this screen, as per the "Golden Rules" of the project.

## Proposed Changes

### Presentation Layer

#### [ResultBusquedaCategoriaScreen.kt](file:///C:/Users/maxin/StudioProjects/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/client/ResultBusquedaCategoriaScreen.kt)

- **Source Correct Data**: Replace `beViewModel.allCategories` with `categoryViewModel.allCategories` and add `categoryViewModel.superCategories`.
- **Context Sync**: Call `beViewModel.onRouteChanged("result_busqueda/$categoryName")` in a `LaunchedEffect`.
- **Refine Logic**: Update `ResultBusquedaCategoriaContent` to search for `categoryName` in both categories and supercategories to determine the correct emoji and accent color.
- **Update Preview**: Ensure the preview also uses representative data for testing.

```kotlin
// Example of the refined matching logic in ResultBusquedaCategoriaContent:
val selectedCategory = remember(allCategories, categoryName) {
    allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
}
val selectedSuperCategory = remember(superCategories, categoryName) {
    superCategories.find { it.title.equals(categoryName, ignoreCase = true) }
}
val finalEmoji = remember(selectedCategory, selectedSuperCategory) {
    selectedCategory?.icon ?: selectedSuperCategory?.icon ?: "🔍"
}
val categoryColor = remember(selectedCategory, selectedSuperCategory) {
    when {
        selectedCategory != null -> Color(CategoryVisuals.getColorFor(selectedCategory.superCategory))
        selectedSuperCategory != null -> Color(selectedSuperCategory.color)
        else -> MaverickBlue
    }
}
```

## Verification Plan

### Automated Tests
- Since this is a UI logic change, I will verify it using a Compose Preview.
- Command: `gradlew app:assembleDebug` to ensure no regressions.

### Manual Verification
- Render the `ResultBusquedaCategoriaScreenPreview` using `render_compose_preview` to verify the emoji and color are correctly picked up from the sample data.
- Verify that both a leaf category (e.g., "Informatica") and potentially a supercategory (if applicable in navigation) would show their respective icons.
