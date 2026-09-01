# Tareas: Control Comercial de Productos v2026

- `[x]` Evolucionar `ChatMotorSincRepositorio.kt`:
    - `[x]` Implementar `solicitarPedidoMav`.
    - `[x]` Configurar actualización de estado `SOLICITADO` en el mensaje original.
    - `[x]` Añadir lógica de mensaje de sistema descriptivo ("Avisando a [Nombre]...").
- `[x]` Actualizar `ChatViewModel.kt` (Cliente):
    - `[x]` Refactorizar `solicitarCompraProducto` para usar el nuevo flujo del repositorio.
- `[x]` Mejorar `BurbujaProducto.kt`:
    - `[x]` Añadir flag `estaSolicitado` al modelo visual.
    - `[x]` Implementar estado deshabilitado del botón (Gris, texto "PEDIDO SOLICITADO", icono check).
- `[x]` Actualizar `OrquestadorBurbujas.kt`:
    - `[x]` Mapear `estadoCita` del mensaje al flag `estaSolicitado`.
- `[x]` Verificar persistencia en Firebase y Room.
- `[x]` Validar compilación multimodular.
