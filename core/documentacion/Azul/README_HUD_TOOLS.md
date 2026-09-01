# 🛠️ Protocolo Maverick Elite: Herramientas del HUD (v2026.ELITE)

Este protocolo define el funcionamiento soberano de las herramientas de Be bajo la **Ley #12 (Sovereignty by Contract)** y la visión de **"Be vive en un solo lugar"**.

---

## 🏛️ ARQUITECTURA DE SOBERANÍA

Be Assistant es un **Vocero Puro**. No decide qué mostrar; solo refleja el contrato que la pantalla activa le dicta.

### 1. El Contrato (ConfiguracionContextoBe)
Cada pantalla es dueña absoluta de la configuración. 
*   **`primarias`**: Herramientas en la isla principal (ej: `archivo_chat`).
*   **`pistaBusqueda`**: El texto hint personalizado en la barra de Be.
*   **`mensajes`**: Lista de burbujas que Be dirá al entrar.

---

## 📡 INTERACCIÓN PANTALLA ↔️ ASISTENTE

### 1. El Rol del ViewModel (El Obrero Inteligente)
El ViewModel define **qué** herramientas necesita.

```kotlin
// --- DENTRO DEL VIEWMODEL ---
fun configurarHUD() {
    coordinador.actualizarContextoHUD(ContextoHUD.PRESUPUESTOS)
    coordinador.actualizarConfiguracionBe(
        ConfiguracionContextoBe(
            primarias = listOf("nuevo_concurso"),
            pistaBusqueda = "BUSCAR LICITACIÓN..."
        )
    )
}
```

### 2. El Rol de la Screen (El Dueño del Momento)
La Pantalla decide **cuándo** se reclama la soberanía usando `DisposableEffect`.

```kotlin
// --- DENTRO DE LA SCREEN (COMPOSABLE) ---
DisposableEffect(Unit) {
    viewModel.configurarHUD() 
    onDispose { }
}
```

---

## 🔍 BÚSQUEDA PROFESIONAL (Grandes Ligas)

Para filtrar datos, el ViewModel debe combinar su lógica local con el motor de búsqueda centralizado:

```kotlin
class MiViewModel(
    private val beMotor: BeBusquedaMotor
) : ViewModel() {

    val datosFiltrados = combine(
        misFiltrosLocales,
        beMotor.consultaNormalizadaDebounced
    ) { filtros, texto ->
        repository.buscarEnSql(filtros, texto)
    }
}
```

---

## 📐 FÍSICA Y GEOMETRÍA

### 1. Posición Inamovible
Be ya no "vuela". Mantiene un **Bias Vertical fijo de 0.85f** para ser predecible. Las hojas de datos (Sheets) deben respetar su espacio.

### 2. El Margen ROG (110.dp)
Obligatorio para evitar colisiones entre el HUD y el contenido de las hojas emergentes.

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
