# Chat Elite 2026: Architectural Overhaul & Visual Transformation

This plan defines the total reconstruction of the chat experience, aligning with the **Maverick Elite Protocol** (README_CORE.md) and modern **Telegram 2026** design standards.

## The 7 Maverick Laws Applied

1.  **Law #1 (Dumb Screens)**: The Chat Screen will only react to `ChatUiState`. All logic moves to `ChatViewModel`.
2.  **Law #2 (Zero Cost)**: Proactive Room caching for all messages, including multimedia base64 transit before cloud upload.
3.  **Law #3 (Dual Load)**: Paging 3 integration with smart separators.
4.  **Law #4 (Immediacy)**: Instant local reflection of sent messages.
5.  **Law #5 (Background Warm-up)**: Proactive syncing of provider profiles.
6.  **Law #8 (Ephemeral Transit)**: Multimedia messages deleted from Firebase immediately after local persistence confirmation.

---

## Proposed Changes

### [Architectural Cleanup: ViewModel Consolidation]

#### [ChatViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatViewModel.kt)
- **Eliminate Injection Bloat**: Incorporate all logic from `AppointmentViewModel`, `BudgetViewModel`, and `UbicacionClimaViewModel` relevant to chat.
- **Atomic Actions**: Add `acceptBudget(budgetId)`, `rejectBudget(budgetId)`, and `syncIdentityContext()`.
- **Paging 3 Data Stream**: Implement `insertSeparators` to inject `DateHeader` items directly into the flow.

---

### [Visual Transformation: Telegram 2026 Style]

#### [NEW] [AttachmentMenuElite.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/AttachmentMenuElite.kt)
- **Design**: Circular expansion from the "+" button with a **Glassmorphism** background.
- **Motion**: Staggered items with spring-based scale and fade.
- **UX**: Haptic-ready interactions and micro-labels.

#### [ChatConversationScreen.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatConversationScreen.kt)
- **Header Refinement**: Smooth `graphicsLayer` interpolation for profile photo scaling (Telegram style).
- **Date Separators**: Native Paging 3 rendering (no more UI-calculated separators).
- **Clean API**: Single ViewModel injection (`ChatViewModel`).

---

## Verification Plan

### Automated Tests
- Build verification: `gradlew :app:assembleDebug`

### Manual Verification
1.  **Attachment Menu**: Open the "+" menu and verify fluid staggered animation and Glassmorphism effects.
2.  **Date Flow**: Scroll through long history and verify date headers stay sticky and accurate.
3.  **Action Atomic Flow**: Accept a budget directly from the chat bubble and verify instant UI feedback.
4.  **Immediacy**: Send an image and verify it stays in "Sending" state (dimmed) until the Base64 handoff to Firebase is confirmed.
