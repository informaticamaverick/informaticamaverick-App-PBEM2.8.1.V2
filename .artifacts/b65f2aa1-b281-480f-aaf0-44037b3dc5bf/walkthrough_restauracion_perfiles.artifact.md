# Walkthrough - Restauración y Diferenciación de Perfiles

He restaurado la infraestructura profesional de la pantalla de perfil del prestador y he asegurado que la pantalla de perfil del usuario (cliente) mantenga su propia identidad visual simplificada.

## Cambios Realizados

### 1. Restauración del Perfil del Prestador (:ui-shared)
- **Visibilidad de Métricas**: He verificado y asegurado que `CabeceraPerfilDinamicaMav` muestre correctamente las métricas profesionales (Trabajos, Rating, Reseñas) cuando la cabecera está expandida.
- **Lápices de Edición Tácticos**: He cambiado el color de los iconos de edición (lápices) en las tarjetas de datos, ubicación y capacidades a **Azul Maverick (`0xFF3B82F6`)**. Esto mejora drásticamente su visibilidad sobre el fondo ROG Dark.
- **Corrección de Edición de Dirección**: He corregido un bug en `TarjetaUbicacionBaseMav` donde al intentar editar la dirección se pasaba un objeto vacío en lugar de la dirección actual.

### 2. Refinamiento del Perfil del Usuario (:ui-shared)
- **Cabecera "Lite"**: `CabeceraUsuarioPerfilMav` ahora es una versión limpia que solo muestra el Avatar, Nombre y el distintivo "CLIENTE MAVERICK", eliminando cualquier rastro de métricas profesionales.
- **Modo Lectura Estricto**: He sincronizado los orquestadores para que, cuando un prestador vea el perfil de un cliente (o viceversa), no aparezca ningún botón de edición ni el botón de "Añadir Dirección".
- **Identidad Táctica**: Los lápices de edición en el perfil de usuario ahora también usan el azul Maverick para coherencia estética.

### 3. Sincronización de Flujos
- **App Prestador**: `ProfileScreen` ahora utiliza el orquestador profesional con todos los privilegios de edición activados por defecto.
- **App Cliente**: `PerfilUsuarioScreen` utiliza el orquestador de cliente, manteniendo la limpieza visual solicitada.

## Verificación Visual

> [!IMPORTANT]
> Los lápices de edición ahora son **Azules** y mucho más fáciles de identificar. Aparecen automáticamente si `esMiPropioPerfil` es verdadero.

### Resultados
- **Perfil Prestador**: ✅ Métricas visibles, lápices azules operativos, soberanía (Pager) intacta.
- **Perfil Usuario**: ✅ Diseño limpio, distintivo "Cliente", sin métricas profesionales.
- **Modo Lectura**: ✅ Bloqueo total de edición en vistas ajenas.
