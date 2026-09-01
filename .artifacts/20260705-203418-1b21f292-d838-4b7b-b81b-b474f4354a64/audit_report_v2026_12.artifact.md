# Informe de Auditoría Técnica: Arquitectura Maverick 3x3 (v2026.12)

Tras revisar el comportamiento reportado y analizar el código fuente, he identificado los cuellos de botella que impiden el correcto funcionamiento de las sucursales y la vinculación de Google.

## 1. El "Eslabón Perdido" en la Sincronización (Room vs Firestore)

**Problema**: En `IdentidadMavRepository.kt`, la función `sincronizarIdentidad` expande correctamente el Usuario en Empresas y Sucursales para subirlas a Firestore, pero **NO las guarda en la base de datos local (Room)**.
- **Consecuencia**: Cuando el ViewModel pide `obtenerTodas()` (que usa Room), solo recibe la identidad del Usuario. Las empresas y sucursales "desaparecen" localmente tras guardarse, por lo que la IU no muestra las pestañas (Tabs) de las nuevas sucursales.

## 2. Pérdida de Identidad Digital (Google Account)

**Problema**: Las entidades expandidas (Empresas y Sucursales) se crean como documentos nuevos pero **no heredan el `correoGoogle`** del Usuario raíz.
- **Consecuencia**: Al cambiar a la pestaña de una Empresa o Sucursal, la IU ve un campo `correoGoogle` vacío y muestra el botón "Vincular", ignorando que el dueño (Usuario) ya está vinculado.

## 3. Lógica de Pestañas y Navegación

**Problema**: En `SeccionPerfilEmpresaMav`, la lista de sucursales se filtra por `idPadre == identidad.id`.
- **Anomalía**: Si Room no tiene las sucursales como entidades independientes (debido al punto 1), esta lista siempre estará vacía o contendrá datos desactualizados, bloqueando la navegación entre Casa Central y sucursales nuevas.

## 4. Respuesta a Consultas de Estructura

- **Sobre `EmpresaMav.kt`**: Que `SucursalMav` esté dentro del mismo archivo no causa errores de lógica, pero **moverlo a su propio archivo** mejoraría la limpieza del código y evitaría confusiones en el futuro.
- **Sobre la Dirección Única**: Es correcto. Una sucursal debe tener **exactamente una dirección operativa**. Actualmente, la IU de edición hereda componentes que permiten añadir "direcciones adicionales" (útil para Usuarios Clientes, pero redundante para Sucursales). Esto debe ser restringido en la IU.

## Propuesta de Corrección (Sin aplicar cambios aún)

1.  **Sincronización Dual**: Modificar el repositorio para que cada Empresa y Sucursal expandida se guarde en Room (`identidadMavDao.insertarOActualizar`) inmediatamente antes de subirse a Firestore.
2.  **Herencia de Cuenta**: Asegurar que `correoGoogle` se propague del Usuario a todas sus entidades vinculadas durante la expansión.
3.  **Refactor de Modelos**: Separar `SucursalMav` y `EmpleadoMav` en archivos independientes para cumplir con el estándar de arquitectura limpia.
4.  **Blindaje de IU**: Ajustar `CardDireccionBase` para que, si el contexto es una `SUCURSAL`, se oculte la opción de añadir más direcciones, reforzando la regla de "Una dirección por punto de venta".

---
**Informática Maverick - Auditoría de Arquitectura**
