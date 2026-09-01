# Plan de Implementación - Corrección de PERMISSION_DENIED en IndiceConcurso

El error `PERMISSION_DENIED` al intentar publicar un concurso en la colección `indice_concursos` sugiere que las Reglas de Seguridad de Firestore no están configuradas para permitir la escritura en esa colección específica, o que el modelo de datos no cumple con los requisitos de las reglas (como el nombre de los campos de propiedad).

Siguiendo la sugerencia del usuario de tomar `IndiceBusqueda` como modelo (el cual funciona correctamente), unificaremos el "Índice de Descubrimiento" utilizando la colección `indice_busqueda` para todos los elementos "shallow" (Prestadores, Sucursales y ahora Concursos). Esto aprovecha una colección con reglas de seguridad ya probadas y operativas.

## Cambios Propuestos

### Componente: Core (Modelos y Mapeadores)

#### [MODIFY] [IndiceConcursoShallowDominio.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/modelos/discovery/IndiceConcursoShallowDominio.kt)
- Agregar el campo `idPropietario` para alinearse con el modelo de seguridad de `IndiceBusqueda`.
- Agregar `tipoIdentidad` (fijo como "CONCURSO").

#### [MODIFY] [IndiceConcursoShallowMapper.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/dominio/mapeadores/discovery/IndiceConcursoShallowMapper.kt)
- Actualizar `deDominioAMapa` para incluir `idPropietario` (mapeado desde `idCliente`) y `tipoIdentidad`.
- Esto asegura que Firestore reconozca al autor del documento para validar los permisos de escritura.

### Componente: Repositorios de Índice (Escritura y Lectura)

#### [MODIFY] [IndiceConcursoUsuarioRepositorio.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/indices/concurso/IndiceConcursoUsuarioRepositorio.kt)
- Cambiar la constante `COLECCION_CONCURSOS` de `"indice_concursos"` a `"indice_busqueda"`.
- De esta manera, la App Azul (Usuario) publicará en la misma colección "Maestra" que ya funciona.

#### [MODIFY] [IndiceConcursoPrestadorRepositorio.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/indices/concurso/IndiceConcursoPrestadorRepositorio.kt)
- El lector también debe apuntar a `"indice_busqueda"` para encontrar los concursos publicados.
- Debido al sistema de etiquetas (`C_CP_RUBRO`), no habrá colisiones con los datos de prestadores (`P_CP_RUBRO`).

#### [MODIFY] [ConcursoRemoteMediator.kt](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/src/main/java/com/example/myapplication/core/datos/repositorios/ConcursoRemoteMediator.kt)
- Actualizar las referencias literales a `"indice_concursos"` por `"indice_busqueda"`.

## Plan de Verificación

### Pruebas Manuales
1.  **App Azul (Usuario)**: Intentar publicar un nuevo concurso desde el `BorradorConcursoViewModel`.
2.  **Logcat**: Verificar que el log `✅ [ATÓMICO_OK] Concurso publicado con éxito` aparezca sin errores de `PERMISSION_DENIED`.
3.  **App Naranja (Prestador)**: Verificar que los concursos aparezcan en el mercado local (la búsqueda ahora consultará `indice_busqueda` con el prefijo `C_`).

### Verificación de Reglas
- Al usar `idPropietario` y la colección `indice_busqueda`, el sistema debería validar correctamente:
  `allow write: if request.auth.uid == request.resource.data.idPropietario`
