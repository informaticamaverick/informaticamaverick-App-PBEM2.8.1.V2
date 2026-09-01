# Plan de Reparación: Cortocircuito de Identidad y Señalización (v2026.ELITE)

Este plan corrige el fallo de ruteo donde los chats se guardan con el ID genérico `"personal"` en lugar del UID real de Firebase, lo que provoca la desaparición de conversaciones en la bandeja y la inversión de nombres. Además, activa el nodo de señalización para despertar las notificaciones.

## Hallazgos Técnicos

### 1. El Cortocircuito `"personal"`
- **Problema**: Algunos componentes de la UI pasan el String `"personal"` como ID de emisor. El repositorio lo guarda tal cual en Room.
- **Consecuencia**: Cuando la app consulta sus chats usando su UID real (`auth.uid`), Room no devuelve nada porque en la base de datos dice `"personal"`.

### 2. Bloqueo de "Timbre" (RTDB)
- **Problema**: El nodo `inbox_signals` no estaba incluido en las reglas de seguridad anteriores.
- **Consecuencia**: El emisor no puede avisar al receptor de que hay un chat nuevo.

### 3. Duplicidad de Imports
- **Problema**: `DireccionMavEntity.kt` tiene imports duplicados de `@Exclude`, lo que ensucia la compilación.

---

## Proposed Changes

### [Módulo: Core - Entidades e Higiene]

#### [MODIFY] [DireccionMavEntity.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/local/entity/DireccionMavEntity.kt)
- Eliminar imports duplicados y asegurar que `@get:Exclude` funcione correctamente.

#### [MODIFY] [ChatMavRepository.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/data/repository/ChatMavRepository.kt)
- **Sanitización de IDs**: Implementar una función interna para asegurar que si un ID llega como `"personal"`, se resuelva inmediatamente al `auth.uid`.
- **Timbre Táctico**: Asegurar la escritura en `inbox_signals` para despertar al receptor.

### [Módulo: App Azul & Naranja - ViewModels]

#### [MODIFY] [ChatViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/presentation/features/chat/ChatViewModel.kt) y [PrestadorChatViewModel.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/prestador/src/main/java/com/example/myapplication/prestador/viewmodel/chat/PrestadorChatViewModel.kt)
- Resolver el ID local antes de enviarlo al repositorio o inicializar el chat.

---

## 🚨 ACCIÓN REQUERIDA: Reglas Finales de RTDB

Copia este JSON en tu **Realtime Database > Rules**. He añadido el nodo `inbox_signals` para que el "timbre" funcione:

```json
{
  "rules": {
    "chats": {
      "$chatId": {
        ".read": "auth != null",
        ".write": "auth != null",
        ".indexOn": ["fechaEnvio"]
      }
    },
    "inbox_signals": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null"
      }
    },
    "transito_presupuestos": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null"
      }
    },
    "status": {
      "$uid": {
        ".read": "auth != null",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}
```

---

## Verification Plan

### Manual Verification
1. **Bandeja Viva**: Enviar un mensaje. La conversación debe aparecer en la lista con el nombre del **receptor**, no el tuyo.
2. **Notificación**: Verificar que al recibir un mensaje suena el aviso de sistema.
3. **Persistencia**: Borrar caché y verificar que los chats de texto regresan desde la nube (vía Backup).

¿Procedo con el desvío del cortocircuito de identidad?
