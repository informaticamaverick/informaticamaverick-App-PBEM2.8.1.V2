# Plan de Optimización de Bandeja de Entrada (v2026.ELITE)

Este plan tiene como objetivo optimizar el flujo de datos de la bandeja de entrada de chats, moviendo la lógica de filtrado y resolución de identidad al motor de base de datos (SQL), eliminando redundancias y asegurando que la información (nombres, fotos, rubros, estado online) sea siempre la más reciente (SSOT).

## User Review Required

> [!IMPORTANT]
> Se requiere un incremento en la versión de la base de datos de 79 a 80. Esto activará las nuevas vistas SQL pero no debería causar pérdida de datos si se maneja correctamente con `fallbackToDestructiveMigration` o migraciones automáticas si el esquema lo permite.

## Proposed Changes

### [Módulo :core] - Capa de Datos y Dominio

#### [NEW] [ConversacionResumenSQLView](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/entidades/vistas/ConversacionResumenSQLView.kt)
Creación de una vista SQL que une las conversaciones con las tablas de prestadores, sucursales, empresas y usuarios para resolver identidades en tiempo real.

#### [MODIFY] [ChatDao](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/dao/ChatDao.kt)
Implementación de la búsqueda soberana mediante SQL, eliminando el filtrado en RAM.

#### [MODIFY] [AppDatabase](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/local/AppDatabase.kt)
Incremento de versión a 80 y registro de la nueva vista.

---

### [Módulo :app] - Capa de Presentación

#### [MODIFY] [BeDictionary](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/ui/componentes/be/modelos/BeDictionary.kt)
Centralización de filtros de chat para que sean consumidos por el ViewModel.

#### [MODIFY] [UsuarioListaChatsViewModel](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/src/main/java/com/example/myapplication/viewmodel/chat/UsuarioListaChatsViewModel.kt)
- Eliminación de lógica de filtrado en Kotlin.
- Eliminación de dependencias redundantes de DAOs de identidad.
- Consumo directo de la nueva vista SQL.

---

### [Limpieza y Refactorización]

#### [MODIFY] [ChatMotorSincLocal](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/motores/ChatMotorSincLocal.kt)
Simplificación del impacto de mensajes: ya no necesita buscar rubros o nombres manualmente, la vista SQL se encargará de resolverlo dinámicamente.

## Verification Plan

### Automated Tests
- Verificar la compilación del proyecto con la nueva versión de Room.
- Verificar que las consultas SQL en `ChatDao` sean válidas.

### Manual Verification
- Abrir la bandeja de entrada y verificar que aparezcan los chats.
- Probar los filtros de "Online", "No leídos" y "Rubros".
- Cambiar el nombre de un prestador en el perfil y verificar que se actualice instantáneamente en la lista de chats.
