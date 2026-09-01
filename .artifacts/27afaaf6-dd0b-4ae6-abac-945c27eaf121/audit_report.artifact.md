# Auditoría de Rendimiento: Pantalla de Inicio (App Azul)

Esta auditoría analiza los flujos de datos y la instanciación de servicios en la pantalla de inicio, identificando cuellos de botella que podrían sobrecargar el hilo principal (Main Thread).

## 📊 Arquitectura de Carga (HomeScreenComplete)

Actualmente, `HomeScreenComplete` actúa como el hub central de inicialización. Al entrar en la pantalla, se instancian y ejecutan los siguientes ViewModels casi simultáneamente:

1.  **HomeScreenViewModel**: Gestiona el estado de refresco (Ligero).
2.  **BeCerebroViewModel**: El orquestador global de datos (Pesado).
3.  **BeAsistenteViewModel**: El motor visual del asistente Be (Medio-Pesado).
4.  **CategoryViewModel**: Gestión de rubros y supercategorías (Medio-Pesado).
5.  **PromoViewModel**: Gestión de banners y anuncios (Pesado).
6.  **UbicacionClimaViewModel**: Sincronización de GPS y Clima (Medio).
7.  **ArmadorUsuarioViewModel**: Perfil e identidades (Medio).

### 🔍 Hallazgos Críticos

#### 1. Sobrecarga por `hiltViewModel()` en Cascada
Cuando `HomeScreenComplete` se compone, todas las llamadas a `hiltViewModel()` se ejecutan en el hilo principal. Si bien los ViewModels son ligeros por sí mismos, sus bloques `init {}` no lo son:
- **PromoViewModel**: Llama a `preCargarAnuncios(2)` en su `init`, lo que inicia el SDK de Google Ads de inmediato.
- **UbicacionClimaViewModel**: Llama a `inicializarUbicacionTactiva`, activando listeners de sensores.
- **BeAsistenteViewModel**: Inicia una recolección de flujos físicos.

#### 2. El "Combine" Gigante en BeAsistenteViewModel
El `uiState` de Be observa **15 flujos simultáneos**. Cualquier cambio en el GPS, en el texto de búsqueda, o en el estado de la cuenta dispara una recomposición del estado de Be.
- **Riesgo**: Si no hay `distinctUntilChanged` granulares, Be podría estar recalculando su "emoción" y "mensajes" miles de veces por segundo durante un scroll.

#### 3. Búsqueda y Mapeo en Hilo Principal
Aunque `CategoryViewModel` usa `flowOn(Dispatchers.Default)` correctamente en la mayoría de sus flujos, `BeCerebroViewModel.searchResults` realiza filtrado de listas y mapeo (`CategoriaMapper.aUiModel`) dentro de un `combine` que podría estar operando en el Main Thread si no se especifica el dispatcher.

#### 4. Generación de Banners Sincronizada
La función `promoViewModel.generateHomeBanners` se llama dentro de un `produceState`. Aunque es diferida por `canLoadHeavyWorkers`, si la lista de rubros (500+) es muy grande, el mapeo de rubros a banners puede causar micro-freezes al momento de aparecer.

## ⚙️ Servicios y Repositorios en Ejecución
Al iniciar la Home, se activan los siguientes servicios de fondo:

| Servicio | Responsabilidad | Impacto Inicial |
| :--- | :--- | :--- |
| `CategoriaRepository` | Carga el catálogo de rubros desde Room. | Medio |
| `FastMavRepository` | Sincroniza el historial de urgencias. | Bajo |
| `WeatherRepository` | Realiza una petición HTTP a OpenWeather. | Red |
| `ShortcutRepository` | Carga accesos directos desde Room. | Bajo |
| `Google Ads SDK` | Inicializa el motor de subastas y descarga assets. | **Alto** |
| `FirebaseAuth` | Verifica el token de sesión. | Red |

## 🚀 Recomendaciones de Optimización

1.  **Lazy Initialization**: Mover `hiltViewModel()` de los "obreros" (Promo, Ubicacion, Armador) dentro de `HomeScreenContent` o incluso más abajo, para que no se instancien hasta que la transición de pantalla termine.
2.  **Dispatcher Audit**: Asegurar que todos los `combine` en `BeCerebroViewModel` y `BeAsistenteViewModel` terminen con `.flowOn(Dispatchers.Default)`.
3.  **Ads Deferral**: No inicializar el Pool de anuncios nativos hasta que el usuario haya hecho scroll al menos 100dp.
4.  **Favoritos (Desacoplado)**: Como se propuso, mover Favoritos a su propio flujo elimina la necesidad de que la Home observe los cambios detallados de cada prestador favorito, reduciendo la carga del `BeCerebroViewModel`.

---
> [!IMPORTANT]
> El hilo principal se está saturando principalmente por la **concurrencia de inicializaciones** y no por un solo proceso pesado. El 350ms de `delay` actual es un parche; la solución real es fragmentar los ViewModels.
