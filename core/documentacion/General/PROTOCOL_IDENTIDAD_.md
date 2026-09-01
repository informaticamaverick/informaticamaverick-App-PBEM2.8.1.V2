# 🆔 Protocolo Maverick Elite: Gestión de Identidades (v2026.31 - Atómico)

Este manual define el estándar de **Aplanamiento Radical** para el sistema de identidades, eliminando la herencia anidada y garantizando la soberanía de datos y la integridad de borrado (Anti-Zombies).

---

## 🏛️ 1. ARQUITECTURA DE IDENTIDAD PLANA

Maverick Elite abandona los árboles JSON. Cada entidad es un documento independiente y plano en la nube.

### A. La Colección Unificada: `prestadores`
Todos los tipos de identidad residen en la colección `prestadores`. 
*   **Identidades Raíz**: `USUARIO` (Humano) y `EMPRESA` (Corporativo).
*   **Identidades Operativas**: `SUCURSAL` (Punto de servicio).
*   **Vínculo de Propiedad**: Se utiliza la variable `idPropietario` (UID del humano) para orquestar el mapa de identidades del usuario.

### B. Desacoplamiento Total (Ley #9)
Se prohíbe terminantemente guardar listas de objetos hijos dentro del documento del padre.
*   **Incorrecto**: Guardar una lista de sucursales dentro del documento de la Empresa.
*   **Correcto**: Cada sucursal es un documento propio con un `idPadre` que apunta a la Empresa.

---

## 🔄 2. FLUJO DE SINCRONIZACIÓN ELITE

### A. Sincronización Local-First (Ley #2)
Los cambios impactan instantáneamente en **Room** a través de `IdentidadMavRepository.sincronizarLocal()`. La UI reacciona mediante `Flow` (Ley #4).

### B. Sincronización Remota Diferida
Para optimizar el costo de red, la subida a Firebase se realiza por confirmación de sesión mediante `IdentidadMavRepository.sincronizarRemoto()`.

### C. Borrado Atómico Garantizado
Al eliminar una identidad, el sistema limpia tres frentes simultáneamente:
1.  Fila física en Room (DAO).
2.  Documento plano en colección `prestadores`.
3.  Entrada de búsqueda en colección `indice_busqueda`.

---

## 📂 3. COMPONENTES INTERVINIENTES

| Archivo | Rol Técnico |
| :--- | :--- |
| `IdentidadUsuarioMavEntity.kt` | Definición de tabla plana para el Humano Cliente. |
| `IdentidadPrestadorMavEntity.kt` | Definición de tabla plana para el Humano Profesional. |
| `IdentidadMavRepository.kt` | Orquestador de carga atómica y ensamblado de jerarquía en memoria. |
| `SesionMavRepository.kt` | Gestiona el *Identity Map* y el *Warm-up* proactivo en el Login. |
| `IdentidadMavMapper.kt` | Traductor universal que fuerza el aplanamiento al mapear desde Firestore. |

---

## 🛡️ 4. SOBERANÍA ADMINISTRATIVA

El dueño de la cuenta (`idPropietario == currentUID`) mantiene acceso total de lectura/escritura a todas sus identidades. El flag `priorizarEmpresa` afecta únicamente a la **visibilidad en el índice de búsqueda externo**, pero nunca bloquea la gestión del administrador (Ley #6 Refinada).

---
**Informática Maverick - Departamento de Arquitectura de Software (2026)**
