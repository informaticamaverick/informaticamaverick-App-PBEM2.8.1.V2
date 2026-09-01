# 🔍 Protocolo de Descubrimiento Atómico (v2026.FINAL)

Este manual define la ingeniería de "Grandes Ligas" para el motor de búsqueda de PBEM. Su objetivo es garantizar resultados instantáneos (Ley #4) con un consumo de datos casi nulo (Ley #2) y soporte offline total.

---

## 🏛️ 1. INFRAESTRUCTURA: LA VISTA SOBERANA

Para evitar la duplicidad física de datos entre prestadores humanos y empresas, el sistema utiliza una **`DatabaseView`** de Room llamada `ResultadosBusquedaView`.

### Lógica de Consolidación (SQL)
La vista realiza un `UNION ALL` entre las tablas `prestadores_mav` y `sucursales_mav`.
- **Identidad Independiente**: Se mapea desde la tabla de prestadores con `tipo = INDIVIDUAL`.
- **Identidad Corporativa**: Se mapea desde la tabla de sucursales con `tipo = SUCURSAL`.
- **Integridad**: Cualquier cambio en el perfil original (ej: nueva foto) se refleja en la vista de búsqueda sin necesidad de sincronización adicional.

---

## ⚙️ 2. EL MOTOR DE CARGA DUAL (SHALLOW TO DEEP)

El descubrimiento se divide en dos fases tácticas para proteger el Hilo Principal y optimizar el ancho de banda.

### Fase 1: Descubrimiento Shallow (Carga Ligera)
1.  **Petición**: El `BusquedaRemoteMediator` solicita un lote (pageSize: 15) a la colección `indice_busqueda` de Firestore.
2.  **Uso de Huellas**: La consulta se filtra exclusivamente por la llave normalizada generada por el `MotorDescubrimientoMav` (ej: `4000_plomeria`).
3.  **Persistencia Atómica**: Los datos descargados se inyectan en Room marcando `esCargaCompleta = false`. La UI se actualiza instantáneamente desde la base de datos local.

### Fase 2: Perfil Deep (Carga Profunda)
1.  **Trigger**: El usuario selecciona un ítem de la lista.
2.  **Validación Local-First**: El repositorio verifica en Room si el perfil ya existe y tiene menos de 24 horas.
3.  **Acción**: Si no es así, dispara una descarga profunda desde la colección `prestadores`.
4.  **Actualización**: Se completa la biografía, galerías de imágenes y datos legales en el registro existente en Room, marcando `esCargaCompleta = true`.

---

## ⚖️ 3. PRIORIDAD ELITE Y ORDENAMIENTO SQL

El ordenamiento es dictado por el Core mediante SQL nativo para garantizar la **Ley de Inmediatez**:

```sql
SELECT * FROM ResultadosBusquedaView 
WHERE filtrosBusqueda LIKE '%' || :huella || '%'
ORDER BY estaSuscrito DESC, reputacion DESC
```

*   **Regla #1**: Los usuarios con suscripción activa (`estaSuscrito = 1`) ocupan siempre los primeros lugares (Status Elite).
*   **Regla #2**: Dentro de cada nivel de suscripción, se ordena por la reputación (estrellas) de mayor a menor.

---

## 🛠️ 4. MANUAL DE IMPLEMENTACIÓN PARA DESARROLLADORES

1.  **Nuevos Rubros**: Nunca escribas el nombre del rubro a mano. Usa `MotorDescubrimientoMav.generarLlaveBusqueda(cp, categoria)`.
2.  **Filtros Aditivos**: Para filtrar por "24hs" o "Verificado", el DAO realiza un filtrado local (Ley #2) sobre los tags de la vista.
3.  **Caché**: Si necesitas borrar el caché de búsqueda por cambio de ciudad, utiliza `baseDeDatos.claveRemotaBusquedaDao().limpiarTodo()`.

---
**Informática Maverick - División de Ingeniería de Datos (2026)**
