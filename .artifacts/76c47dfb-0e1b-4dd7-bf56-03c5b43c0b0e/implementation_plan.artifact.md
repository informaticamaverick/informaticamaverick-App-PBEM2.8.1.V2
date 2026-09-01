# Implementation Plan - Professional Refactoring of NuevoTurnoLocalSheet

This plan addresses the render issues in the Compose Preview and implements a professional UI overhaul for the local appointment management sheet.

## Goal
- Fix the `GestionEventosViewModel` instantiation error in Preview by refactoring to a stateless pattern.
- Redesign the UI to clearly separate Local and Client data.
- Implement a Date Picker for selection (today onwards).
- Enhance resource and time slot selection with professional styling and feedback.

## User Review Required
> [!IMPORTANT]
> The new UI will use Material 3 `DatePicker`. Ensure the project dependencies support `ExperimentalMaterial3Api` (already present in the file).

## Proposed Changes

### [prestador]

#### [MODIFY] [NuevoTurnoLocalSheet.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/ui/pantallas/chat/componentes/NuevoTurnoLocalSheet.kt)

- **Stateless Refactoring**: Split `NuevoTurnoLocalSheet` into:
    - `NuevoTurnoLocalSheet`: Stateful wrapper handling ViewModel integration.
    - `NuevoTurnoLocalSheetContent`: Pure UI component for rendering and previewing.
- **UI Sections**:
    - **Header**: Distinct cards for "Local" (showing `nombrePrestador` and full address) and "Cliente" (`nombreCliente`).
    - **Selection Flow**:
        1. **Fecha**: Interactive card that opens a `DatePickerDialog`. Validates selectable dates (>= today).
        2. **Recurso**: Dedicated section with resource icons/details.
        3. **Horarios**: Grid of blocks. Occupied blocks are grayed out/disabled. Selected block is highlighted with Elite theme colors.
- **State Management**: Use `rememberDatePickerState` for date selection.
- **Elite Styling**: Apply `SharedPalette` colors and Black/ExtraBold typography for a professional look.

## Verification Plan

### Manual Verification
- **Preview**: Open `PreviewNuevoTurnoLocal` in Android Studio and verify it renders without errors.
- **Interactions**:
    - Tap "Fecha" to see the Date Picker.
    - Select different resources to trigger availability updates.
    - Select a time slot and verify the "Enviar" button enables.
    - Verify occupied slots cannot be clicked.
