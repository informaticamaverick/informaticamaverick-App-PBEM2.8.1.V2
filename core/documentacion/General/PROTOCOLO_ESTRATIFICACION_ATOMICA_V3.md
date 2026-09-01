# ⚛️ Protocolo Maverick Elite: Estratificación Atómica V3

Este protocolo define la jerarquía técnica de construcción de componentes en Jetpack Compose, siguiendo los estándares de **"Grandes Ligas"** y las recomendaciones oficiales del **Android Knowledge Base**.

---

## ⚖️ FILOSOFÍA: Fragmentar para Vencer

Se prohíbe el uso del patrón **"Master Builder"**. En su lugar, el sistema se construye por capas de especialización. Una función Composable debe resolver **exactamente un problema**.

---

## 🏛️ LAS 4 CAPAS DEL COMPONENTE

### 1. Nivel 1: ÁTOMOS (Pureza Visual)
*   **Archivos**: `MoldeMenuPiezas.kt`, `PiezasHerramientasBe.kt`.
*   **Características**:
    *   Son **"Sordomudos"**: No conocen la base de datos ni el estado global.
    *   **Configurables**: Reciben parámetros granulares (colores, iconos, etiquetas).
    *   **Agnósticos**: Se nombran por su forma, no por su función (ej: `MenuItemEliteV3` en lugar de `MenuPerfilItem`).

### 2. Nivel 2: MOLDES (Infraestructura y Física)
*   **Archivos**: `MoldeMenuArmador.kt`, `MoldeSheetEmergenteV3.kt`.
*   **Características**:
    *   Gestionan el **"Envase"**: Sombras, bordes neón, colas (flechas) y zIndex.
    *   **Física Pasiva**: Dueños de las animaciones (Spring/Bounciness) y el posicionamiento.
    *   **Slot APIs**: Nunca deciden qué hay dentro. Exponen lambdas (`content: @Composable () -> Unit`) para inyectar el contenido.

### 3. Nivel 3: ARMADORES (Inteligencia de Contenido)
*   **Archivos**: `ArmadorMenuV3.kt`, `ArmadorMoldeListaV3.kt`.
*   **Características**:
    *   Son el **"Cerebro de Ensamblaje"**: Saben qué átomos deben ir juntos para un propósito específico.
    *   **Funciones Delimitadas**: Contienen funciones como `MenuPerfilContenido` o `MenuFiltrosContenido`.
    *   **Estandarización**: Garantizan que el menú de perfil se vea igual en toda la app.

### 4. Nivel 4: ESPECIALISTAS (Componentes de Opinión)
*   **Archivos**: `MenuUbicacionV3.kt` (integrado), `MenuClimaV3.kt` (integrado).
*   **Características**:
    *   Son el **"Punto de Entrada"** para las pantallas.
    *   Unen un **Molde** con un **Armador**.
    *   **Soberanía de Pre-set**: Ya traen configurada la geometría (ej: `esAiry = true`, `alignment = TopEnd`) para que la pantalla no tenga que adivinar.

---

## 🔍 ESTÁNDAR ANDROID (RECOMENDACIÓN OFICIAL)

1.  **Preferir Composición sobre Configuración**: En lugar de pasar un objeto `Style` gigante, pasa pequeñas funciones o lambdas.
2.  **Hoisting de Estado**: El componente de nivel bajo no debe tener estado propio (ej: `expanded`). El estado debe ser manejado por la pantalla u orquestador.
3.  **Evitar el "Grab-bag Style"**: No crees componentes con 20 parámetros opcionales. Si un componente necesita demasiadas opciones, divídelo en dos componentes especialistas.

---

## 🔄 FLUJO DE CONSTRUCCIÓN "ELITE"

1.  **Pantalla** llama al **Especialista** (`MenuUbicacionV3`).
2.  **Especialista** envuelve todo en un **Molde** (`MoldeMenuArmadorV3`).
3.  El **Molde** recibe el contenido del **Armador** (`MenuUbicacionContenido`).
4.  El **Armador** dibuja los **Átomos** (`MenuItemEliteV3`).

---
**Informática Maverick - Departamento de Arquitectura de Sistemas (2026)**
