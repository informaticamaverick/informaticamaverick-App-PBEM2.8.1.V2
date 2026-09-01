# 🏛️ Protocolo de los 5 Pilares de Identidad Maverick (v2026.FINAL)

Este documento define la arquitectura soberana de identidades del ecosistema Informática Maverick. El sistema utiliza una jerarquía de 5 pilares para gestionar la complejidad de un usuario que puede ser, simultáneamente, un cliente, un profesional independiente y el dueño de una red de empresas.

---

## 🏗️ ARQUITECTURA DE LOS 5 PILARES

### PILAR #1: La Cuenta (El Maestro de Control)
*   **Entidad**: `CuentaMavEntity`
*   **Propósito**: Actúa como el "Root" o super-usuario. Centraliza la autenticación (UID de Firebase), el estado de suscripción (Google Play Billing) y la soberanía.
*   **Soberanía**: Decide qué perfil tiene el mando de la App mediante el flag `priorizarEmpresa` y el ID `idPerfilActivo`.
*   **Suscripción**: Es el único lugar donde reside el estado **Elite**. Si la cuenta es Elite, todos sus perfiles hijos (prestador y empresas) heredan los beneficios.

### PILAR #2: Humano Profesional (El Prestador)
*   **Entidad**: `IdentidadPrestadorMavEntity`
*   **Propósito**: Representa la identidad "independiente" del profesional. Acumula la reputación personal, los años de experiencia y las especialidades.
*   **Funcionamiento**: Permite que un profesional trabaje bajo su propio nombre sin necesidad de una estructura legal de empresa.

### PILAR #3: Entidad Legal (La Empresa / Marca)
*   **Entidad**: `EmpresaMavEntity`
*   **Propósito**: Contenedor de marca corporativa y datos fiscales (Razon Social, CUIT). 
*   **Jerarquía**: Una empresa no tiene ubicación física por sí misma; es una entidad lógica que agrupa múltiples sucursales (Pilares #4).

### PILAR #4: Punto de Venta (La Sucursal / POS)
*   **Entidad**: `SucursalMavEntity`
*   **Propósito**: Es el **Eje Geográfico**. Representa el lugar físico donde se presta el servicio o se retira el producto.
*   **Descubrimiento**: Es la entidad que se "aplana" en el índice de búsqueda. El cliente no busca "Empresas", busca "Sucursales cercanas".

### PILAR #5: Humano Cliente (El Usuario)
*   **Entidad**: `IdentidadUsuarioMavEntity`
*   **Propósito**: Perfil simplificado para la experiencia de consumo. Contiene los datos personales del cliente, sus direcciones de servicio y su historial de contratación.

---

## 🔄 TRANSFORMACIÓN Y FLUJO DE DATOS (Mappers)

El sistema utiliza una arquitectura de **Carga Dual** para optimizar el rendimiento (Ley #3) y el costo (Ley #2).

### 1. Carga Shallow (Ligera)
*   **Modelo**: `IdentidadShallowMav`
*   **Uso**: Resultados de búsqueda, listas de chat, mapas de calor.
*   **Origen**: Colección `indice_busqueda` en Firestore.
*   **Mapper**: `PrestadorMapper.deShallowAModeloUi`.
*   **Peso**: <1KB por documento.

### 2. Carga Deep (Profunda)
*   **Modelo**: `PrestadorCompletoMav` / `UsuarioCompletoMav`
*   **Uso**: Pantalla de Perfil Detallada.
*   **Origen**: Colecciones soberanas `prestadores` o `clientes`.
*   **Mapper**: `PrestadorMapper.deCompletoAModeloUi`.
*   **Acción**: Al activarse, actualiza el registro en Room y marca `esCargaCompleta = true`.

---

## 🎨 MODELOS DE INTERFAZ (UI Models)

Para garantizar la **Ley #1 (Pantallas Tontas)**, la UI nunca ve las Entidades de Room ni los DTOs de Firestore. Solo consume:

*   **`PrestadorUiModel`**: Contrato unificado que puede representar a un Humano Profesional o a una Sucursal de Empresa de forma indistinta para la interfaz.
*   **`UsuarioUiModel`**: Versión táctica para representar al cliente en chats o perfiles.

---

## ⚖️ REGLAS DE SOBERANÍA ATÓMICA (Ley #6)

El sistema aplica la lógica de **Deduplicación Táctica** para evitar ruido visual:

1.  **Modo Empresa**: Si el usuario activa "Priorizar Empresa", el sistema oculta automáticamente su Pilar #2 (Personal) del índice de búsqueda.
2.  **Deduplicación por CP**: Si una empresa tiene 3 sucursales en la misma zona, el motor de búsqueda las agrupa y solo muestra la más cercana, indicando la presencia de las otras mediante un badge dinámico.

---
**Informática Maverick - Departamento de Arquitectura de Identidad (2026)**
