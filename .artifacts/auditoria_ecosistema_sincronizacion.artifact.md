# Auditoría Completa del Ecosistema de Sincronización (v2026.ELITE)

Tras analizar el flujo de datos en la App Azul, App Naranja y el Núcleo (:core), presento el estado de situación para decidir el camino hacia las "Grandes Ligas".

## 1. Comparativa de Implementación (Azul vs. Naranja)

Ambas apps han adoptado el estándar **Elite v2026**, pero sus necesidades de negocio son divergentes:

| Característica | App Azul (Cliente) | App Naranja (Prestador) | Coincidencia |
| :--- | :--- | :--- | :--- |
| **Repositorio Local** | `SincUsuarioRepositorio` | `SincPrestadorRepositorio` | **Estructura idéntica** |
| **Colección Firestore** | `clientes` | `prestadores` | Divergente |
| **Complejidad Jerárquica** | Media (User -> Dirs) | Alta (Pro -> Emp -> Suc -> Infra) | Divergente |
| **Índice de Búsqueda** | No requiere | **Crítico** (`indice_busqueda`) | Divergente |
| **Worker de Fondo** | `SincUsuarioWorker` | `SincPrestadorWorker` | **Independientes** |

> [!IMPORTANT]
> **Conclusión**: Unificar la lógica "Deep" en un solo motor en el Core obligaría al Core a conocer estructuras complejas de ambas apps, creando un acoplamiento frágil. La separación actual es **arquitectónicamente superior** para escalabilidad.

## 2. El Vacío en el Core (La Necesidad del Motor)

El problema actual **no es la separación**, sino que el Core ha quedado huérfano de un **Resolutor de Identidades Comunes**.

### Piezas Afectadas:
- **Chat**: No puede mostrar el nombre/foto de la persona remota porque no tiene un servicio vivo en `:core` que haga el "Shallow Pull".
- **Bandeja de Entrada**: Los datos de los hilos de conversación dependen de resoluciones rápidas de UID -> Nombre.

## 3. Análisis del `MotorSincronizacionMav` (Legacy)

El motor antiguo contenía funciones óptimas de resolución básica que deberíamos rehabilitar, pero **sin la lógica de negocio pesada**.

### Funciones a Recuperar para Infraestructura:
1.  `descargarIdentidadShallow(uid, tipo)`: Solo para traer Nombre, Foto y Miniatura.
2.  `sincronizarCuenta(uid)`: Para asegurar que los datos de la cuenta (suscripción, etc) sean coherentes.

## 4. Diagnóstico "Grandes Ligas"

Para que el ecosistema sea de primer nivel, no debemos volver atrás, sino **formalizar el contrato de comunicación**:

1.  **Capa 1: Infraestructura (:core)** -> Un motor rehabilitado (limpio y auditable) que solo entienda de "Identidades Básicas".
2.  **Capa 2: Negocio (:app / :prestador)** -> Repositorios soberanos que manejen sus colecciones privadas y lógica compleja.

---
**¿Deseas que proceda a rehabilitar el motor en :core exclusivamente como un "Servicio de Identidad y Cuenta" para servir a ambas apps y al Chat?**
