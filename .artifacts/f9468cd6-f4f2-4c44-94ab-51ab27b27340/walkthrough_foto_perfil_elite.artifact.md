# Walkthrough: Identidad Visual Premium y Gestión de Fotos (v2026.ELITE)

Se ha implementado el sistema de actualización de fotos de perfil con un flujo táctico que permite capturas directas desde la cámara o selección de archivos, todo integrado bajo el paraguas del **Borrador Soberano**.

## Cambios Realizados

### 📸 Cabecera con Edición Táctica
- **Lápiz de Edición**: Se añadió un icono de lápiz flotante sobre el avatar en `CabeceraPerfilDinamicaMav`. Este botón solo es visible cuando el prestador está en **Modo Edición**, manteniendo la estética limpia en la vista pública.
- **Micro-interacción**: El botón utiliza el color naranja Maverick y cuenta con una elevación sutil para destacar sobre cualquier fondo.

### 📂 Menú Multi-Origen
- **Modal de Selección**: Al tocar el avatar en edición, se despliega un `ModalBottomSheet` premium con dos opciones:
    - **Cámara**: Abre la interfaz de cámara nativa para capturar una foto instantánea.
    - **Archivos**: Abre el gestor de archivos de Android para elegir una imagen guardada.
- **Diseño Inmersivo**: El menú sigue la línea visual de la App Naranja, con fondos oscuros y acentos de color coherentes.

### 🧠 Integración con el Borrador y Compresión
- **Procesamiento Elite**: Se utilizan las funciones de `ImageUtils` para comprimir las fotos en formato WebP de alta eficiencia, generando automáticamente una miniatura (Thumbnail) para cargas ultra-rápidas.
- **Aislamiento en RAM**: Las fotos nuevas **no se suben a Firebase inmediatamente**. Se guardan localmente en la caché de la App y se vinculan al `GestorBorradorPerfilPrestador`.
- **Persistencia Atómica**: Solo cuando el usuario presiona "GUARDAR Y SALIR", las fotos se consolidan en Room y se encola su subida a la nube.

## Verificación de Experiencia

1.  **Agilidad**: La captura de cámara y el guardado en el borrador son instantáneos, sin esperas de red.
2.  **Seguridad**: Si el usuario cambia la foto pero luego presiona **"DESCARTAR"**, el sistema restaura automáticamente la foto original y limpia los archivos temporales.
3.  **Higiene**: Se cumple estrictamente con la **Ley #9**, manteniendo todos los nombres de funciones y variables en Español.

> [!TIP]
> Este flujo de "Borrador de Fotos" es el estándar de las apps de Grandes Ligas, permitiendo al usuario experimentar con su imagen de perfil antes de hacer el cambio definitivo y público.
