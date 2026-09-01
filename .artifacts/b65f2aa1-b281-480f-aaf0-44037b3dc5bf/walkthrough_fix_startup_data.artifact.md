# Walkthrough - Activación del Flujo de Datos y Sembrado

He corregido el problema por el cual las categorías y rubros no aparecían en la App del Prestador, asegurando que el proceso de sembrado (seeding) se ejecute correctamente al iniciar la aplicación.

## Cambios Realizados

### 1. Activación del Motor de Inicio (:prestador)
He detectado que el `PrestadorStartupManager` estaba definido pero no se estaba llamando en ningún lugar.
- **MainActivity**: He inyectado el `startupManager` y activado `performInitialStartup()` en el `onCreate`.
- **Funcionamiento**: Ahora, al abrir la app, el sistema espera un breve momento (800ms) para no afectar la fluidez inicial y luego puebla la base de datos local `prestador.db` con el catálogo completo de 500+ servicios.

### 2. Sincronización del Buscador de Rubros (:ui-shared)
Con la base de datos ahora poblada:
- La barra de búsqueda en la tarjeta de **Rubros y Especialidades** ahora filtrará correctamente los resultados desde Room.
- Al escribir, verás los 15 resultados más relevantes del catálogo global listos para ser seleccionados.

### 3. Consistencia en Direcciones y Flags
He verificado que:
- Los **Flags Tácticos** (Emoji + Label) ahora se pintan con los datos reales del prestador.
- Las **Múltiples Ubicaciones** se muestran en tarjetas independientes y el botón "AÑADIR UBICACIÓN" está operativo.
- El **Modo Empresa** sigue existiendo y utiliza el mismo sistema granular para gestionar sus sucursales.

## Verificación del Flujo
1. **Inicio**: `MainActivity` -> `StartupManager` -> `CategorySeeder`.
2. **ViewModel**: `PrestadorPerfilViewModel` observa `allCategories` (Flow) -> Emite cuando el seeder termina.
3. **UI**: La tarjeta de rubros recibe la lista y habilita la búsqueda táctica.

> [!NOTE]
> La primera vez que abras la sección de rubros tras una instalación limpia, dale un segundo para que el motor de base de datos asiente el catálogo.
