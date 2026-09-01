# Tareas de Reparación: Cortocircuito y Señalización (v2026.ELITE)

- [x] **Fase 1: Higiene de Entidades (Core)**
    - [x] Limpiar imports duplicados en `DireccionMavEntity.kt`.
- [x] **Fase 2: Resolución de Identidad Real (Core)**
    - [x] Sanitizar IDs en `ChatMavRepository.kt`: reemplazar `"personal"` por el UID real antes de guardar en Room o enviar a la nube.
    - [x] Asegurar que `inbox_signals` se escriba con el UID real del receptor.
- [x] **Fase 3: Refactor de ViewModels (App / Prestador)**
    - [x] Resolver el ID local en `ChatViewModel.kt` (Azul).
    - [x] Resolver el ID local en `PrestadorChatViewModel.kt` (Naranja).
- [x] **Fase 4: Verificación**
    - [x] Confirmar que el nombre del prestador aparece en la bandeja azul.
    - [x] Validar que la app naranja recibe chats nuevos sin reiniciar.
