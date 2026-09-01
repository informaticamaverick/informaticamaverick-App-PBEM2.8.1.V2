# 🔍 Auditoría Técnica: Ecosistema de Direcciones Maverick (SSOT 2026)

Esta auditoría analiza la implementación actual de direcciones frente a los requerimientos de multi-perfil (App Azul y App Naranja) y las leyes internas de arquitectura.

## ⚖️ Veredicto: INCONSISTENTE
Aunque la base técnica es sólida (Offline-First, Room, Flow), existe una **Brecha Semántica** en la entidad de direcciones que causa pérdida de datos y fallos en el mapeo UI.

---

## ✅ Lo que está BIEN (Conforme a la Ley)

1.  **Ley #16 (Tabla Tablita Tablón):** La dirección está atomizada en `DireccionMavEntity`. No es un campo gigante de texto, sino una entidad relacionada.
2.  **Ley #4 (Inmediatez):** Las entidades `SucursalMavEntity` e `IdentidadPrestadorMavEntity` poseen campos "espejo" (`codigoPostal`, `latitud`, `longitud`) para búsquedas instantáneas sin necesidad de JOINs complejos.
3.  **Local-First:** El DAO `DireccionMavDao` utiliza `Flow`, permitiendo que la UI reaccione a cambios en tiempo real.
4.  **Estandarización:** Se respetan los protocolos de idioma (Español) y auditoría (Huellas de Pan en comentarios).

---

## ❌ Lo que está MAL (El "Gran Problema")

### 1. Ambigüedad de Relación (El Vacío Semántico)
La entidad `DireccionMavEntity` utiliza un `idReferencia` genérico.
*   **Problema:** No existe un campo `tipoDireccion` (Enum). El sistema no sabe si esa dirección es la "Principal del Usuario", una "Sucursal de Referencia", o un "Punto de Venta (POS)".
*   **Consecuencia:** Al recuperar datos, el Mapper debe "adivinar" mediante flags como `esEmpresa`, lo cual es propenso a errores y no profesional.

### 2. Violación del Principio de Responsabilidad Única (SRP)
*   **Problema:** `DireccionMavEntity` y el modelo `DireccionMav` contienen campos como `nombreSucursal` y `esEmpresa`.
*   **Falla:** La dirección debería ser agnóstica a quién la posee. Los datos del poseedor pertenecen a la relación, no al lugar físico.

### 3. Gestión de Multi-Perfiles (Google Standards)
Según los estándares de Google Maps/Places y arquitecturas multi-tenant:
*   Un usuario debe tener un `Relationship` con la dirección que defina su rol (EJ: `HOME`, `WORK`, `BILLING`).
*   Tu estructura actual intenta meter el rol dentro de la dirección, lo que rompe cuando un mismo punto físico tiene dos roles.

### 4. Fragilidad en App Naranja
*   El Prestador necesita múltiples direcciones (Áreas de servicio) y las Sucursales necesitan una fija (POS).
*   Sin un discriminador de `Tipo`, el `DireccionMavDao` devuelve una lista plana donde se mezclan áreas de cobertura con locales físicos.

---

## 🛠️ Propuesta de Mejora "Elite"

### 1. Definir el `TipoDireccionMav`
Crear un Enum que defina el propósito de la dirección:
*   `PERFIL_PERSONAL`: Dirección del humano.
*   `SUCURSAL_REFERENCIA`: App Azul, solo informativa.
*   `PUNTO_VENTA_POS`: App Naranja, operativa.
*   `AREA_COBERTURA`: Para prestadores a domicilio.

### 2. Refactorizar `DireccionMavEntity`
Eliminar flags de contexto y añadir el `tipo`.
```kotlin
@Entity(tableName = "direcciones_mav")
data class DireccionMavEntity(
    @PrimaryKey val id: String,
    val idReferencia: String,
    val tipo: TipoDireccionMav, // <-- CRÍTICO
    // ... datos geográficos puros ...
)
```

### 3. Implementar POJOs de Relación
Siguiendo la **Ley #16**, no cargar direcciones manuales con `@Ignore`. Usar:
```kotlin
data class UsuarioConDirecciones(
    @Embedded val usuario: IdentidadUsuarioMavEntity,
    @Relation(parentColumn = "id", entityColumn = "idReferencia")
    val direcciones: List<DireccionMavEntity>
)
```

---

## 🌐 Estándares de la Industria (Google & Uber Patterns)
Para manejar **Multi-Perfiles**, los líderes del sector utilizan:
1.  **Place Objects:** Datos inmutables de la ubicación.
2.  **UserPlaces / EntityLocations:** Tablas pivotales que añaden metadatos (etiqueta personalizada, tipo de uso, si es la principal).

**Recomendación:** Tu proyecto debería evolucionar hacia este patrón para que las direcciones "no se pierdan" al cambiar de perfil de App Azul a App Naranja.

---
**Auditoría realizada por: Senior AI Architect**
**Fecha:** 2026-08-09
