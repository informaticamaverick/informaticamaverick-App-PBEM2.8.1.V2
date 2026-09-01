# SuperCategory Details Sheet UI Improvements

Adjust the `SuperCategoryDetailsPanelContent` and its integration with `BeAssistant` for better UX and visual consistency as requested by the user.

## Proposed Changes

### [Home Feature]

#### [CategoriaSheetDetallePrestadores.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/CategoriaSheetDetallePrestadores.kt)

- **Repositioning**: Increase `topOffset` to ensure it sits cleanly below the search bar HUD.
- **Visuals**: Update the text color of the helper label.

### [Design System / Components]

#### [SheetEmergenteVertical.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/components/SheetEmergenteVertical.kt)

- Update the color of `helperText` from `AppPalette.ElectricPurple` to a neutral gray (`Color.Gray`).

### [Global Assistant]

#### [HomeScreenViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/home/HomeScreenViewModel.kt)

- Update the `onTriggerAction` handler in `HomeScreenContent` to ensure that when `onTriggerAction` is called with "toggle_search" (from BeAssistant's X button) while a sheet is visible, it dismisses the sheet.

## Verification Plan

### Manual Verification
- **Visual Check**: Open the supercategory sheet and verify it's positioned below the search bar without overlap.
- **Text Color**: Verify "SERVICIOS ENCONTRADOS EN" is now gray.
- **Close Button**: Open the sheet, and tap the "X" button next to the keyboard icon in the Be dock. The sheet should close.
- **Navigation**: Verify that selecting a category from the sheet still navigates correctly.
