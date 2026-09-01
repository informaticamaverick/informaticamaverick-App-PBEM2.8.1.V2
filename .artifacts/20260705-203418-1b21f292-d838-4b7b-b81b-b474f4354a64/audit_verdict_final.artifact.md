# Veredicto Final y Plan de Acción: Reestructuración Atómica Maverick (3x3)

## 🔍 Diagnóstico Final (El Veredicto)
El sistema actual sufre de un **"Colapso por Anidación"**. Al guardar Empresas y Sucursales como listas dentro del Usuario (JSON), Room se vuelve ciego a la jerarquía. La IU no puede filtrar lo que no existe como una entidad independiente.

**La solución definitiva NO es parchar, sino ATOMIZAR.**

---

## 🛠️ Plan de Acción: Los 4 Pilares de la Estabilidad

### Pilar 1: Atomización de Modelos
- **Separación de Archivos**: Crear `EmpresaMav.kt`, `SucursalMav.kt` y `EmpleadoMav.kt` como archivos independientes de dominio.
- **Relación por IDs**: Eliminar las listas anidadas físicas (`val sucursales: List<SucursalMav>`). Reemplazarlas por una arquitectura de **IDs de Referencia** (`idPadre`).

### Pilar 2: SSOT Local Atómico (Room)
- **Aplanamiento Radical**: `IdentidadMavEntity` seguirá siendo la tabla única, pero **CADA sucursal y CADA empresa será una fila real**.
- **Adiós a los TypeConverters de Listas**: Ya no guardaremos JSONs de sucursales. Si un usuario tiene 2 empresas y cada una 2 sucursales, Room tendrá **5 filas independientes** vinculadas por `idPropietario` e `idPadre`.

### Pilar 3: Inteligencia de Repositorio (Sync & Inherit)
- **Herencia Automática**: Al guardar/sincronizar, el Repositorio inyectará el `correoGoogle` del Usuario en todas sus Empresas y Sucursales hijas.
- **Propagación de Rubros**: Las Sucursales recibirán automáticamente las categorías de su Empresa padre para indexación en Firestore (`prestadores`).

### Pilar 4: Blindaje de Interfaz (IU Elite)
- **Contexto de Dirección**: Si la identidad es `SUCURSAL`, la IU solo mostrará **una dirección obligatoria** (o base). Se eliminará la opción de "direcciones adicionales" para evitar el error de lógica actual.
- **Reactividad por Tabs**: Las pestañas de sucursales funcionarán instantáneamente porque ahora consultarán identidades reales en Room usando el `idPadre`.

---

## 📋 Pasos de Ejecución Sugeridos

1.  **Dominio**: Mover `SucursalMav` y `EmpleadoMav` a sus propios archivos.
2.  **Entidad**: Limpiar `IdentidadMavEntity` eliminando la lista `val empresas`.
3.  **Repositorio**: Implementar el método `expandirYPersistirJerarquia()` que guarde individualmente cada nivel en Room y Firestore.
4.  **ViewModel**: Ajustar la carga de `misEntidades` para que sea una consulta plana filtrada por `idPropietario`.
5.  **IU**: Refactorizar `SeccionPerfilEmpresaMav` para que lea las sucursales desde el flujo de identidades planas.

---
**¿Deseas que proceda con este plan atómico para normalizar la arquitectura 3x3?**
