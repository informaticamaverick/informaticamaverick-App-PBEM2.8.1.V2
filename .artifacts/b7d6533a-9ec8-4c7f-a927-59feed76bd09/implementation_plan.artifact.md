# Plan de Implementación - Rediseño de Indicadores de Estado Externos

Este plan detalla la reestructuración de los indicadores de estado fuera de las burbujas de chat para lograr una estética de "Burbuja de Estado + Etiqueta" alineada con la posición del mensaje.

## User Review Required

> [!IMPORTANT]
> - **Nuevo Formato**: El estado ya no será un badge rectangular, sino un **Círculo con Emoji** acompañado de una **Etiqueta de Texto**.
> - **Alineación Dinámica**:
>     *   **Enviados (Míos)**: La etiqueta de texto aparecerá a la **izquierda** del círculo con emoji (`[Texto] [Círculo] [Burbuja]`).
>     *   **Recibidos (Cliente)**: La etiqueta de texto aparecerá a la **derecha** del círculo con emoji (`[Burbuja] [Círculo] [Texto]`).
> - **Emojis Estándar**: Confirmado (✅), Pendiente (⏳), Cancelado (❌).

## Cambios Propuestos

### Módulo: ui-shared

#### [MODIFICAR] [BurbujaTurnoLocal.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/ui/components/chat/BurbujaTurnoLocal.kt) y [BurbujaVisitaTecnica.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/src/main/java/com/example/myapplication/uishared/ui/components/chat/BurbujaVisitaTecnica.kt)

1.  **Refactor de `BadgeEstado`**:
    *   Actualizar la función para devolver el **Emoji** y el **Color** según el estado.
2.  **Actualización de `contenidoExtra`**:
    *   Implementar una lógica de `Row` dentro del slot externo que posicione el texto y el círculo según `esMio`.
    *   Diseñar el círculo de estado con el mismo tamaño y estilo que el botón de herramientas (36dp, borde de cristal).
    *   Crear la etiqueta de texto con tipografía compacta (`8.sp`, `Black`) y fondo sutil.

## Plan de Verificación

### Pruebas Visuales
- Validar en los Previews que la etiqueta cambie de lado correctamente según si el mensaje es enviado o recibido.
- Verificar que el círculo de estado y el de herramientas estén alineados verticalmente.

### Compilación
- Ejecutar `./gradlew :ui-shared:compileDebugKotlin`.
